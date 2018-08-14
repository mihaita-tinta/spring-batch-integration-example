package net.oldgeek;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.PeekableItemReader;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringBatchCompletionPolicyTest {
    
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("sampleJob")
    private Job sampleJob;

    @Test
    public void when_completionPolicy_expect_chunkIsGroupedByCategory() throws Exception {
        JobExecution jobExecution = jobLauncher.run(sampleJob, new JobParametersBuilder().toJobParameters());
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

    @TestConfiguration
    @EnableBatchProcessing
    public static class SampleJob {

        @Autowired
        JobBuilderFactory jobBuilderFactory;

        @Autowired
        StepBuilderFactory stepBuilderFactory;

        @Bean
        public Job sampleJob() {
            return jobBuilderFactory
                .get("userDataOnDemandRefreshJob")
                .incrementer(new RunIdIncrementer())
                .start(sampleStep())
                .build();
        }

        @Bean
        public ChunkListener sampleChunkListener() {
            return new ChunkListener() {
                @Override
                public void beforeChunk(ChunkContext context) {
                    log.info("beforeChunk - afterChunk " + Thread.currentThread());
                }

                @Override
                public void afterChunk(ChunkContext context) {
                    log.info("afterChunk - afterChunk " + Thread.currentThread());
                }

                @Override
                public void afterChunkError(ChunkContext context) {
                    log.info("afterChunkError - afterChunkError " + Thread.currentThread());
                }
            };
        }

        @Bean
        Step sampleStep() {
            PeekingCompletionPolicyReader reader = new PeekingCompletionPolicyReader(
                new PeekableListItemReader<>(IntStream.range(0, 100)
                .mapToObj(i -> {
                    return EntityA.builder()
                        .id(i)
                        .category(i % 2)
                        .build();
                })
                .sorted(Comparator.comparing(o -> o.category))
                .collect(Collectors.toList()))
            );

            return stepBuilderFactory.get("sampleStep")
                .<EntityA, EntityA>chunk(reader)
                .reader(reader)
                .processor(sampleProcessor())
                .writer(list -> {
                    log.info("sampleStep - writer new chunk " + list.size() + " " + Thread.currentThread());
                })
                .listener(sampleChunkListener())
                .build();
        }

        @Bean
        @StepScope
        ItemProcessor<EntityA, EntityA> sampleProcessor() {
            return item -> {
                log.info("sampleStep - processor item " + item + " " + Thread.currentThread());
                item.setName("entity-" + item);
                return item;
            };
        }

    }

    @Data
    @Builder
    public static class EntityA {
        int id;
        int category;
        String name;
    }

    public static class PeekableListItemReader<T> implements PeekableItemReader<T> {
        private List<T> list;

        public PeekableListItemReader(List<T> list) {
            this.list = new ArrayList<T>(list);
        }

        @Override
        public T read() {
            if (!list.isEmpty()) {
                return list.remove(0);
            }
            return null;
        }

        @Override
        public T peek() {
            if (list.isEmpty())
                return null;
            return list.get(0);
        }
    }

    public static class PeekingCompletionPolicyReader extends SimpleCompletionPolicy implements ItemReader<EntityA> {

        private final PeekableItemReader<? extends EntityA> delegate;

        private EntityA currentReadItem = null;

        public PeekingCompletionPolicyReader(PeekableItemReader<? extends EntityA> delegate) {
            this.delegate = delegate;
        }

        @Override
        public EntityA read() throws Exception {
            currentReadItem = delegate.read();
            return currentReadItem;
        }

        @Override
        public RepeatContext start(final RepeatContext context) {
            return new ComparisonPolicyTerminationContext(context);
        }

        protected class ComparisonPolicyTerminationContext extends SimpleTerminationContext {
            final RepeatContext context;
            public ComparisonPolicyTerminationContext(final RepeatContext context) {
                super(context);
                this.context = context;
            }

            @Override
            public boolean isComplete() {
                try {
                    EntityA nextReadItem = delegate.peek();
                    context.setAttribute("category", currentReadItem.category);
                    if (nextReadItem == null)
                        return true;

                    // logic to check if same country
                    if (currentReadItem.category == nextReadItem.category) {
                        return false;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
                return true;
            }
        }
    }
}
