package ru.ssau.codecleaner.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_sessions")
public class AnalysisSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    @Enumerated(EnumType.STRING)
    private AnalysisMethod analysisMethod;

    private String commitHash;
    private Double healthScore;

    // Метрики качества анализа
    private Double precision;        // TP / (TP + FP)
    private Double recall;           // TP / (TP + FN)
    private Double f1Score;          // 2 * (precision * recall) / (precision + recall)
    private Double falsePositiveRate; // FP / (FP + TN)
    private Long analysisTimeMs;     // Время анализа в миллисекундах

    @JsonIgnore
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileReport> fileReports = new ArrayList<>();

    // Конструкторы
    public AnalysisSession() {}

    public AnalysisSession(Project project, LocalDateTime startTime, AnalysisStatus status) {
        this.project = project;
        this.startTime = startTime;
        this.status = status;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public AnalysisStatus getStatus() { return status; }
    public void setStatus(AnalysisStatus status) { this.status = status; }

    public AnalysisMethod getAnalysisMethod() { return analysisMethod; }
    public void setAnalysisMethod(AnalysisMethod analysisMethod) { this.analysisMethod = analysisMethod; }

    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

    public Double getHealthScore() { return healthScore; }
    public void setHealthScore(Double healthScore) { this.healthScore = healthScore; }

    public List<FileReport> getFileReports() { return fileReports; }
    public void setFileReports(List<FileReport> fileReports) { this.fileReports = fileReports; }

    public Double getPrecision() { return precision; }
    public void setPrecision(Double precision) { this.precision = precision; }

    public Double getRecall() { return recall; }
    public void setRecall(Double recall) { this.recall = recall; }

    public Double getF1Score() { return f1Score; }
    public void setF1Score(Double f1Score) { this.f1Score = f1Score; }

    public Double getFalsePositiveRate() { return falsePositiveRate; }
    public void setFalsePositiveRate(Double falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }

    public Long getAnalysisTimeMs() { return analysisTimeMs; }
    public void setAnalysisTimeMs(Long analysisTimeMs) { this.analysisTimeMs = analysisTimeMs; }
}