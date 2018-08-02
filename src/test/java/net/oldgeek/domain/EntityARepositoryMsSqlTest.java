package net.oldgeek.domain;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * docker pull microsoft/mssql-server-linux:2017-latest
 * docker-machine stop
 * ------> from virtual box increase memory to 2GB at least
 * docker-machine start
 * docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=YourStrong!Passw0rd' -p 1433:1433 --name sql1 -d microsoft/mssql-server-linux:2017-latest
 * winpty docker exec -it sql1 sh
 * /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'YourStrong!Passw0rd'
 * CREATE DATABASE TestDB
 * select name from sys.databases
 * use TestDB
 * GO
 * SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'entitya'
 * sp_help entitya
 *  select cast(id as uniqueidentifier), name from entitya where id = cast('25A35B4D-7DB3-7741-ACEA-1821A5C0A43D' as uniqueidentifier)
 * @author mih
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("mssql")
public class EntityARepositoryMsSqlTest {

	@Autowired
	EntityARepository repoA;
	
	@Autowired
	EntityBRepository repoB;
	
	@Test
	public void test() {
		

		EntityA a = new EntityA();
		a.setName("abc");
		EntityA saved = repoA.saveAndFlush(a);
		assertNotNull(saved.getId());
		
		repoA.findAllByOrderByName().forEach(System.out::println);
		saved = repoA.findOne(a.getId());
		assertNotNull(saved);
	}

	@Test
	public void testBatch() {

		List<EntityA> list = IntStream.range(0, 10)
			.mapToObj(i -> {

				EntityA a = new EntityA();
				a.setName("abc" + i);
				return a;
			}).collect(Collectors.toList());
		repoA.save(list);
	}
	
	@Test
	public void testSaveAWithB() {
		
		EntityB b = new EntityB();
		b.setName("bbb");
		
		repoB.save(b);
		
		EntityA a = new EntityA();
		a.setName("abc");
		a.setB(b);
		EntityA saved = repoA.saveAndFlush(a);
		assertNotNull(saved.getId());
		
		repoA.findAllByB(b).forEach(System.out::println);
	}
}
