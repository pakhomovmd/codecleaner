package ru.ssau.codecleaner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Repository URL is required")
    @Pattern(regexp = "https?://.*", message = "Repository URL must be a valid HTTP/HTTPS URL")
    private String repoUrl;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "User ID is required")
    private Long userId;

    // Конструкторы
    public ProjectRequest() {}

    public ProjectRequest(String name, String repoUrl, String description, Long userId) {
        this.name = name;
        this.repoUrl = repoUrl;
        this.description = description;
        this.userId = userId;
    }

    // Геттеры и сеттеры
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

    public Long getUserId() { 
        return userId; 
    }
    
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }
}
