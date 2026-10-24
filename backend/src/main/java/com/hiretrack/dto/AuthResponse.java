package com.hiretrack.dto;

import com.hiretrack.entity.Role;

// Returned by register and login. The frontend stores the token and sends it on later requests.
public record AuthResponse(
        String token,
        String name,
        String email,
        Role role
) {
}
