package com.bookflow.auth.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.bookflow.auth.dto.CurrentUserResponse;
import com.bookflow.users.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public CurrentUserResponse toCurrentUserResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new CurrentUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus().name(),
                roles
        );
    }

}
