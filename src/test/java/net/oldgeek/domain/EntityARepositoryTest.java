package net.oldgeek.domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class EntityARepositoryTest {
	
	@Autowired
	EntityARepository repo;
	
	@Test
	public void test() {
		EntityA a = new EntityA();
		a.setName("abc");
		EntityA saved = repo.saveAndFlush(a);
		assertNotNull(saved.getId());
		
		repo.findAllByOrderByName().forEach(System.out::println);
	}

}
