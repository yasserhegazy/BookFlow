package com.bookflow.db;

import java.time.Instant;

import com.bookflow.auth.entity.RefreshToken;
import com.bookflow.auth.repository.RefreshTokenRepository;
import com.bookflow.common.enums.RoleName;
import com.bookflow.common.enums.UserStatus;
import com.bookflow.config.JpaAuditingConfig;
import com.bookflow.users.entity.Role;
import com.bookflow.users.entity.User;
import com.bookflow.users.repository.RoleRepository;
import com.bookflow.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DatabaseFoundationTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("bookflow_test")
			.withUsername("bookflow")
			.withPassword("bookflow");

	@DynamicPropertySource
	static void configureDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void shouldRunMigrationsAndSeedRoles() {
		var tableCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.tables
				WHERE table_schema = 'public'
				  AND table_name IN ('users', 'roles', 'user_roles', 'refresh_tokens')
				""", Integer.class);

		assertThat(tableCount).isEqualTo(4);
		assertThat(roleRepository.findAll())
				.extracting(Role::getName)
				.containsExactlyInAnyOrder(
						RoleName.SUPER_ADMIN,
						RoleName.BUSINESS_OWNER,
						RoleName.STAFF,
						RoleName.CUSTOMER);
	}

	@Test
	void shouldPersistUserRoleAndRefreshTokenWithAuditingFields() {
		var customerRole = roleRepository.findByName(RoleName.CUSTOMER).orElseThrow();
		var user = new User(
				"Yasser Hegazy",
				"yasser@example.com",
				"$2a$10$hashed-password-value",
				"+970000000000",
				UserStatus.ACTIVE);
		user.addRole(customerRole);

		var savedUser = userRepository.saveAndFlush(user);
		var refreshToken = refreshTokenRepository.saveAndFlush(new RefreshToken(
				savedUser,
				"hashed-refresh-token",
				Instant.now().plusSeconds(3600)));

		entityManager.clear();

		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getCreatedAt()).isNotNull();
		assertThat(savedUser.getUpdatedAt()).isNotNull();
		assertThat(savedUser.getVersion()).isNotNull();
		assertThat(refreshToken.getCreatedAt()).isNotNull();
		assertThat(userRepository.existsByEmail("yasser@example.com")).isTrue();
		assertThat(refreshTokenRepository.existsByTokenHash("hashed-refresh-token")).isTrue();
	}

	@Test
	void shouldRejectDuplicateUserEmail() {
		userRepository.saveAndFlush(new User(
				"First User",
				"duplicate@example.com",
				"$2a$10$first-hashed-password",
				null,
				UserStatus.ACTIVE));

		var duplicateUser = new User(
				"Second User",
				"duplicate@example.com",
				"$2a$10$second-hashed-password",
				null,
				UserStatus.ACTIVE);

		assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

}
