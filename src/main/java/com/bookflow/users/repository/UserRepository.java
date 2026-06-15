package com.bookflow.users.repository;

import java.util.Optional;

import com.bookflow.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email AND u.deletedAt IS NULL")
	Optional<User> findActiveByEmailWithRoles(@Param("email") String email);

}
