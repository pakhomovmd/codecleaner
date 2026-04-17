package ru.ssau.codecleaner.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ssau.codecleaner.dto.AuthRequest;
import ru.ssau.codecleaner.dto.AuthResponse;
import ru.ssau.codecleaner.dto.SignUpRequest;
import ru.ssau.codecleaner.dto.UserDto;
import ru.ssau.codecleaner.entity.User;
import ru.ssau.codecleaner.repository.UserRepository;
import ru.ssau.codecleaner.service.CustomUserDetailsService;
import ru.ssau.codecleaner.service.TokenService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                         TokenService tokenService,
                         UserRepository userRepository,
                         CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signupRequest) {
        UserDto userDto = userDetailsService.registerUser(signupRequest);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!", "email", userDto.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .collect(Collectors.toList());

        String accessToken = tokenService.createAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = tokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            var payload = tokenService.verifyToken(refreshToken);
            Long userId = ((Number) payload.get("userId")).longValue();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<String> roles = List.of("ROLE_" + user.getRole().name());

            String newAccessToken = tokenService.createAccessToken(userId, user.getEmail(), roles);

            return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));

        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        UserDto userDto = userDetailsService.getUserByEmail(email);

        return ResponseEntity.ok(userDto);
    }
}
