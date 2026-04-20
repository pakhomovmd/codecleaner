package ru.ssau.codecleaner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.codecleaner.dto.ProjectDto;
import ru.ssau.codecleaner.dto.UserDto;
import ru.ssau.codecleaner.entity.Project;
import ru.ssau.codecleaner.entity.Role;
import ru.ssau.codecleaner.entity.User;
import ru.ssau.codecleaner.repository.ProjectRepository;
import ru.ssau.codecleaner.repository.UserRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("user@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setRole(Role.VIEWER);
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFullName("Admin User");
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    void testGetAllUsers() {
        // Act
        List<UserDto> users = adminService.getAllUsers();

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("admin@example.com")));
    }

    @Test
    void testGetUserProjects() {
        // Arrange
        Project project = new Project("Test Project", "https://github.com/test/repo", "Description", testUser);
        projectRepository.save(project);

        // Act
        List<ProjectDto> projects = adminService.getUserProjects(testUser.getId());

        // Assert
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals("Test Project", projects.get(0).getName());
    }

    @Test
    void testDeleteUser() {
        // Arrange
        Long userId = testUser.getId();

        // Act
        adminService.deleteUser(userId);

        // Assert
        assertFalse(userRepository.findById(userId).isPresent());
    }

    @Test
    void testGetStats() {
        // Arrange
        Project project = new Project("Test Project", "https://github.com/test/repo", "Description", testUser);
        projectRepository.save(project);

        // Act
        Map<String, Object> stats = adminService.getStats();

        // Assert
        assertNotNull(stats);
        assertEquals(2L, ((Number) stats.get("totalUsers")).longValue());
        assertEquals(1L, ((Number) stats.get("totalProjects")).longValue());
        assertEquals(0L, ((Number) stats.get("totalAnalyses")).longValue());
    }

    @Test
    void testDeleteUserCascadesProjects() {
        // Arrange
        Project project = new Project("Test Project", "https://github.com/test/repo", "Description", testUser);
        projectRepository.save(project);
        Long userId = testUser.getId();

        // Act
        adminService.deleteUser(userId);

        // Assert
        assertFalse(userRepository.findById(userId).isPresent());
        assertEquals(0, projectRepository.findByOwnerId(userId).size());
    }
}
