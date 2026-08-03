package com.stockpro.auth;

import com.stockpro.auth.dto.*;
import com.stockpro.auth.entity.Role;
import com.stockpro.auth.entity.User;
import com.stockpro.auth.repository.UserRepository;
import com.stockpro.auth.security.JwtUtil;
import com.stockpro.auth.service.AuthServiceImpl;
import com.stockpro.auth.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .fullName("Alice Admin")
                .email("alice@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .phone("+91-9000000001")
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: saves user and returns response")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Alice Admin");
        req.setEmail("alice@example.com");
        req.setPassword("password123");
        req.setPhone("+91-9000000001");
        req.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        UserResponse response = authService.register(req);

        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getFullName()).isEqualTo("Alice Admin");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: throws RuntimeException when email already registered")
    void register_duplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("alice@example.com");

        verify(userRepository, never()).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: returns token on valid credentials")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "alice@example.com", "ADMIN")).thenReturn("jwt-token-abc");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token-abc");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("login: throws RuntimeException when email not found")
    void login_emailNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login: throws RuntimeException when password is wrong")
    void login_wrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("wrongpass");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login: throws RuntimeException when account is deactivated")
    void login_deactivatedAccount() {
        activeUser.setActive(false);
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("deactivated");
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken: returns true for valid, non-blacklisted token")
    void validateToken_valid() {
        Claims claims = mock(Claims.class);
        when(tokenBlacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractAllClaims("valid-token")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(tokenBlacklistService.isUserBlocked(1L)).thenReturn(false);

        assertThat(authService.validateToken("valid-token")).isTrue();
    }

    @Test
    @DisplayName("validateToken: returns false when token is blacklisted")
    void validateToken_blacklisted() {
        when(tokenBlacklistService.isBlacklisted("blacklisted-token")).thenReturn(true);

        assertThat(authService.validateToken("blacklisted-token")).isFalse();
    }

    @Test
    @DisplayName("validateToken: returns false when token is expired/invalid")
    void validateToken_invalidToken() {
        when(tokenBlacklistService.isBlacklisted("expired-token")).thenReturn(false);
        when(jwtUtil.isTokenValid("expired-token")).thenReturn(false);

        assertThat(authService.validateToken("expired-token")).isFalse();
    }

    @Test
    @DisplayName("validateToken: returns false when user is blocked")
    void validateToken_userBlocked() {
        Claims claims = mock(Claims.class);
        when(tokenBlacklistService.isBlacklisted("token")).thenReturn(false);
        when(jwtUtil.isTokenValid("token")).thenReturn(true);
        when(jwtUtil.extractAllClaims("token")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(tokenBlacklistService.isUserBlocked(1L)).thenReturn(true);

        assertThat(authService.validateToken("token")).isFalse();
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById: returns user response when found")
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        UserResponse response = authService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("getUserById: throws RuntimeException when not found")
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers: returns list of all users")
    void getAllUsers_returnsList() {
        User u2 = User.builder().id(2L).fullName("Bob Manager").email("bob@example.com")
                .passwordHash("hash").role(Role.MANAGER).isActive(true)
                .createdAt(LocalDateTime.now()).build();
        when(userRepository.findAll()).thenReturn(List.of(activeUser, u2));

        List<UserResponse> result = authService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponse::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    // ── deactivateUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deactivateUser: sets active=false and blocks token")
    void deactivateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        authService.deactivateUser(1L);

        assertThat(activeUser.isActive()).isFalse();
        verify(userRepository).save(activeUser);
        verify(tokenBlacklistService).blockUser(1L);
    }

    @Test
    @DisplayName("deactivateUser: throws RuntimeException when user not found")
    void deactivateUser_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.deactivateUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ── reactivateUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("reactivateUser: sets active=true and unblocks user")
    void reactivateUser_success() {
        activeUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        authService.reactivateUser(1L);

        assertThat(activeUser.isActive()).isTrue();
        verify(userRepository).save(activeUser);
        verify(tokenBlacklistService).unblockUser(1L);
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout: blacklists token with remaining TTL")
    void logout_success() {
        when(jwtUtil.getRemainingMillis("token")).thenReturn(3600000L);

        authService.logout("token");

        verify(tokenBlacklistService).blacklist("token", 3600000L);
    }
}
