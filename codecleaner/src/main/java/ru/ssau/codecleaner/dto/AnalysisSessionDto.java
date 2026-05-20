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
    private Double precision;
    private Double recall;
    private Double f1Score;
    private Double falsePositiveRate;
    private Long analysisTimeMs;

    // Конструкторы
    public AnalysisSessionDto() {}

    public AnalysisSessionDto(Long id, Long projectId, String projectName, String startTime, 
                              String endTime, String status, String commitHash, Double healthScore, 
                              String analysisMethod, Double precision, Double recall, Double f1Score, 
                              Double falsePositiveRate, Long analysisTimeMs) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.commitHash = commitHash;
        this.healthScore = healthScore;
        this.analysisMethod = analysisMethod;
        this.precision = precision;
        this.recall = recall;
        this.f1Score = f1Score;
        this.falsePositiveRate = falsePositiveRate;
        this.analysisTimeMs = analysisTimeMs;
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

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }

    public Double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public void setFalsePositiveRate(Double falsePositiveRate) {
        this.falsePositiveRate = falsePositiveRate;
    }

    public Long getAnalysisTimeMs() {
        return analysisTimeMs;
    }

    public void setAnalysisTimeMs(Long analysisTimeMs) {
        this.analysisTimeMs = analysisTimeMs;
    }
}
