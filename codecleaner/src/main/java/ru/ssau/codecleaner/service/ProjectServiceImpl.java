package ru.ssau.codecleaner.service;

import org.springframework.stereotype.Service;
import ru.ssau.codecleaner.dto.ProjectDto;
import ru.ssau.codecleaner.dto.ProjectRequest;
import ru.ssau.codecleaner.entity.Project;
import ru.ssau.codecleaner.entity.User;
import ru.ssau.codecleaner.exception.ProjectNotFoundException;
import ru.ssau.codecleaner.exception.UserNotFoundException;
import ru.ssau.codecleaner.repository.ProjectRepository;
import ru.ssau.codecleaner.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ProjectDto createProject(ProjectRequest request) {
        User owner = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        Project project = new Project();
        project.setName(request.getName());
        project.setRepoUrl(request.getRepoUrl());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());
        project.setIsArchived(false);

        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }

    @Override
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDto> getProjectsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        
        return projectRepository.findByOwnerId(user.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return convertToDto(project);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        projectRepository.delete(project);
        System.out.println("🗑️ Project deleted: " + project.getName() + " (ID: " + id + ")");
    }

    private ProjectDto convertToDto(Project project) {
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
