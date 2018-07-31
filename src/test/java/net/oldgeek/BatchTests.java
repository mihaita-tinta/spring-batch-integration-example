package net.oldgeek;

import java.util.Date;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.oldgeek.domain.EntityA;
import net.oldgeek.domain.EntityARepository;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class BatchTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	EntityARepository repo;
	
	@Before
	public void before() {

		IntStream.range(0, 100)
			.mapToObj(i -> {
				EntityA a = new EntityA();
				a.setName("entity-" + i);
				return a;
			})
			.forEach(repo :: save);
	}
	@Test
	public void testSampleJob() throws Exception {
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString("file_path", "src/test/resources/sample.txt");
		// We add a dummy value to make job params unique, or else spring batch
		// will only run it the first time
		jobParametersBuilder.addDate("dummy", new Date());
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParametersBuilder.toJobParameters());
		System.out.println(jobExecution.getExitStatus());
	}
}
