package net.oldgeek.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityBRepository extends JpaRepository<EntityB, UUID> {

	List<EntityB> findAllByOrderByName();
}
