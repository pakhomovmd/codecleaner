package ru.ssau.codecleaner.exception;

public class AnalysisSessionNotFoundException extends RuntimeException {
    public AnalysisSessionNotFoundException(Long id) {
        super("Analysis session not found with id: " + id);
    }
}
