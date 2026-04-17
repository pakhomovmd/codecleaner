package ru.ssau.codecleaner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProjectDto {
    private Long id;
    
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Repository URL is required")
    @Pattern(regexp = "https?://.*", message = "Repository URL must be a valid HTTP/HTTPS URL")
    private String repoUrl;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private String createdAt;
    private Boolean isArchived;
    private Long ownerId;
    private String ownerEmail;

    // Конструкторы
    public ProjectDto() {}

    public ProjectDto(Long id, String name, String repoUrl, String description, 
                      String createdAt, Boolean isArchived, Long ownerId, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.repoUrl = repoUrl;
        this.description = description;
        this.createdAt = createdAt;
        this.isArchived = isArchived;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
