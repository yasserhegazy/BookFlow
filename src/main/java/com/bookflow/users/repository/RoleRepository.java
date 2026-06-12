package com.bookflow.users.repository;

import java.util.Optional;

import com.bookflow.common.enums.RoleName;
import com.bookflow.users.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByName(RoleName name);

}
