package com.hiretrack.service;

import com.hiretrack.dto.AuthResponse;
import com.hiretrack.dto.LoginRequest;
import com.hiretrack.dto.RegisterRequest;
import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;
import com.hiretrack.exception.EmailAlreadyExistsException;
import com.hiretrack.repository.UserRepository;
import com.hiretrack.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        // New accounts are always regular users; admins are only created by the seeder
        User user = new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), Role.ROLE_USER);
        userRepository.save(user);

        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());

        // Checks the password against the BCrypt hash. Throws BadCredentialsException if wrong.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole());
    }

    // "Alice@Mail.com " and "alice@mail.com" should be the same account
    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
