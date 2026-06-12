package com.bookflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableJpaAuditing
@Profile("!test")
public class JpaAuditingConfig {
}
