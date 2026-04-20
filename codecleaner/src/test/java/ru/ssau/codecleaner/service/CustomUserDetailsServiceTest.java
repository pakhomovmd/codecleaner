package ru.ssau.codecleaner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.codecleaner.dto.SignUpRequest;
import ru.ssau.codecleaner.dto.UserDto;
import ru.ssau.codecleaner.entity.Role;
import ru.ssau.codecleaner.entity.User;
import ru.ssau.codecleaner.exception.EmailAlreadyExistsException;
import ru.ssau.codecleaner.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testLoadUserByUsername() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setRole(Role.VIEWER);
        userRepository.save(user);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent@example.com");
        });
    }

    @Test
    void testRegisterUser() {
        // Arrange
        SignUpRequest request = new SignUpRequest("new@example.com", "password123", "New User");

        // Act
        UserDto userDto = userDetailsService.registerUser(request);

        // Assert
        assertNotNull(userDto);
        assertNotNull(userDto.getId());
        assertEquals("new@example.com", userDto.getEmail());
        assertEquals("New User", userDto.getFullName());
        assertEquals("VIEWER", userDto.getRole());
    }

    @Test
    void testRegisterUserEmailAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("password");
        existingUser.setFullName("Existing User");
        existingUser.setRole(Role.VIEWER);
        userRepository.save(existingUser);

        SignUpRequest request = new SignUpRequest("existing@example.com", "newpassword", "Another User");

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userDetailsService.registerUser(request);
        });
    }

    @Test
    void testGetUserByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        // Act
        UserDto userDto = userDetailsService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(userDto);
        assertEquals("test@example.com", userDto.getEmail());
        assertEquals("Test User", userDto.getFullName());
        assertEquals("ADMIN", userDto.getRole());
    }

    @Test
    void testGetUserByEmailNotFound() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.getUserByEmail("notfound@example.com");
        });
    }
}
