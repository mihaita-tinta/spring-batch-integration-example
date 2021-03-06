package net.oldgeek.domain;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class EntityA {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	
	private String name;
	
	@OneToOne
	private EntityB b;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EntityB getB() {
		return b;
	}

	public void setB(EntityB b) {
		this.b = b;
	}

	@Override
	public String toString() {
		return "EntityA [id=" + id + ", name=" + name + "]";
	}

}
