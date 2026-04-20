package ru.ssau.codecleaner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.codecleaner.dto.AnalysisSessionDto;
import ru.ssau.codecleaner.entity.*;
import ru.ssau.codecleaner.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AnalysisSessionRepository analysisSessionRepository;

    @Autowired
    private FileReportRepository fileReportRepository;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        fileReportRepository.deleteAll();
        analysisSessionRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setRole(Role.VIEWER);
        testUser = userRepository.save(testUser);

        testProject = new Project("Test Project", "https://github.com/test/repo", "Description", testUser);
        testProject = projectRepository.save(testProject);
    }

    @Test
    void testGetAnalysesByProject() {
        // Arrange
        AnalysisSession session = new AnalysisSession();
        session.setProject(testProject);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now());
        session.setStatus(AnalysisStatus.COMPLETED);
        session.setAnalysisMethod(AnalysisMethod.SIMPLE_TEXT_SEARCH);
        session.setHealthScore(85.0);
        analysisSessionRepository.save(session);

        // Act
        List<AnalysisSessionDto> analyses = analysisService.getAnalysesByProject(testProject.getId());

        // Assert
        assertNotNull(analyses);
        assertEquals(1, analyses.size());
        assertEquals(testProject.getId(), analyses.get(0).getProjectId());
        assertEquals(85.0, analyses.get(0).getHealthScore());
    }

    @Test
    void testDeleteAnalysis() {
        // Arrange
        AnalysisSession session = new AnalysisSession();
        session.setProject(testProject);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(AnalysisStatus.COMPLETED);
        session.setAnalysisMethod(AnalysisMethod.AST_ANALYSIS);
        session = analysisSessionRepository.save(session);
        Long sessionId = session.getId();

        // Act
        analysisService.deleteAnalysis(sessionId);

        // Assert
        assertFalse(analysisSessionRepository.findById(sessionId).isPresent());
    }

    @Test
    void testDeleteAllAnalysesByProject() {
        // Arrange
        AnalysisSession session1 = new AnalysisSession();
        session1.setProject(testProject);
        session1.setStartTime(LocalDateTime.now());
        session1.setStatus(AnalysisStatus.COMPLETED);
        session1.setAnalysisMethod(AnalysisMethod.SIMPLE_TEXT_SEARCH);
        analysisSessionRepository.save(session1);

        AnalysisSession session2 = new AnalysisSession();
        session2.setProject(testProject);
        session2.setStartTime(LocalDateTime.now());
        session2.setStatus(AnalysisStatus.COMPLETED);
        session2.setAnalysisMethod(AnalysisMethod.COVERAGE_BASED);
        analysisSessionRepository.save(session2);

        // Act
        analysisService.deleteAllAnalysesByProject(testProject.getId());

        // Assert
        List<AnalysisSession> remaining = analysisSessionRepository.findByProjectId(testProject.getId());
        assertEquals(0, remaining.size());
    }

    @Test
    void testGetAnalysisResult() {
        // Arrange
        AnalysisSession session = new AnalysisSession();
        session.setProject(testProject);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now());
        session.setStatus(AnalysisStatus.COMPLETED);
        session.setAnalysisMethod(AnalysisMethod.SIMPLE_TEXT_SEARCH);
        session.setHealthScore(90.0);
        session = analysisSessionRepository.save(session);

        FileReport fileReport = new FileReport();
        fileReport.setAnalysis(session);
        fileReport.setFilePath("test.css");
        fileReport.setFileType(FileType.CSS);
        fileReport.setTotalSizeBytes(1000L);
        fileReport.setUnusedSizeBytes(100L);
        fileReport.setUnusedPercentage(10.0);
        fileReportRepository.save(fileReport);

        // Act
        Map<String, Object> result = analysisService.getAnalysisResult(session.getId());

        // Assert
        assertNotNull(result);
        assertEquals(session.getId(), result.get("id"));
        assertEquals(90.0, result.get("healthScore"));
        assertNotNull(result.get("fileReports"));
    }
}
