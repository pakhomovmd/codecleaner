package ru.ssau.codecleaner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ssau.codecleaner.dto.AnalysisSessionDto;
import ru.ssau.codecleaner.entity.AnalysisMethod;
import ru.ssau.codecleaner.service.AnalysisService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/methods")
    public ResponseEntity<List<Map<String, String>>> getAnalysisMethods() {
        List<Map<String, String>> methods = Arrays.stream(AnalysisMethod.values())
            .map(method -> {
                Map<String, String> methodInfo = new HashMap<>();
                methodInfo.put("name", method.name());
                methodInfo.put("displayName", method.getDisplayName());
                methodInfo.put("description", method.getDescription());
                return methodInfo;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/upload/{projectId}")
    public ResponseEntity<?> uploadAndAnalyze(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "method", defaultValue = "SIMPLE_TEXT_SEARCH") String methodName) {
        
        try {
            AnalysisMethod method;
            try {
                method = AnalysisMethod.valueOf(methodName);
            } catch (IllegalArgumentException e) {
                method = AnalysisMethod.SIMPLE_TEXT_SEARCH;
            }
            
            Map<String, Object> response = analysisService.uploadAndAnalyze(projectId, file, method);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getAnalysisResult(@PathVariable Long sessionId) {
        Map<String, Object> response = analysisService.getAnalysisResult(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AnalysisSessionDto>> getAnalysesByProject(@PathVariable Long projectId) {
        List<AnalysisSessionDto> analyses = analysisService.getAnalysesByProject(projectId);
        return ResponseEntity.ok(analyses);
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<?> deleteAnalysis(@PathVariable Long sessionId) {
        analysisService.deleteAnalysis(sessionId);
        return ResponseEntity.ok().body(Map.of("message", "Analysis deleted successfully"));
    }

    @DeleteMapping("/project/{projectId}/all")
    public ResponseEntity<?> deleteAllAnalysesByProject(@PathVariable Long projectId) {
        analysisService.deleteAllAnalysesByProject(projectId);
        return ResponseEntity.ok().body(Map.of("message", "All analyses deleted successfully"));
    }
}
