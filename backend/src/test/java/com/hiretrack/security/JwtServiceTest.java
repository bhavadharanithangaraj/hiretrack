package com.hiretrack.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Plain JUnit test: JwtService has no dependencies, so we just create it with "new".
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-chars!";
    private static final long ONE_HOUR = 3_600_000;

    @Test
    void generatedToken_containsTheEmail() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

        String token = jwtService.generateToken("alice@test.com", "ROLE_USER");

        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@test.com");
    }

    @Test
    void tokenSignedWithAnotherSecret_isRejected() {
        JwtService attacker = new JwtService("a-completely-different-secret-of-32+chars", ONE_HOUR);
        String forgedToken = attacker.generateToken("admin@hiretrack.com", "ROLE_ADMIN");

        JwtService server = new JwtService(SECRET, ONE_HOUR);

        assertThat(server.extractEmail(forgedToken)).isNull();
    }

    @Test
    void expiredToken_isRejected() {
        // A negative expiration creates a token that expired one second ago
        JwtService jwtService = new JwtService(SECRET, -1000);

        String token = jwtService.generateToken("alice@test.com", "ROLE_USER");

        assertThat(jwtService.extractEmail(token)).isNull();
    }

    @Test
    void garbageToken_isRejected() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

        assertThat(jwtService.extractEmail("not-a-jwt")).isNull();
    }
}
