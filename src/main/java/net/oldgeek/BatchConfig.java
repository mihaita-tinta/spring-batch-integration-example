package net.oldgeek;

import java.util.Collections;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;

import net.oldgeek.domain.EntityA;
import net.oldgeek.domain.EntityARepository;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	EntityARepository repo;

	@Bean
	Step sampleStep() {
		return stepBuilderFactory.get("sampleStep")//
				.<EntityA, EntityA>chunk(5) //
				.reader(itemReader(null, null)) //
				.writer(i -> {
					System.out.println("new chunk");
					i.stream().forEach(j -> System.out.println(j));
				
				}) //
				.build();
	}

	@Bean
	Job sampleJob() {
		Job job = jobBuilderFactory.get("sampleJob") //
				.incrementer(new RunIdIncrementer()) //
				.start(sampleStep()) //
				.build();
		return job;
	}

	@Bean
	@StepScope
	ItemReader<EntityA> itemReader(@Value("#{jobParameters[file_path]}") String filePath,
										@Value("#{jobParameters}") Map<String, Object> params) {

        RepositoryItemReader<EntityA> reader = new RepositoryItemReader<>();
        reader.setRepository(repo);
        reader.setMethodName("findAll");        
        reader.setSort(Collections.singletonMap("name", Direction.ASC));
		return reader;
	}

}
