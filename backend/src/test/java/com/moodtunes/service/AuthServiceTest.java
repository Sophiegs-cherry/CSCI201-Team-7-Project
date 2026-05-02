package com.moodtunes.service;

import com.moodtunes.dto.AuthResponse;
import com.moodtunes.dto.LoginRequest;
import com.moodtunes.dto.RegisterRequest;
import com.moodtunes.model.User;
import com.moodtunes.repository.UserRepository;
import com.moodtunes.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService unit tests — covers logic NOT exercised by AuthControllerTest:
 *   - BCrypt hashing actually produces a non-plaintext hash (Plan 1.1, CLAUDE.md constraint)
 *   - Duplicate username / email detection at the service layer
 *   - Password strength validation (regex enforcement)
 *   - Login by email path (controller tests only used username)
 *   - Wrong password produces auth failure
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks private AuthService authService;

    @BeforeEach
    void wirePasswordEncoder() {
        // PasswordEncoder is the real BCrypt one so we can assert the hash is real.
        org.springframework.test.util.ReflectionTestUtils.setField(
                authService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void register_storesBCryptHashNotPlaintext() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@test.com");
        req.setPassword("Password1!");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(42);
            return u;
        });
        when(tokenProvider.generateToken("alice")).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        // Capture the saved user
        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertNotEquals("Password1!", saved.getPasswordHash(),
                "Password must NOT be stored as plaintext");
        assertTrue(saved.getPasswordHash().startsWith("$2"),
                "Password hash must look like BCrypt ($2a/$2b prefix)");
        assertTrue(passwordEncoder.matches("Password1!", saved.getPasswordHash()),
                "BCrypt hash must verify against the original password");
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void register_rejectsWeakPassword_noUpperCase() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setEmail("bob@test.com");
        req.setPassword("password1!");  // missing uppercase

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(req));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_rejectsWeakPassword_noSpecialChar() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setEmail("bob@test.com");
        req.setPassword("Password123");  // missing special char

        assertThrows(RuntimeException.class, () -> authService.register(req));
    }

    @Test
    void register_rejectsShortPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setEmail("bob@test.com");
        req.setPassword("Aa1!");  // < 8 chars

        assertThrows(RuntimeException.class, () -> authService.register(req));
    }

    @Test
    void register_throwsOnDuplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setEmail("new@test.com");
        req.setPassword("Password1!");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(req));
        assertEquals("Username already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsOnDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("taken@test.com");
        req.setPassword("Password1!");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(req));
        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_succeedsWithUsername() {
        LoginRequest req = new LoginRequest("alice", "Password1!");

        User user = new User();
        user.setUserId(1);
        user.setUsername("alice");
        user.setEmail("alice@test.com");
        user.setPasswordHash(passwordEncoder.encode("Password1!"));

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "Password1!");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken("alice")).thenReturn("jwt");

        AuthResponse response = authService.login(req);

        assertEquals("jwt", response.getToken());
        assertEquals("alice", response.getUsername());
    }

    @Test
    void login_succeedsWithEmail() {
        LoginRequest req = new LoginRequest("alice@test.com", "Password1!");

        User user = new User();
        user.setUserId(1);
        user.setUsername("alice");
        user.setEmail("alice@test.com");

        when(userRepository.findByUsername("alice@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("alice", "Password1!"));
        when(tokenProvider.generateToken("alice")).thenReturn("jwt");

        AuthResponse response = authService.login(req);

        assertEquals("alice", response.getUsername());
    }

    @Test
    void login_throwsForUnknownUserOrEmail() {
        LoginRequest req = new LoginRequest("ghost", "Password1!");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(req));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void login_propagatesBadCredentialsFromAuthManager() {
        LoginRequest req = new LoginRequest("alice", "WrongPassword!1");

        User user = new User();
        user.setUserId(1);
        user.setUsername("alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }
}
