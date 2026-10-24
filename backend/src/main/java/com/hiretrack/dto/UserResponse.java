package com.hiretrack.dto;

import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;

import java.time.Instant;

// User info for the admin list. Note: no password field!
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
