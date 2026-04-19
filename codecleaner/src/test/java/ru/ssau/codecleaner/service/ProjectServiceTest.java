package ru.ssau.codecleaner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.codecleaner.dto.ProjectDto;
import ru.ssau.codecleaner.entity.Project;
import ru.ssau.codecleaner.entity.User;
import ru.ssau.codecleaner.entity.Role;
import ru.ssau.codecleaner.repository.ProjectRepository;
import ru.ssau.codecleaner.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем БД перед каждым тестом
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Создаём тестового пользователя
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setRole(Role.VIEWER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateProject() {
        // Arrange
        String projectName = "Test Project";
        String repoUrl = "https://github.com/test/repo";
        String description = "Test description";
        
        ru.ssau.codecleaner.dto.ProjectRequest request = new ru.ssau.codecleaner.dto.ProjectRequest(
            projectName, repoUrl, description, testUser.getId()
        );

        // Act
        ProjectDto result = projectService.createProject(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(projectName, result.getName());
        assertEquals(repoUrl, result.getRepoUrl());
        assertEquals(description, result.getDescription());
        assertEquals(testUser.getId(), result.getOwnerId());
        assertEquals(testUser.getEmail(), result.getOwnerEmail());
    }

    @Test
    void testGetProjectsByUserEmail() {
        // Arrange
        Project project1 = new Project("Project 1", "https://github.com/test/repo1", "Description 1", testUser);
        Project project2 = new Project("Project 2", "https://github.com/test/repo2", "Description 2", testUser);
        projectRepository.save(project1);
        projectRepository.save(project2);

        // Act
        List<ProjectDto> projects = projectService.getProjectsByUserEmail(testUser.getEmail());

        // Assert
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("Project 1")));
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("Project 2")));
    }

    @Test
    void testGetProjectById() {
        // Arrange
        Project project = new Project("Test Project", "https://github.com/test/repo", "Description", testUser);
        project = projectRepository.save(project);

        // Act
        ProjectDto result = projectService.getProjectById(project.getId());

        // Assert
        assertNotNull(result);
        assertEquals(project.getId(), result.getId());
        assertEquals(project.getName(), result.getName());
        assertEquals(project.getRepoUrl(), result.getRepoUrl());
    }

    @Test
    void testDeleteProject() {
        // Arrange
        Project project = new Project("Test Project", "https://github.com/test/repo", "Description", testUser);
        project = projectRepository.save(project);
        Long projectId = project.getId();

        // Act
        projectService.deleteProject(projectId);

        // Assert
        assertFalse(projectRepository.findById(projectId).isPresent());
    }

    @Test
    void testGetAllProjects() {
        // Arrange
        Project project1 = new Project("Project 1", "https://github.com/test/repo1", "Description 1", testUser);
        Project project2 = new Project("Project 2", "https://github.com/test/repo2", "Description 2", testUser);
        projectRepository.save(project1);
        projectRepository.save(project2);

        // Act
        List<ProjectDto> projects = projectService.getAllProjects();

        // Assert
        assertNotNull(projects);
        assertTrue(projects.size() >= 2);
    }
}
