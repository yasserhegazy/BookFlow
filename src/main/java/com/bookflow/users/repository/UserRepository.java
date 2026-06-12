package com.bookflow.users.repository;

import java.util.Optional;

import com.bookflow.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	boolean existsByEmail(String email);

}
