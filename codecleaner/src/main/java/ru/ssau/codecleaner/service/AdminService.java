package ru.ssau.codecleaner.service;

import org.springframework.stereotype.Service;
import ru.ssau.codecleaner.dto.ProjectDto;
import ru.ssau.codecleaner.dto.UserDto;
import ru.ssau.codecleaner.entity.Project;
import ru.ssau.codecleaner.entity.User;
import ru.ssau.codecleaner.exception.UserNotFoundException;
import ru.ssau.codecleaner.repository.AnalysisSessionRepository;
import ru.ssau.codecleaner.repository.ProjectRepository;
import ru.ssau.codecleaner.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AnalysisSessionRepository analysisSessionRepository;

    public AdminService(UserRepository userRepository,
                       ProjectRepository projectRepository,
                       AnalysisSessionRepository analysisSessionRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.analysisSessionRepository = analysisSessionRepository;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    public List<ProjectDto> getUserProjects(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return projectRepository.findByOwnerId(userId).stream()
                .map(this::convertProjectToDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(user);
        System.out.println("🗑️ User deleted by admin: " + user.getEmail() + " (ID: " + userId + ")");
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProjects", projectRepository.count());
        stats.put("totalAnalyses", analysisSessionRepository.count());
        return stats;
    }

    private UserDto convertUserToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    private ProjectDto convertProjectToDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getRepoUrl(),
                project.getDescription(),
                project.getCreatedAt() != null ? project.getCreatedAt().toString() : null,
                project.getIsArchived(),
                project.getOwner().getId(),
                project.getOwner().getEmail()
        );
    }
}
