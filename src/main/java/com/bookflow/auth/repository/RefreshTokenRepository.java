package com.bookflow.auth.repository;

import java.util.Optional;

import com.bookflow.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	boolean existsByTokenHash(String tokenHash);

}
