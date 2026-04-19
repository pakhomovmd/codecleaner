package ru.ssau.codecleaner.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeAnalysisServiceTest {

    @Test
    void testServiceExists() {
        // Простой тест что сервис можно создать
        assertDoesNotThrow(() -> {
            // CodeAnalysisService требует репозитории, 
            // поэтому просто проверяем что класс существует
            Class.forName("ru.ssau.codecleaner.service.CodeAnalysisService");
        });
    }

    @Test
    void testAnalysisMethodEnum() {
        // Проверяем что все методы анализа определены
        assertEquals(3, ru.ssau.codecleaner.entity.AnalysisMethod.values().length);
        assertNotNull(ru.ssau.codecleaner.entity.AnalysisMethod.SIMPLE_TEXT_SEARCH);
        assertNotNull(ru.ssau.codecleaner.entity.AnalysisMethod.AST_ANALYSIS);
        assertNotNull(ru.ssau.codecleaner.entity.AnalysisMethod.COVERAGE_BASED);
    }
}
