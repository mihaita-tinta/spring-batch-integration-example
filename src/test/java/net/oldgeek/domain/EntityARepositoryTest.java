package net.oldgeek.domain;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
public class EntityARepositoryTest {
	
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
		
		repoA.findAllByOrderByName().forEach(System.out::println);
	}
	

	@Sql("/entitya-h2.sql")
	@Test
	public void testFindOne() {
		repoA.findAllByOrderByName().forEach(System.out::println);
		EntityA a = repoA.findOne(UUID.fromString("483aba6f-4a38-4540-9f4a-9a09ff9dda1c"));
		assertNotNull(a);
	}
}
