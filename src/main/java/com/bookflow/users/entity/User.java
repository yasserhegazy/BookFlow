package com.bookflow.users.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.bookflow.common.entity.AuditableEntity;
import com.bookflow.common.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(length = 50)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserStatus status = UserStatus.ACTIVE;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	protected User() {
	}

	public User(String name, String email, String passwordHash, String phone, UserStatus status) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.phone = phone;
		this.status = status;
	}

	public void addRole(Role role) {
		roles.add(role);
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getPhone() {
		return phone;
	}

	public UserStatus getStatus() {
		return status;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public Set<Role> getRoles() {
		return Set.copyOf(roles);
	}

}
