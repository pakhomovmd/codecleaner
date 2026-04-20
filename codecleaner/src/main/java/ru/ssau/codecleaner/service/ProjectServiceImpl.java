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
    private final GitHubService gitHubService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository,
                              GitHubService gitHubService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.gitHubService = gitHubService;
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

        // Если указан repoUrl, клонируем репозиторий
        if (request.getRepoUrl() != null && !request.getRepoUrl().trim().isEmpty()) {
            try {
                System.out.println("🔄 Cloning repository from: " + request.getRepoUrl());
                var zipFile = gitHubService.cloneAndZipRepository(request.getRepoUrl());
                
                // Сохраняем ZIP в постоянное хранилище
                String zipPath = saveClonedZip(zipFile, project.getName());
                project.setClonedZipPath(zipPath);
                
                System.out.println("✅ Repository cloned and saved to: " + zipPath);
            } catch (Exception e) {
                System.err.println("❌ Failed to clone repository: " + e.getMessage());
                throw new RuntimeException("Failed to clone repository: " + e.getMessage(), e);
            }
        }

        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }
    
    /**
     * Сохраняет клонированный ZIP в постоянное хранилище
     */
    private String saveClonedZip(org.springframework.web.multipart.MultipartFile zipFile, String projectName) throws java.io.IOException {
        // Создаем директорию для хранения клонированных репозиториев
        java.nio.file.Path storageDir = java.nio.file.Paths.get("cloned-repos");
        if (!java.nio.file.Files.exists(storageDir)) {
            java.nio.file.Files.createDirectories(storageDir);
        }
        
        // Генерируем уникальное имя файла
        String fileName = projectName.replaceAll("[^a-zA-Z0-9-_]", "_") + "_" + System.currentTimeMillis() + ".zip";
        java.nio.file.Path filePath = storageDir.resolve(fileName);
        
        // Сохраняем файл
        zipFile.transferTo(filePath.toFile());
        
        return filePath.toString();
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
                project.getOwner().getEmail(),
                project.getClonedZipPath()
        );
    }
}
