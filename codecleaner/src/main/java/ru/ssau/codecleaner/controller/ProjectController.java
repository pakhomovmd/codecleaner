package ru.ssau.codecleaner.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ssau.codecleaner.dto.ProjectDto;
import ru.ssau.codecleaner.dto.ProjectRequest;
import ru.ssau.codecleaner.service.ProjectService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        List<ProjectDto> projects = projectService.getProjectsByUserEmail(email);
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectRequest request) {
        System.out.println("📥 Received project creation request:");
        System.out.println("  Name: " + request.getName());
        System.out.println("  RepoUrl: " + request.getRepoUrl());
        System.out.println("  Description: " + request.getDescription());
        System.out.println("  UserId: " + request.getUserId());
        
        ProjectDto project = projectService.createProject(request);
        System.out.println("✅ Project created successfully with ID: " + project.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().body(Map.of("message", "Project deleted successfully"));
    }
}
