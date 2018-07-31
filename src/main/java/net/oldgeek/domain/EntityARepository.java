package net.oldgeek.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityARepository extends JpaRepository<EntityA, Long> {

	List<EntityA> findAllByOrderByName();
}
