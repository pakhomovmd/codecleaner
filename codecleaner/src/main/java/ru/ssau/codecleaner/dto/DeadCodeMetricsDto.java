package ru.ssau.codecleaner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeadCodeMetricsDto {
    
    @JsonProperty("projectName")
    private String projectName;
    
    @JsonProperty("projectType")
    private String projectType;
    
    @JsonProperty("deadCodePercentage")
    private String deadCodePercentage;
    
    @JsonProperty("totalFiles")
    private Integer totalFiles;
    
    @JsonProperty("deadCodeLocations")
    private List<DeadCodeLocation> deadCodeLocations;
    
    @JsonProperty("statistics")
    private Statistics statistics;
    
    @JsonProperty("notes")
    private String notes;
    
    // Геттеры и сеттеры
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    
    public String getDeadCodePercentage() { return deadCodePercentage; }
    public void setDeadCodePercentage(String deadCodePercentage) { this.deadCodePercentage = deadCodePercentage; }
    
    public Integer getTotalFiles() { return totalFiles; }
    public void setTotalFiles(Integer totalFiles) { this.totalFiles = totalFiles; }
    
    public List<DeadCodeLocation> getDeadCodeLocations() { return deadCodeLocations; }
    public void setDeadCodeLocations(List<DeadCodeLocation> deadCodeLocations) { this.deadCodeLocations = deadCodeLocations; }
    
    public Statistics getStatistics() { return statistics; }
    public void setStatistics(Statistics statistics) { this.statistics = statistics; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeadCodeLocation {
        @JsonProperty("file")
        private String file;
        
        @JsonProperty("lines")
        private String lines;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("description")
        private String description;
        
        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
        
        public String getLines() { return lines; }
        public void setLines(String lines) { this.lines = lines; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Statistics {
        @JsonProperty("totalLinesOfCode")
        private Integer totalLinesOfCode;
        
        @JsonProperty("deadCodeLines")
        private Integer deadCodeLines;
        
        @JsonProperty("deadCodePercentageActual")
        private Double deadCodePercentageActual;
        
        @JsonProperty("htmlFiles")
        private Integer htmlFiles;
        
        @JsonProperty("cssFiles")
        private Integer cssFiles;
        
        @JsonProperty("jsFiles")
        private Integer jsFiles;
        
        public Integer getTotalLinesOfCode() { return totalLinesOfCode; }
        public void setTotalLinesOfCode(Integer totalLinesOfCode) { this.totalLinesOfCode = totalLinesOfCode; }
        
        public Integer getDeadCodeLines() { return deadCodeLines; }
        public void setDeadCodeLines(Integer deadCodeLines) { this.deadCodeLines = deadCodeLines; }
        
        public Double getDeadCodePercentageActual() { return deadCodePercentageActual; }
        public void setDeadCodePercentageActual(Double deadCodePercentageActual) { this.deadCodePercentageActual = deadCodePercentageActual; }
        
        public Integer getHtmlFiles() { return htmlFiles; }
        public void setHtmlFiles(Integer htmlFiles) { this.htmlFiles = htmlFiles; }
        
        public Integer getCssFiles() { return cssFiles; }
        public void setCssFiles(Integer cssFiles) { this.cssFiles = cssFiles; }
        
        public Integer getJsFiles() { return jsFiles; }
        public void setJsFiles(Integer jsFiles) { this.jsFiles = jsFiles; }
    }
}
