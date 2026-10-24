package com.hiretrack.service;

import com.hiretrack.dto.AuthResponse;
import com.hiretrack.dto.LoginRequest;
import com.hiretrack.dto.RegisterRequest;
import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;
import com.hiretrack.exception.EmailAlreadyExistsException;
import com.hiretrack.repository.UserRepository;
import com.hiretrack.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_hashesPassword_assignsUserRole_andReturnsToken() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(jwtService.generateToken("alice@test.com", "ROLE_USER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(
                new RegisterRequest("Alice", "  Alice@Test.com ", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("alice@test.com");  // trimmed + lowercased
        assertThat(saved.getPassword()).isEqualTo("bcrypt-hash");  // never the plain password
        assertThat(saved.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Alice", "alice@test.com", "password123")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        User alice = new User("Alice", "alice@test.com", "bcrypt-hash", Role.ROLE_USER);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));
        when(jwtService.generateToken("alice@test.com", "ROLE_USER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("alice@test.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_withWrongPassword_throws_andDoesNotIssueToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@test.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any());
    }
}
