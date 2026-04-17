package ru.ssau.codecleaner.service;

import ru.ssau.codecleaner.dto.ProjectDto;
import ru.ssau.codecleaner.dto.ProjectRequest;

import java.util.List;

public interface ProjectService {
    ProjectDto createProject(ProjectRequest request);
    List<ProjectDto> getAllProjects();
    List<ProjectDto> getProjectsByUserEmail(String email);
    ProjectDto getProjectById(Long id);
    void deleteProject(Long id);
}
