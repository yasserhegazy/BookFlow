package com.bookflow.users.entity;

import java.time.Instant;

import com.bookflow.common.entity.BaseEntity;
import com.bookflow.common.enums.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
public class Role extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 50)
	private RoleName name;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Role() {
	}

	public Role(RoleName name) {
		this.name = name;
	}

	public RoleName getName() {
		return name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
