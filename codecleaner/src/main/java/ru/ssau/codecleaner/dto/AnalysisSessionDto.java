package ru.ssau.codecleaner.dto;

public class AnalysisSessionDto {
    private Long id;
    private Long projectId;
    private String projectName;
    private String startTime;
    private String endTime;
    private String status;
    private String commitHash;
    private Double healthScore;
    private String analysisMethod;

    // Конструкторы
    public AnalysisSessionDto() {}

    public AnalysisSessionDto(Long id, Long projectId, String projectName, String startTime, 
                              String endTime, String status, String commitHash, Double healthScore, String analysisMethod) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.commitHash = commitHash;
        this.healthScore = healthScore;
        this.analysisMethod = analysisMethod;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public Double getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Double healthScore) {
        this.healthScore = healthScore;
    }

    public String getAnalysisMethod() {
        return analysisMethod;
    }

    public void setAnalysisMethod(String analysisMethod) {
        this.analysisMethod = analysisMethod;
    }
}
