package ru.ssau.codecleaner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ssau.codecleaner.dto.AnalysisSessionDto;
import ru.ssau.codecleaner.entity.AnalysisSession;
import ru.ssau.codecleaner.entity.AnalysisStatus;
import ru.ssau.codecleaner.entity.AnalysisMethod;
import ru.ssau.codecleaner.entity.DeadCodeFragment;
import ru.ssau.codecleaner.entity.FileReport;
import ru.ssau.codecleaner.entity.Project;
import ru.ssau.codecleaner.exception.AnalysisSessionNotFoundException;
import ru.ssau.codecleaner.exception.ProjectNotFoundException;
import ru.ssau.codecleaner.repository.AnalysisSessionRepository;
import ru.ssau.codecleaner.repository.DeadCodeFragmentRepository;
import ru.ssau.codecleaner.repository.FileReportRepository;
import ru.ssau.codecleaner.repository.ProjectRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final ProjectRepository projectRepository;
    private final AnalysisSessionRepository analysisSessionRepository;
    private final CodeAnalysisService codeAnalysisService;
    private final FileReportRepository fileReportRepository;
    private final DeadCodeFragmentRepository deadCodeFragmentRepository;

    public AnalysisService(ProjectRepository projectRepository,
                          AnalysisSessionRepository analysisSessionRepository,
                          CodeAnalysisService codeAnalysisService,
                          FileReportRepository fileReportRepository,
                          DeadCodeFragmentRepository deadCodeFragmentRepository) {
        this.projectRepository = projectRepository;
        this.analysisSessionRepository = analysisSessionRepository;
        this.codeAnalysisService = codeAnalysisService;
        this.fileReportRepository = fileReportRepository;
        this.deadCodeFragmentRepository = deadCodeFragmentRepository;
    }

    public Map<String, Object> uploadAndAnalyze(Long projectId, MultipartFile file, AnalysisMethod method) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        AnalysisSession session = new AnalysisSession();
        session.setProject(project);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(AnalysisStatus.RUNNING);
        session.setAnalysisMethod(method);
        analysisSessionRepository.save(session);

        try {
            codeAnalysisService.analyzeProject(file, session, method);

            session.setEndTime(LocalDateTime.now());
            session.setStatus(AnalysisStatus.COMPLETED);
            analysisSessionRepository.save(session);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Analysis completed");
            response.put("sessionId", session.getId());
            response.put("healthScore", session.getHealthScore());
            response.put("status", session.getStatus().name());
            response.put("method", session.getAnalysisMethod().name());

            return response;
        } catch (Exception e) {
            session.setStatus(AnalysisStatus.FAILED);
            analysisSessionRepository.save(session);
            throw new RuntimeException("Analysis failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAnalysisResult(Long sessionId) {
        AnalysisSession session = analysisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(sessionId));

        List<FileReport> reports = fileReportRepository.findByAnalysisId(sessionId);

        for (FileReport report : reports) {
            List<DeadCodeFragment> fragments = deadCodeFragmentRepository.findByFileReportId(report.getId());
            report.setDeadCodeFragments(fragments);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", session.getId());
        response.put("projectId", session.getProject().getId());
        response.put("status", session.getStatus().name());
        response.put("healthScore", session.getHealthScore());
        response.put("startTime", session.getStartTime());
        response.put("endTime", session.getEndTime());
        response.put("analysisMethod", session.getAnalysisMethod() != null ? session.getAnalysisMethod().name() : "SIMPLE_TEXT_SEARCH");
        response.put("fileReports", reports);

        return response;
    }

    public List<AnalysisSessionDto> getAnalysesByProject(Long projectId) {
        List<AnalysisSession> sessions = analysisSessionRepository.findByProjectId(projectId);
        return sessions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void deleteAnalysis(Long sessionId) {
        AnalysisSession session = analysisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(sessionId));
        analysisSessionRepository.delete(session);
        System.out.println("🗑️ Analysis session deleted: ID " + sessionId);
    }

    public void deleteAllAnalysesByProject(Long projectId) {
        List<AnalysisSession> sessions = analysisSessionRepository.findByProjectId(projectId);
        analysisSessionRepository.deleteAll(sessions);
        System.out.println("🗑️ All analysis sessions deleted for project ID: " + projectId + " (Count: " + sessions.size() + ")");
    }

    private AnalysisSessionDto convertToDto(AnalysisSession session) {
        return new AnalysisSessionDto(
                session.getId(),
                session.getProject().getId(),
                session.getProject().getName(),
                session.getStartTime() != null ? session.getStartTime().toString() : null,
                session.getEndTime() != null ? session.getEndTime().toString() : null,
                session.getStatus().name(),
                session.getCommitHash(),
                session.getHealthScore(),
                session.getAnalysisMethod() != null ? session.getAnalysisMethod().name() : "SIMPLE_TEXT_SEARCH"
        );
    }
}
