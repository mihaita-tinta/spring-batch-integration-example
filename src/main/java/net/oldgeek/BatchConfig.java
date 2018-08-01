package net.oldgeek;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort.Direction;

import net.oldgeek.domain.EntityA;
import net.oldgeek.domain.EntityARepository;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	private int maxThreads = 10;

	@Autowired
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	EntityARepository repo;

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(maxThreads);
		return taskExecutor;
	}

	@Bean
	Job sampleJob() {
		Job job = jobBuilderFactory.get("sampleJob") 
				.incrementer(new RunIdIncrementer()) 
				.start(processFromListStep()) 
				.next(processFromRepositoryStep()) 
				.build();
		return job;
	}
	
	@Bean
	Step processFromListStep() {
		return stepBuilderFactory.get("processFromListStep")
				.<Integer, EntityA>chunk(10) 
				.reader(listItemReader())
				.processor(item -> {
					System.out.println("processFromListStep - processor item " + item + " " + Thread.currentThread());
						EntityA a = new EntityA();
						a.setName("entity-" + item);
						return a;
				})
				.writer(list -> {
					System.out.println("processFromListStep - writer new chunk " + list.size() + " " + Thread.currentThread());
					repo.save(list);
				
				})
				.build();
	}
	
	@Bean
	@StepScope
	ItemReader<Integer> listItemReader() {
		return new ListItemReader<>(IntStream.range(0, 1000)
									.mapToObj(i -> i)
									.collect(Collectors.toList()));
	}
	
	@Bean
	Step processFromRepositoryStep() {
		return stepBuilderFactory.get("processFromRepositoryStep")
				.<EntityA, EntityA>chunk(10) 
				.reader(repoItemReader(null, null)) 
				.writer(i -> {
					System.out.println("processFromRepositoryStep - writer new chunk " + i.size() + " " + Thread.currentThread());
					i.stream().forEach(j -> System.out.println(j + " " + Thread.currentThread()));
				
				})
				.taskExecutor(taskExecutor())
				.throttleLimit(maxThreads)
				.build();
	}

	@Bean
	@StepScope
	ItemReader<EntityA> repoItemReader(@Value("#{jobParameters[file_path]}") String filePath,
										@Value("#{jobParameters}") Map<String, Object> params) {

        RepositoryItemReader<EntityA> reader = new RepositoryItemReader<>();
        reader.setRepository(repo);
        reader.setPageSize(50);
        reader.setMethodName("findAll");        
        reader.setSort(Collections.singletonMap("name", Direction.ASC));
		return reader;
	}

}
