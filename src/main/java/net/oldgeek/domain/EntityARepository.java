package net.oldgeek.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityARepository extends JpaRepository<EntityA, Long> {

}
