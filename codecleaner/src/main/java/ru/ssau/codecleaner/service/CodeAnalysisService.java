package ru.ssau.codecleaner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ssau.codecleaner.entity.*;
import ru.ssau.codecleaner.repository.*;
import ru.ssau.codecleaner.dto.DeadCodeMetricsDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class CodeAnalysisService {

    private final FileReportRepository fileReportRepository;
    private final DeadCodeFragmentRepository deadCodeFragmentRepository;

    public CodeAnalysisService(FileReportRepository fileReportRepository,
                               DeadCodeFragmentRepository deadCodeFragmentRepository) {
        this.fileReportRepository = fileReportRepository;
        this.deadCodeFragmentRepository = deadCodeFragmentRepository;
    }

    public AnalysisSession analyzeProject(MultipartFile zipFile, AnalysisSession session, AnalysisMethod method) throws IOException {
        // Создаём временную директорию
        Path tempDir = Files.createTempDirectory("codeanalysis_");

        try {
            // 1. Распаковываем ZIP
            unzip(zipFile, tempDir);

            // 2. Собираем все файлы
            List<Path> cssFiles = findFiles(tempDir, ".css");
            List<Path> jsFiles = findFiles(tempDir, ".js");
            List<Path> htmlFiles = findFiles(tempDir, ".html");

            // 3. Выбираем метод анализа
            switch (method) {
                case SIMPLE_TEXT_SEARCH:
                    analyzeWithSimpleTextSearch(cssFiles, jsFiles, htmlFiles, session);
                    break;
                case AST_ANALYSIS:
                    analyzeWithAstAnalysis(cssFiles, jsFiles, htmlFiles, session);
                    break;
                case COVERAGE_BASED:
                    analyzeWithCoverageBased(cssFiles, jsFiles, htmlFiles, session);
                    break;
                default:
                    analyzeWithSimpleTextSearch(cssFiles, jsFiles, htmlFiles, session);
            }

            // 4. Вычисляем общую метрику здоровья
            double totalHealth = calculateHealthScore(session);
            session.setHealthScore(totalHealth);

            // 5. Вычисляем метрики качества
            calculateAndSetMetrics(session, method, tempDir);

            return session;

        } finally {
            // Удаляем временные файлы
            deleteDirectory(tempDir);
        }
    }

    private void calculateAndSetMetrics(AnalysisSession session, AnalysisMethod method, Path tempDir) {
        // Проверяем, есть ли файл dead-code-metrics.json в проекте
        Path metricsFile = findDeadCodeMetricsFile(tempDir);
        
        System.out.println("DEBUG: Searching for dead-code-metrics.json in: " + tempDir);
        System.out.println("DEBUG: Found metrics file: " + metricsFile);
        
        if (metricsFile != null) {
            // Есть ground truth - рассчитываем реальные метрики
            try {
                System.out.println("DEBUG: Attempting to calculate real metrics...");
                calculateRealMetrics(session, metricsFile);
                System.out.println("DEBUG: Metrics calculated successfully");
            } catch (IOException e) {
                System.err.println("ERROR: Failed to read metrics file: " + e.getMessage());
                e.printStackTrace();
                // Если не удалось прочитать файл, устанавливаем null
                setMetricsToNull(session);
            }
        } else {
            System.out.println("DEBUG: No metrics file found, setting metrics to null");
            // Нет ground truth - устанавливаем null (будет показано N/A в UI)
            setMetricsToNull(session);
        }
    }
    
    private Path findDeadCodeMetricsFile(Path dir) {
        try {
            System.out.println("DEBUG: Walking directory tree from: " + dir);
            // Ищем файл dead-code-metrics.json рекурсивно
            try (var stream = Files.walk(dir)) {
                List<Path> allFiles = stream.collect(java.util.stream.Collectors.toList());
                System.out.println("DEBUG: Total files found: " + allFiles.size());
                for (Path p : allFiles) {
                    System.out.println("DEBUG: File: " + p);
                }
                
                Path result = allFiles.stream()
                    .filter(path -> path.getFileName().toString().equals("dead-code-metrics.json"))
                    .findFirst()
                    .orElse(null);
                
                if (result != null) {
                    System.out.println("DEBUG: Found metrics file at: " + result);
                } else {
                    System.out.println("DEBUG: Metrics file NOT found");
                }
                
                return result;
            }
        } catch (IOException e) {
            System.err.println("ERROR: Exception while searching for metrics file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void setMetricsToNull(AnalysisSession session) {
        session.setPrecision(null);
        session.setRecall(null);
        session.setF1Score(null);
        session.setFalsePositiveRate(null);
    }
    
    private void calculateRealMetrics(AnalysisSession session, Path metricsFile) throws IOException {
        // Читаем ground truth из JSON
        ObjectMapper mapper = new ObjectMapper();
        DeadCodeMetricsDto groundTruth = mapper.readValue(metricsFile.toFile(), DeadCodeMetricsDto.class);
        
        System.out.println("DEBUG: Ground truth loaded: " + groundTruth.getProjectName());
        System.out.println("DEBUG: Dead code locations: " + groundTruth.getDeadCodeLocations().size());
        
        // Получаем все найденные фрагменты мёртвого кода
        List<FileReport> reports = fileReportRepository.findByAnalysisId(session.getId());
        
        System.out.println("DEBUG: File reports found: " + reports.size());
        
        // Собираем все найденные строки мёртвого кода (по файлам)
        Map<String, Set<Integer>> foundDeadLines = new HashMap<>();
        for (FileReport report : reports) {
            List<DeadCodeFragment> fragments = deadCodeFragmentRepository.findByFileReportId(report.getId());
            System.out.println("DEBUG: Fragments in " + report.getFilePath() + ": " + fragments.size());
            
            String fileName = extractFileName(report.getFilePath());
            Set<Integer> lines = foundDeadLines.computeIfAbsent(fileName, k -> new HashSet<>());
            
            for (DeadCodeFragment fragment : fragments) {
                // Добавляем все строки из диапазона
                for (int line = fragment.getLineStart(); line <= fragment.getLineEnd(); line++) {
                    lines.add(line);
                }
                System.out.println("DEBUG: Found dead code in " + fileName + ": lines " + 
                                 fragment.getLineStart() + "-" + fragment.getLineEnd());
            }
        }
        
        // Собираем реальный мёртвый код из ground truth (по файлам)
        Map<String, Set<Integer>> actualDeadLines = new HashMap<>();
        if (groundTruth.getDeadCodeLocations() != null) {
            for (DeadCodeMetricsDto.DeadCodeLocation location : groundTruth.getDeadCodeLocations()) {
                String fileName = extractFileName(location.getFile());
                Set<Integer> lines = actualDeadLines.computeIfAbsent(fileName, k -> new HashSet<>());
                
                // Парсим диапазон строк (например, "64-293")
                int[] range = parseLineRange(location.getLines());
                for (int line = range[0]; line <= range[1]; line++) {
                    lines.add(line);
                }
                System.out.println("DEBUG: Actual dead code in " + fileName + ": lines " + location.getLines());
            }
        }
        
        // Подсчитываем метрики на уровне строк
        int truePositives = 0;   // Строки мёртвого кода, правильно найденные
        int falsePositives = 0;  // Живые строки, ошибочно помеченные как мёртвые
        int falseNegatives = 0;  // Строки мёртвого кода, которые не нашли
        
        // Считаем TP и FP
        for (Map.Entry<String, Set<Integer>> entry : foundDeadLines.entrySet()) {
            String fileName = entry.getKey();
            Set<Integer> foundLines = entry.getValue();
            Set<Integer> actualLines = actualDeadLines.getOrDefault(fileName, new HashSet<>());
            
            for (Integer line : foundLines) {
                if (actualLines.contains(line)) {
                    truePositives++;
                } else {
                    falsePositives++;
                }
            }
        }
        
        // Считаем FN
        for (Map.Entry<String, Set<Integer>> entry : actualDeadLines.entrySet()) {
            String fileName = entry.getKey();
            Set<Integer> actualLines = entry.getValue();
            Set<Integer> foundLines = foundDeadLines.getOrDefault(fileName, new HashSet<>());
            
            for (Integer line : actualLines) {
                if (!foundLines.contains(line)) {
                    falseNegatives++;
                }
            }
        }
        
        System.out.println("DEBUG: TP=" + truePositives + ", FP=" + falsePositives + ", FN=" + falseNegatives);
        
        // Рассчитываем метрики
        double precision = (truePositives + falsePositives) == 0 ? 0.0 : 
                          (double) truePositives / (truePositives + falsePositives);
        
        double recall = (truePositives + falseNegatives) == 0 ? 0.0 : 
                       (double) truePositives / (truePositives + falseNegatives);
        
        double f1Score = (precision + recall) == 0 ? 0.0 : 
                        2 * (precision * recall) / (precision + recall);
        
        // FPR = FP / (FP + TN)
        // TN = общее количество живых строк - FP
        int totalLines = groundTruth.getStatistics() != null ? 
                        groundTruth.getStatistics().getTotalLinesOfCode() : 1000;
        int totalDeadLines = groundTruth.getStatistics() != null ?
                            groundTruth.getStatistics().getDeadCodeLines() : 0;
        int totalLiveLines = totalLines - totalDeadLines;
        int trueNegatives = Math.max(0, totalLiveLines - falsePositives);
        
        double falsePositiveRate = (falsePositives + trueNegatives) == 0 ? 0.0 : 
                                   (double) falsePositives / (falsePositives + trueNegatives);
        
        System.out.println("DEBUG: Precision=" + precision + ", Recall=" + recall + ", F1=" + f1Score + ", FPR=" + falsePositiveRate);
        
        // Устанавливаем метрики
        session.setPrecision(precision);
        session.setRecall(recall);
        session.setF1Score(f1Score);
        session.setFalsePositiveRate(falsePositiveRate);
    }
    
    /**
     * Извлекает имя файла из полного пути
     */
    private String extractFileName(String path) {
        if (path == null) return "";
        
        // Убираем временные директории
        String normalized = normalizeFilePath(path);
        
        // Извлекаем только имя файла
        if (normalized.contains("/")) {
            return normalized.substring(normalized.lastIndexOf("/") + 1);
        }
        if (normalized.contains("\\")) {
            return normalized.substring(normalized.lastIndexOf("\\") + 1);
        }
        return normalized;
    }
    
    private String normalizeFilePath(String path) {
        // Нормализуем путь для сравнения (убираем временные директории)
        if (path.contains("codeanalysis_")) {
            int idx = path.indexOf("codeanalysis_");
            String temp = path.substring(idx);
            int nextSlash = temp.indexOf(File.separator, 15); // После "codeanalysis_"
            if (nextSlash > 0) {
                return temp.substring(nextSlash + 1).replace("\\", "/");
            }
        }
        return path.replace("\\", "/");
    }
    
    private boolean isMatchingDeadCode(String code, Set<String> codeSet) {
        // Проверяем точное совпадение
        if (codeSet.contains(code)) {
            return true;
        }
        
        // Проверяем частичное совпадение (файл и пересечение строк)
        String[] parts = code.split(":");
        if (parts.length < 2) return false;
        
        String file = parts[0];
        String lines = parts[1];
        
        // Извлекаем только имя файла из полного пути
        String fileName = file;
        if (file.contains(File.separator)) {
            fileName = file.substring(file.lastIndexOf(File.separator) + 1);
        }
        if (file.contains("/")) {
            fileName = file.substring(file.lastIndexOf("/") + 1);
        }
        
        for (String other : codeSet) {
            String[] otherParts = other.split(":");
            if (otherParts.length < 2) continue;
            
            String otherFile = otherParts[0];
            String otherLines = otherParts[1];
            
            // Извлекаем имя файла из ground truth
            String otherFileName = otherFile;
            if (otherFile.contains(File.separator)) {
                otherFileName = otherFile.substring(otherFile.lastIndexOf(File.separator) + 1);
            }
            if (otherFile.contains("/")) {
                otherFileName = otherFile.substring(otherFile.lastIndexOf("/") + 1);
            }
            
            // Проверяем совпадение имени файла
            if (fileName.equals(otherFileName)) {
                // Проверяем пересечение диапазонов строк
                if (linesOverlap(lines, otherLines)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean linesOverlap(String lines1, String lines2) {
        try {
            int[] range1 = parseLineRange(lines1);
            int[] range2 = parseLineRange(lines2);
            
            // Проверяем пересечение диапазонов
            return range1[0] <= range2[1] && range2[0] <= range1[1];
        } catch (Exception e) {
            return false;
        }
    }
    
    private int[] parseLineRange(String lines) {
        if (lines.contains("-")) {
            String[] parts = lines.split("-");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } else {
            int line = Integer.parseInt(lines);
            return new int[]{line, line};
        }
    }

    private void analyzeWithSimpleTextSearch(List<Path> cssFiles, List<Path> jsFiles, 
                                            List<Path> htmlFiles, AnalysisSession session) throws IOException {
        System.out.println("=== SIMPLE TEXT SEARCH METHOD ===");
        // ОПТИМИЗАЦИЯ: читаем весь контент один раз
        String allContent = getAllContent(htmlFiles, jsFiles);
        
        // Анализируем CSS файлы
        for (Path cssFile : cssFiles) {
            analyzeCssFile(cssFile, allContent, session);
        }

        // Анализируем JS файлы
        for (Path jsFile : jsFiles) {
            analyzeJsFile(jsFile, allContent, session);
        }
    }

    private void analyzeWithAstAnalysis(List<Path> cssFiles, List<Path> jsFiles, 
                                       List<Path> htmlFiles, AnalysisSession session) throws IOException {
        System.out.println("=== AST ANALYSIS METHOD ===");
        // ОПТИМИЗАЦИЯ: читаем весь контент один раз
        String allContent = getAllContent(htmlFiles, jsFiles);
        
        // Анализируем CSS файлы (используем улучшенный метод)
        for (Path cssFile : cssFiles) {
            analyzeCssFileImproved(cssFile, allContent, session);
        }

        // Анализируем JS файлы с AST-подобным подходом
        for (Path jsFile : jsFiles) {
            analyzeJsFileWithAst(jsFile, allContent, session);
        }
    }

    private int findLineNumberWithSelector(String[] lines, String selector) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(selector)) {
                return i + 1; // номера строк с 1
            }
        }
        return 0;
    }

    private String findSelectorSnippet(String[] lines, String selector, int lineNumber) {
        if (lineNumber == 0) return selector;

        int idx = lineNumber - 1;
        StringBuilder snippet = new StringBuilder();

        // Находим начало блока
        int startIdx = idx;
        while (startIdx > 0 && !lines[startIdx].contains(selector) && !lines[startIdx].contains("{")) {
            startIdx--;
        }

        // Собираем блок
        int braceCount = 0;
        boolean blockStarted = false;

        for (int i = startIdx; i < Math.min(lines.length, startIdx + 30); i++) {
            String line = lines[i];
            snippet.append(line).append("\n");

            if (line.contains("{")) {
                blockStarted = true;
            }

            braceCount += countChar(line, '{');
            braceCount -= countChar(line, '}');

            if (blockStarted && braceCount <= 0 && i > startIdx) {
                break;
            }
        }

        String result = snippet.toString().trim();
        if (result.length() > 500) {
            result = result.substring(0, 500) + "...";
        }
        return result;
    }

    private int findLineNumberWithFunction(String[] lines, String functionName) {
        Pattern pattern = Pattern.compile("function\\s+" + Pattern.quote(functionName));
        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                return i + 1;
            }
        }
        return 0;
    }

    private String findFunctionSnippet(String[] lines, String functionName, int lineNumber) {
        if (lineNumber == 0) return functionName;

        StringBuilder snippet = new StringBuilder();
        int idx = lineNumber - 1;
        int braceCount = 0;
        boolean started = false;

        for (int i = idx; i < Math.min(lines.length, idx + 20); i++) {
            String line = lines[i];
            snippet.append(line).append("\n");

            if (!started && line.contains("function")) {
                started = true;
            }

            braceCount += countChar(line, '{');
            braceCount -= countChar(line, '}');

            if (started && braceCount <= 0) {
                break;
            }
        }

        String result = snippet.toString();
        if (result.length() > 500) {
            result = result.substring(0, 500) + "...";
        }
        return result;
    }

    private int findLineNumberWithVariable(String[] lines, String varName) {
        Pattern pattern = Pattern.compile("(?:const|let|var)\\s+" + Pattern.quote(varName) + "\\s*=");
        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                return i + 1;
            }
        }
        return 0;
    }

    private String findVariableSnippet(String[] lines, String varName, int lineNumber) {
        if (lineNumber == 0) return varName;
        String line = lines[lineNumber - 1].trim();
        if (line.length() > 200) {
            line = line.substring(0, 200) + "...";
        }
        return line;
    }

    /**
     * Подсчитывает реальные использования функции (исключая объявление)
     */
    private int countFunctionUsages(String funcName, String allContent, String fileContent) {
        int count = 0;
        
        // 1. Прямой вызов: funcName()
        Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*\\(");
        Matcher callMatcher = callPattern.matcher(allContent);
        while (callMatcher.find()) {
            // Проверяем, что это не объявление функции
            int start = Math.max(0, callMatcher.start() - 10);
            String before = allContent.substring(start, callMatcher.start());
            if (!before.contains("function") && !before.contains("=")) {
                count++;
            }
        }
        
        // 2. Передача как callback: addEventListener('click', funcName)
        Pattern callbackPattern = Pattern.compile("[,\\(]\\s*" + Pattern.quote(funcName) + "\\s*[,\\)]");
        Matcher callbackMatcher = callbackPattern.matcher(allContent);
        while (callbackMatcher.find()) {
            count++;
        }
        
        // 3. Использование в HTML: onclick="funcName()"
        Pattern htmlPattern = Pattern.compile("on\\w+=[\"'][^\"']*" + Pattern.quote(funcName) + "[^\"']*[\"']");
        if (htmlPattern.matcher(allContent).find()) {
            count++;
        }
        
        return count;
    }

    private int countChar(String text, char ch) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    private int findBlockEndLine(String[] lines, int startLine) {
        if (startLine == 0) return startLine;

        int idx = startLine - 1;
        int braceCount = 0;
        boolean blockStarted = false;

        for (int i = idx; i < Math.min(lines.length, idx + 30); i++) {
            String line = lines[i];

            if (line.contains("{")) {
                blockStarted = true;
            }

            braceCount += countChar(line, '{');
            braceCount -= countChar(line, '}');

            if (blockStarted && braceCount <= 0 && i > idx) {
                return i + 1;
            }
        }

        return startLine;
    }

    private int findJsBlockEndLine(String[] lines, int startLine) {
        if (startLine == 0) return startLine;

        int idx = startLine - 1;
        int braceCount = 0;
        boolean blockStarted = false;

        for (int i = idx; i < Math.min(lines.length, idx + 50); i++) {
            String line = lines[i];

            if (line.contains("{") && !blockStarted) {
                blockStarted = true;
            }

            braceCount += countChar(line, '{');
            braceCount -= countChar(line, '}');

            if (blockStarted && braceCount <= 0 && i > idx) {
                return i + 1;
            }
        }

        return startLine;
    }

    public AnalysisSession analyzeProject(MultipartFile zipFile, AnalysisSession session) throws IOException {
        // Создаём временную директорию
        Path tempDir = Files.createTempDirectory("codeanalysis_");

        try {
            // 1. Распаковываем ZIP
            unzip(zipFile, tempDir);

            // 2. Собираем все файлы
            List<Path> cssFiles = findFiles(tempDir, ".css");
            List<Path> jsFiles = findFiles(tempDir, ".js");
            List<Path> htmlFiles = findFiles(tempDir, ".html");

            // ОПТИМИЗАЦИЯ: читаем весь контент один раз
            String allContent = getAllContent(htmlFiles, jsFiles);

            // 3. Анализируем CSS файлы
            for (Path cssFile : cssFiles) {
                analyzeCssFile(cssFile, allContent, session);
            }

            // 4. Анализируем JS файлы
            for (Path jsFile : jsFiles) {
                analyzeJsFile(jsFile, allContent, session);
            }

            // 5. Вычисляем общую метрику здоровья
            double totalHealth = calculateHealthScore(session);
            session.setHealthScore(totalHealth);

            return session;

        } finally {
            // Удаляем временные файлы
            deleteDirectory(tempDir);
        }
    }

    private void analyzeCssFile(Path cssFile, String allContent, AnalysisSession session) throws IOException {
        String content = Files.readString(cssFile);
        List<String> selectors = extractCssSelectors(content);

        // Находим используемые и неиспользуемые селекторы
        List<String> unusedSelectors = new ArrayList<>();

        for (String selector : selectors) {
            // Пропускаем специальные ключевые слова CSS
            if (selector.equals("from") || selector.equals("to")) {
                continue;
            }
            
            // Убираем префиксы . и # для более точного поиска
            String cleanSelector = selector.replaceAll("^[.#]", "");
            
            // Для составных селекторов (например, .nav-link.active) проверяем каждую часть
            boolean isUsed = false;
            if (cleanSelector.contains(".")) {
                // Составной селектор: проверяем, что ВСЕ части есть в HTML
                String[] parts = cleanSelector.split("\\.");
                boolean allPartsFound = true;
                for (String part : parts) {
                    if (!part.isEmpty() && !isUsedInHtml(part, allContent)) {
                        allPartsFound = false;
                        break;
                    }
                }
                isUsed = allPartsFound;
            } else {
                // Простой селектор - проверяем использование в HTML/JS
                isUsed = isUsedInHtml(cleanSelector, allContent);
            }
            
            if (!isUsed) {
                unusedSelectors.add(selector);
            }
        }

        // Вычисляем размер неиспользуемого кода
        long totalSize = Files.size(cssFile);
        long unusedSize = estimateUnusedSize(content, unusedSelectors);
        double unusedPercentage = (totalSize == 0) ? 0 : (unusedSize * 100.0 / totalSize);

        // Создаём FileReport
        FileReport report = new FileReport();
        report.setAnalysis(session);
        report.setFilePath(cssFile.toString());
        report.setTotalSizeBytes(totalSize);
        report.setUnusedSizeBytes(unusedSize);
        report.setUnusedPercentage(unusedPercentage);
        report.setFileType(FileType.CSS);

        fileReportRepository.save(report);

        // ОПТИМИЗАЦИЯ: Собираем все фрагменты и сохраняем одним батчем
        List<DeadCodeFragment> fragments = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (String selector : unusedSelectors) {
            int lineNumber = findLineNumberWithSelector(lines, selector);
            String snippet = findSelectorSnippet(lines, selector, lineNumber);
            int lineEnd = findBlockEndLine(lines, lineNumber);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(selector);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("CSS селектор не найден в HTML/JS файлах");
            fragment.setLineStart(lineNumber);
            fragment.setLineEnd(lineEnd);
            
            fragments.add(fragment);
        }
        
        // Сохраняем все фрагменты одним запросом
        if (!fragments.isEmpty()) {
            deadCodeFragmentRepository.saveAll(fragments);
        }
    }

    private void analyzeJsFile(Path jsFile, String allContent, AnalysisSession session) throws IOException {
        String content = Files.readString(jsFile);
        List<String> functions = extractJsFunctions(content);
        List<String> variables = extractJsVariables(content);

        // Находим неиспользуемые функции
        List<String> unusedFunctions = new ArrayList<>();
        for (String func : functions) {
            // Подсчитываем использования функции с помощью regex для точности
            int usageCount = countFunctionUsages(func, allContent, content);
            
            // Если функция не используется (только объявление) - она мёртвая
            if (usageCount == 0) {
                unusedFunctions.add(func);
            }
        }

        // Находим неиспользуемые переменные
        List<String> unusedVariables = new ArrayList<>();
        for (String var : variables) {
            // Подсчитываем использования переменной (исключая объявление)
            int usageCount = countOccurrences(content, "\\b" + var + "\\b") - 1; // -1 для объявления
            
            // Проверяем использование в других файлах
            String otherFiles = allContent.replace(content, "");
            int usageInOtherFiles = countOccurrences(otherFiles, "\\b" + var + "\\b");
            
            if (usageCount <= 0 && usageInOtherFiles == 0) {
                unusedVariables.add(var);
            }
        }

        long totalSize = Files.size(jsFile);
        long unusedSize = estimateUnusedSize(content, unusedFunctions) +
                estimateUnusedSize(content, unusedVariables);
        double unusedPercentage = (totalSize == 0) ? 0 : (unusedSize * 100.0 / totalSize);

        // Создаём FileReport
        FileReport report = new FileReport();
        report.setAnalysis(session);
        report.setFilePath(jsFile.toString());
        report.setTotalSizeBytes(totalSize);
        report.setUnusedSizeBytes(unusedSize);
        report.setUnusedPercentage(unusedPercentage);
        report.setFileType(FileType.JS);

        fileReportRepository.save(report);

        String[] lines = content.split("\n");
        
        // ОПТИМИЗАЦИЯ: Собираем все фрагменты
        List<DeadCodeFragment> fragments = new ArrayList<>();

        // Создаём фрагменты для неиспользуемых функций
        for (String func : unusedFunctions) {
            int lineNumber = findLineNumberWithFunction(lines, func);
            String snippet = findFunctionSnippet(lines, func, lineNumber);
            int lineEnd = findJsBlockEndLine(lines, lineNumber);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(func);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("JS функция не вызывается в проекте");
            fragment.setLineStart(lineNumber);
            fragment.setLineEnd(lineEnd);

            fragments.add(fragment);
        }

        // Создаём фрагменты для неиспользуемых переменных
        for (String var : unusedVariables) {
            int lineNumber = findLineNumberWithVariable(lines, var);
            String snippet = findVariableSnippet(lines, var, lineNumber);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(var);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("JS переменная объявлена, но не используется");
            fragment.setLineStart(lineNumber);
            fragment.setLineEnd(lineNumber);

            fragments.add(fragment);
        }
        
        // Сохраняем все фрагменты одним запросом
        if (!fragments.isEmpty()) {
            deadCodeFragmentRepository.saveAll(fragments);
        }
    }

    private List<String> extractCssSelectors(String css) {
        Set<String> selectorsSet = new HashSet<>();
        
        // Удаляем комментарии
        String cleanCss = css.replaceAll("/\\*.*?\\*/", "");
        
        // Регулярное выражение для поиска CSS селекторов
        Pattern pattern = Pattern.compile("([^{}]+)\\{");
        Matcher matcher = pattern.matcher(cleanCss);

        while (matcher.find()) {
            String selectorPart = matcher.group(1).trim();
            
            // Пропускаем @-правила (media, keyframes и т.д.)
            if (selectorPart.startsWith("@")) {
                continue;
            }
            
            // Разделяем по запятым для множественных селекторов
            String[] parts = selectorPart.split(",");
            for (String part : parts) {
                String selector = part.trim();
                
                if (selector.isEmpty()) {
                    continue;
                }
                
                // Убираем псевдоклассы и псевдоэлементы для проверки
                String mainSelector = selector.split(":")[0].trim();
                
                if (!mainSelector.isEmpty() && !mainSelector.startsWith("@")) {
                    selectorsSet.add(mainSelector);
                }
            }
        }

        return new ArrayList<>(selectorsSet);
    }

    private List<String> extractJsFunctions(String js) {
        Set<String> functions = new HashSet<>();
        
        // 1. function name()
        Pattern pattern1 = Pattern.compile("function\\s+(\\w+)\\s*\\(");
        Matcher matcher1 = pattern1.matcher(js);
        while (matcher1.find()) {
            functions.add(matcher1.group(1));
        }
        
        // 2. const/let/var name = function()
        Pattern pattern2 = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=\\s*function");
        Matcher matcher2 = pattern2.matcher(js);
        while (matcher2.find()) {
            functions.add(matcher2.group(1));
        }
        
        // 3. const/let/var name = () => или async () =>
        Pattern pattern3 = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:async\\s+)?\\([^)]*\\)\\s*=>");
        Matcher matcher3 = pattern3.matcher(js);
        while (matcher3.find()) {
            functions.add(matcher3.group(1));
        }
        
        // 4. async function name()
        Pattern pattern4 = Pattern.compile("async\\s+function\\s+(\\w+)\\s*\\(");
        Matcher matcher4 = pattern4.matcher(js);
        while (matcher4.find()) {
            functions.add(matcher4.group(1));
        }
        
        return new ArrayList<>(functions);
    }

    private List<String> extractJsVariables(String js) {
        Set<String> variables = new HashSet<>();
        
        // Ищем объявления переменных (НЕ функций)
        Pattern pattern = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?!function|async|\\([^)]*\\)\\s*=>)");
        Matcher matcher = pattern.matcher(js);

        while (matcher.find()) {
            String varName = matcher.group(1);
            // Исключаем зарезервированные слова
            if (!varName.equals("function") && !varName.equals("async")) {
                variables.add(varName);
            }
        }
        return new ArrayList<>(variables);
    }

    private String getAllContent(List<Path> htmlFiles, List<Path> jsFiles) throws IOException {
        StringBuilder content = new StringBuilder();
        for (Path file : htmlFiles) {
            content.append(Files.readString(file));
        }
        for (Path file : jsFiles) {
            content.append(Files.readString(file));
        }
        return content.toString();
    }

    private int countOccurrences(String text, String word) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private long estimateUnusedSize(String content, List<String> unusedItems) {
        long size = 0;
        for (String item : unusedItems) {
            // Ищем размер фрагмента кода, содержащего этот селектор/функцию
            Pattern pattern = Pattern.compile("[^;{}]*" + Pattern.quote(item) + "[^;{}]*[;{}]");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                size += matcher.group().length();
            }
        }
        return size;
    }

    private String findSelectorInFile(String content, String selector) {
        Pattern pattern = Pattern.compile("[^;{}]*" + Pattern.quote(selector) + "[^;{}]*[;{}]");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String snippet = matcher.group().trim();
            if (snippet.length() > 200) {
                snippet = snippet.substring(0, 200) + "...";
            }
            return snippet;
        }
        return selector;
    }

    private String findFunctionInFile(String content, String functionName) {
        Pattern pattern = Pattern.compile("function\\s+" + Pattern.quote(functionName) + "\\s*\\([^)]*\\)\\s*\\{[^}]*\\}");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String snippet = matcher.group();
            if (snippet.length() > 300) {
                snippet = snippet.substring(0, 300) + "...";
            }
            return snippet;
        }
        return functionName;
    }

    private int findLineNumber(String content, String searchText) {
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(searchText)) {
                return i + 1; // номера строк с 1
            }
        }
        return 0;
    }

    private double calculateHealthScore(AnalysisSession session) {
        List<FileReport> reports = fileReportRepository.findByAnalysisId(session.getId());
        if (reports.isEmpty()) {
            return 100.0;
        }

        double totalUnusedPercentage = 0;
        for (FileReport report : reports) {
            totalUnusedPercentage += report.getUnusedPercentage();
        }
        double averageUnused = totalUnusedPercentage / reports.size();

        // HealthScore = 100 - средний процент мёртвого кода
        return Math.max(0, 100 - averageUnused);
    }

    // Вспомогательные методы для работы с файлами
    private void unzip(MultipartFile zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private List<Path> findFiles(Path dir, String extension) throws IOException {
        List<Path> result = new ArrayList<>();
        try (var stream = Files.walk(dir)) {
            stream.filter(path -> path.toString().endsWith(extension))
                    .forEach(result::add);
        }
        return result;
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                // ignore
                            }
                        });
            }
        }
    }

    // ========== УЛУЧШЕННЫЕ МЕТОДЫ АНАЛИЗА ==========

    /**
     * Улучшенный анализ CSS - учитывает динамическую генерацию классов
     */
    private void analyzeCssFileImproved(Path cssFile, String allContent, AnalysisSession session) throws IOException {
        String content = Files.readString(cssFile);
        List<String> selectors = extractCssSelectors(content);

        // Находим неиспользуемые селекторы с улучшенной проверкой
        List<String> unusedSelectors = new ArrayList<>();

        for (String selector : selectors) {
            // Пропускаем ключевые слова CSS
            if (selector.equals("from") || selector.equals("to")) {
                continue;
            }
            
            if (!isSelectorUsed(selector, allContent)) {
                unusedSelectors.add(selector);
            }
        }

        // Вычисляем размер неиспользуемого кода
        long totalSize = Files.size(cssFile);
        long unusedSize = estimateUnusedSize(content, unusedSelectors);
        double unusedPercentage = (totalSize == 0) ? 0 : (unusedSize * 100.0 / totalSize);

        // Создаём FileReport
        FileReport report = new FileReport();
        report.setAnalysis(session);
        report.setFilePath(cssFile.toString());
        report.setTotalSizeBytes(totalSize);
        report.setUnusedSizeBytes(unusedSize);
        report.setUnusedPercentage(unusedPercentage);
        report.setFileType(FileType.CSS);

        fileReportRepository.save(report);

        // Создаём DeadCodeFragment для каждого неиспользуемого селектора
        String[] lines = content.split("\n");
        for (String selector : unusedSelectors) {
            int lineNumber = findLineNumberWithSelector(lines, selector);
            String snippet = findSelectorSnippet(lines, selector, lineNumber);
            int lineEnd = findBlockEndLine(lines, lineNumber);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(selector);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("CSS селектор не найден в HTML/JS файлах (улучшенный поиск)");
            fragment.setLineStart(lineNumber);
            fragment.setLineEnd(lineEnd);

            deadCodeFragmentRepository.save(fragment);
        }
    }

    /**
     * Проверяет использование CSS селектора с учётом различных вариантов
     */
    private boolean isSelectorUsed(String selector, String content) {
        // Убираем префиксы . и # для поиска
        String cleanSelector = selector.replaceAll("^[.#]", "");
        
        // Проверяем прямое упоминание
        if (content.contains(cleanSelector)) {
            return true;
        }
        
        // Проверяем в атрибутах class и id
        if (content.matches("(?s).*class=[\"'][^\"']*" + Pattern.quote(cleanSelector) + "[^\"']*[\"'].*")) {
            return true;
        }
        if (content.matches("(?s).*id=[\"']" + Pattern.quote(cleanSelector) + "[\"'].*")) {
            return true;
        }
        
        // Проверяем динамическую генерацию через шаблонные строки
        if (content.contains("${") && content.contains(cleanSelector.substring(0, Math.min(5, cleanSelector.length())))) {
            return true;
        }
        
        return false;
    }

    /**
     * AST-подобный анализ JavaScript - строит граф зависимостей
     */
    private void analyzeJsFileWithAst(Path jsFile, String allContent, AnalysisSession session) throws IOException {
        System.out.println("DEBUG [AST]: Analyzing JS file: " + jsFile.getFileName());
        String content = Files.readString(jsFile);
        
        // Извлекаем все определения
        Map<String, FunctionInfo> functions = extractFunctionsWithInfo(content);
        Map<String, VariableInfo> variables = extractVariablesWithInfo(content);
        
        System.out.println("DEBUG [AST]: Total functions found: " + functions.size());
        System.out.println("DEBUG [AST]: Total variables found: " + variables.size());
        
        // Строим граф вызовов
        Set<String> usedFunctions = new HashSet<>();
        Set<String> usedVariables = new HashSet<>();
        
        // Находим точки входа (функции, вызываемые из HTML или глобально)
        for (String funcName : functions.keySet()) {
            if (isEntryPoint(funcName, allContent, content)) {
                usedFunctions.add(funcName);
                // Рекурсивно помечаем все вызываемые функции
                markUsedDependencies(funcName, content, functions, usedFunctions);
            }
        }
        
        System.out.println("DEBUG [AST]: Used functions: " + usedFunctions.size());
        
        // Проверяем использование переменных
        for (String varName : variables.keySet()) {
            int usageCount = countOccurrences(allContent, varName);
            if (usageCount > 1) { // Больше 1, т.к. 1 раз - это объявление
                usedVariables.add(varName);
            }
        }
        
        System.out.println("DEBUG [AST]: Used variables: " + usedVariables.size());
        
        // Находим неиспользуемые элементы
        List<String> unusedFunctions = new ArrayList<>();
        for (String funcName : functions.keySet()) {
            if (!usedFunctions.contains(funcName)) {
                unusedFunctions.add(funcName);
            }
        }
        
        List<String> unusedVariables = new ArrayList<>();
        for (String varName : variables.keySet()) {
            if (!usedVariables.contains(varName)) {
                unusedVariables.add(varName);
            }
        }
        
        System.out.println("DEBUG [AST]: Unused functions: " + unusedFunctions.size());
        System.out.println("DEBUG [AST]: Unused variables: " + unusedVariables.size());
        
        long totalSize = Files.size(jsFile);
        long unusedSize = estimateUnusedSizeFromInfo(functions, unusedFunctions, variables, unusedVariables);
        double unusedPercentage = (totalSize == 0) ? 0 : (unusedSize * 100.0 / totalSize);

        // Создаём FileReport
        FileReport report = new FileReport();
        report.setAnalysis(session);
        report.setFilePath(jsFile.toString());
        report.setTotalSizeBytes(totalSize);
        report.setUnusedSizeBytes(unusedSize);
        report.setUnusedPercentage(unusedPercentage);
        report.setFileType(FileType.JS);

        fileReportRepository.save(report);

        String[] lines = content.split("\n");

        // Создаём фрагменты для неиспользуемых функций
        for (String func : unusedFunctions) {
            FunctionInfo info = functions.get(func);
            String snippet = findFunctionSnippet(lines, func, info.lineStart);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(func);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("JS функция не вызывается (AST-анализ)");
            fragment.setLineStart(info.lineStart);
            fragment.setLineEnd(info.lineEnd);

            deadCodeFragmentRepository.save(fragment);
        }

        // Создаём фрагменты для неиспользуемых переменных
        for (String var : unusedVariables) {
            VariableInfo info = variables.get(var);
            String snippet = findVariableSnippet(lines, var, info.lineStart);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(var);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("JS переменная не используется (AST-анализ)");
            fragment.setLineStart(info.lineStart);
            fragment.setLineEnd(info.lineStart);

            deadCodeFragmentRepository.save(fragment);
        }
    }

    /**
     * Извлекает функции с информацией о позиции и теле
     */
    private Map<String, FunctionInfo> extractFunctionsWithInfo(String js) {
        Map<String, FunctionInfo> functions = new HashMap<>();
        String[] lines = js.split("\n");
        
        // Паттерны для различных объявлений функций
        Pattern functionPattern = Pattern.compile("function\\s+(\\w+)\\s*\\(");
        Pattern arrowPattern = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:\\([^)]*\\)|\\w+)\\s*=>");
        Pattern methodPattern = Pattern.compile("(\\w+)\\s*\\([^)]*\\)\\s*\\{");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // function name()
            Matcher m1 = functionPattern.matcher(line);
            if (m1.find()) {
                String name = m1.group(1);
                int endLine = findJsBlockEndLine(lines, i + 1);
                functions.put(name, new FunctionInfo(name, i + 1, endLine));
            }
            
            // const name = () =>
            Matcher m2 = arrowPattern.matcher(line);
            if (m2.find()) {
                String name = m2.group(1);
                int endLine = findJsBlockEndLine(lines, i + 1);
                functions.put(name, new FunctionInfo(name, i + 1, endLine));
            }
        }
        
        return functions;
    }

    /**
     * Извлекает переменные с информацией о позиции
     */
    private Map<String, VariableInfo> extractVariablesWithInfo(String js) {
        Map<String, VariableInfo> variables = new HashMap<>();
        String[] lines = js.split("\n");
        
        Pattern pattern = Pattern.compile("(?:const|let|var)\\s+(\\w+)\\s*=");
        
        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                String name = matcher.group(1);
                // Пропускаем функции (они уже обработаны)
                if (!lines[i].contains("function") && !lines[i].contains("=>")) {
                    variables.put(name, new VariableInfo(name, i + 1));
                }
            }
        }
        
        return variables;
    }

    /**
     * Проверяет, является ли функция точкой входа
     */
    private boolean isEntryPoint(String funcName, String allContent, String fileContent) {
        // 1. Вызывается из HTML через onclick и другие события
        Pattern htmlEventPattern = Pattern.compile("on\\w+=[\"'][^\"']*" + Pattern.quote(funcName) + "[^\"']*[\"']");
        if (htmlEventPattern.matcher(allContent).find()) {
            return true;
        }
        
        // 2. Передается в addEventListener или другие обработчики (БЕЗ скобок)
        Pattern listenerPattern = Pattern.compile("addEventListener\\s*\\([^,]+,\\s*" + Pattern.quote(funcName) + "\\s*[,\\)]");
        if (listenerPattern.matcher(allContent).find()) {
            return true;
        }
        
        // 3. Прямой вызов функции (с скобками) - НО исключаем объявление
        Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*\\(");
        Matcher callMatcher = callPattern.matcher(allContent);
        while (callMatcher.find()) {
            // Проверяем, что это не объявление функции
            int start = Math.max(0, callMatcher.start() - 10);
            String before = allContent.substring(start, callMatcher.start());
            if (!before.contains("function") && !before.contains("=")) {
                return true;
            }
        }
        
        // 4. Экспортируется
        if (fileContent.contains("export") && fileContent.contains(funcName)) {
            return true;
        }
        
        return false;
    }

    /**
     * Рекурсивно помечает используемые зависимости
     */
    private void markUsedDependencies(String funcName, String content, 
                                     Map<String, FunctionInfo> allFunctions, 
                                     Set<String> usedFunctions) {
        FunctionInfo info = allFunctions.get(funcName);
        if (info == null) return;
        
        // Получаем тело функции
        String[] lines = content.split("\n");
        if (info.lineStart <= 0 || info.lineEnd > lines.length) return;
        
        StringBuilder functionBody = new StringBuilder();
        for (int i = info.lineStart - 1; i < Math.min(info.lineEnd, lines.length); i++) {
            functionBody.append(lines[i]).append("\n");
        }
        String funcBody = functionBody.toString();
        
        // Ищем вызовы других функций внутри ТЕЛА этой функции
        for (String otherFunc : allFunctions.keySet()) {
            if (!usedFunctions.contains(otherFunc) && !otherFunc.equals(funcName)) {
                // Проверяем, вызывается ли otherFunc внутри тела funcName
                Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(otherFunc) + "\\s*\\(");
                if (callPattern.matcher(funcBody).find()) {
                    usedFunctions.add(otherFunc);
                    markUsedDependencies(otherFunc, content, allFunctions, usedFunctions);
                }
            }
        }
    }

    /**
     * Оценивает размер неиспользуемого кода на основе информации о функциях/переменных
     */
    private long estimateUnusedSizeFromInfo(Map<String, FunctionInfo> functions, 
                                            List<String> unusedFunctions,
                                            Map<String, VariableInfo> variables,
                                            List<String> unusedVariables) {
        long size = 0;
        
        for (String func : unusedFunctions) {
            FunctionInfo info = functions.get(func);
            if (info != null) {
                size += (info.lineEnd - info.lineStart + 1) * 50; // Примерно 50 символов на строку
            }
        }
        
        for (String var : unusedVariables) {
            size += 100; // Примерная длина объявления переменной
        }
        
        return size;
    }

    /**
     * Проверяет использование селектора в HTML/JS коде
     * Ищет в атрибутах class, id и querySelector
     */
    private boolean isUsedInHtml(String cleanSelector, String allContent) {
        // 1. Проверка в атрибуте class="..."
        Pattern classPattern = Pattern.compile("class=[\"'][^\"']*\\b" + Pattern.quote(cleanSelector) + "\\b[^\"']*[\"']");
        if (classPattern.matcher(allContent).find()) {
            return true;
        }
        
        // 2. Проверка в атрибуте id="..."
        Pattern idPattern = Pattern.compile("id=[\"']" + Pattern.quote(cleanSelector) + "[\"']");
        if (idPattern.matcher(allContent).find()) {
            return true;
        }
        
        // 3. Проверка в querySelector/querySelectorAll
        if (allContent.contains("querySelector") && allContent.contains(cleanSelector)) {
            return true;
        }
        
        // 4. Проверка в classList операциях
        if (allContent.contains("classList") && allContent.contains(cleanSelector)) {
            return true;
        }
        
        return false;
    }

    // Вспомогательные классы для хранения информации
    private static class FunctionInfo {
        String name;
        int lineStart;
        int lineEnd;
        
        FunctionInfo(String name, int lineStart, int lineEnd) {
            this.name = name;
            this.lineStart = lineStart;
            this.lineEnd = lineEnd;
        }
    }
    
    private static class VariableInfo {
        String name;
        int lineStart;
        
        VariableInfo(String name, int lineStart) {
            this.name = name;
            this.lineStart = lineStart;
        }
    }

    // ========== COVERAGE-BASED АНАЛИЗ ==========

    /**
     * Coverage-based анализ - имитация инструментов покрытия кода
     * Учитывает реальные паттерны использования, event handlers, динамическую загрузку
     */
    private void analyzeWithCoverageBased(List<Path> cssFiles, List<Path> jsFiles, 
                                         List<Path> htmlFiles, AnalysisSession session) throws IOException {
        System.out.println("=== COVERAGE-BASED METHOD ===");
        // ОПТИМИЗАЦИЯ: читаем весь контент один раз
        String allContent = getAllContent(htmlFiles, jsFiles);
        
        // Анализируем CSS с учётом динамического применения
        for (Path cssFile : cssFiles) {
            analyzeCssWithCoverage(cssFile, allContent, session);
        }

        // Анализируем JS с учётом реального выполнения
        for (Path jsFile : jsFiles) {
            analyzeJsWithCoverage(jsFile, allContent, session);
        }
    }

    /**
     * Анализ CSS с имитацией coverage - учитывает динамическое применение стилей
     */
    private void analyzeCssWithCoverage(Path cssFile, String allContent, AnalysisSession session) throws IOException {
        String content = Files.readString(cssFile);
        List<String> selectors = extractCssSelectors(content);

        List<String> unusedSelectors = new ArrayList<>();
        Map<String, String> usageReasons = new HashMap<>();

        for (String selector : selectors) {
            // Пропускаем ключевые слова CSS
            if (selector.equals("from") || selector.equals("to")) {
                continue;
            }
            
            CoverageResult coverage = checkSelectorCoverage(selector, allContent, content);
            if (!coverage.isUsed) {
                unusedSelectors.add(selector);
                usageReasons.put(selector, coverage.reason);
            }
        }

        long totalSize = Files.size(cssFile);
        long unusedSize = estimateUnusedSize(content, unusedSelectors);
        double unusedPercentage = (totalSize == 0) ? 0 : (unusedSize * 100.0 / totalSize);

        FileReport report = new FileReport();
        report.setAnalysis(session);
        report.setFilePath(cssFile.toString());
        report.setTotalSizeBytes(totalSize);
        report.setUnusedSizeBytes(unusedSize);
        report.setUnusedPercentage(unusedPercentage);
        report.setFileType(FileType.CSS);

        fileReportRepository.save(report);

        String[] lines = content.split("\n");
        for (String selector : unusedSelectors) {
            int lineNumber = findLineNumberWithSelector(lines, selector);
            String snippet = findSelectorSnippet(lines, selector, lineNumber);
            int lineEnd = findBlockEndLine(lines, lineNumber);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(selector);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("CSS селектор не используется (Coverage-анализ): " + usageReasons.get(selector));
            fragment.setLineStart(lineNumber);
            fragment.setLineEnd(lineEnd);

            deadCodeFragmentRepository.save(fragment);
        }
    }

    /**
     * Проверка покрытия CSS селектора с детальным анализом
     */
    private CoverageResult checkSelectorCoverage(String selector, String allContent, String cssContent) {
        String cleanSelector = selector.replaceAll("^[.#]", "");
        
        // Для составных селекторов (например, .nav-link.active) проверяем все части
        if (cleanSelector.contains(".")) {
            String[] parts = cleanSelector.split("\\.");
            boolean allPartsFound = true;
            for (String part : parts) {
                if (!part.isEmpty() && !allContent.contains(part)) {
                    allPartsFound = false;
                    break;
                }
            }
            if (allPartsFound) {
                return new CoverageResult(true, "Все части составного селектора найдены в HTML");
            }
        }
        
        // 1. Прямое использование в HTML
        if (allContent.contains(cleanSelector)) {
            return new CoverageResult(true, "Используется в HTML");
        }
        
        // 2. Динамическое добавление через JavaScript
        if (allContent.contains("classList.add") && allContent.contains(cleanSelector)) {
            return new CoverageResult(true, "Добавляется динамически через classList");
        }
        
        if (allContent.contains("className") && allContent.contains(cleanSelector)) {
            return new CoverageResult(true, "Устанавливается через className");
        }
        
        // 3. jQuery/DOM манипуляции
        if (allContent.contains("$(" + selector) || allContent.contains("$('" + selector)) {
            return new CoverageResult(true, "Используется в jQuery селекторе");
        }
        
        if (allContent.contains("querySelector") && allContent.contains(selector)) {
            return new CoverageResult(true, "Используется в querySelector");
        }
        
        // 4. Шаблонные строки и конкатенация
        if (allContent.contains("`") && allContent.contains(cleanSelector)) {
            return new CoverageResult(true, "Возможно используется в шаблонной строке");
        }
        
        // 5. Проверка на псевдо-классы (обычно используются)
        if (selector.contains(":hover") || selector.contains(":active") || selector.contains(":focus")) {
            // Проверяем базовый селектор без псевдокласса
            String baseSelector = selector.split(":")[0];
            if (allContent.contains(baseSelector.replaceAll("^[.#]", ""))) {
                return new CoverageResult(true, "Псевдо-класс для используемого элемента");
            }
        }
        
        return new CoverageResult(false, "Не найдено использование в коде");
    }

    /**
     * Анализ JS с имитацией coverage - учитывает реальное выполнение
     */
    private void analyzeJsWithCoverage(Path jsFile, String allContent, AnalysisSession session) throws IOException {
        System.out.println("DEBUG [COVERAGE]: Analyzing JS file: " + jsFile.getFileName());
        String content = Files.readString(jsFile);
        
        Map<String, FunctionInfo> functions = extractFunctionsWithInfo(content);
        Map<String, VariableInfo> variables = extractVariablesWithInfo(content);
        
        System.out.println("DEBUG [COVERAGE]: Total functions found: " + functions.size());
        System.out.println("DEBUG [COVERAGE]: Total variables found: " + variables.size());
        
        // Анализ покрытия функций
        Set<String> coveredFunctions = new HashSet<>();
        Map<String, String> functionCoverageReasons = new HashMap<>();
        
        for (String funcName : functions.keySet()) {
            CoverageResult coverage = checkFunctionCoverage(funcName, allContent, content);
            if (coverage.isUsed) {
                coveredFunctions.add(funcName);
                functionCoverageReasons.put(funcName, coverage.reason);
                // Рекурсивно помечаем зависимости
                markUsedDependencies(funcName, content, functions, coveredFunctions);
            }
        }
        
        System.out.println("DEBUG [COVERAGE]: Covered functions: " + coveredFunctions.size());
        
        // Анализ покрытия переменных
        Set<String> coveredVariables = new HashSet<>();
        for (String varName : variables.keySet()) {
            CoverageResult coverage = checkVariableCoverage(varName, allContent, content);
            if (coverage.isUsed) {
                coveredVariables.add(varName);
            }
        }
        
        System.out.println("DEBUG [COVERAGE]: Covered variables: " + coveredVariables.size());
        
        List<String> unusedFunctions = new ArrayList<>();
        for (String funcName : functions.keySet()) {
            if (!coveredFunctions.contains(funcName)) {
                unusedFunctions.add(funcName);
            }
        }
        
        List<String> unusedVariables = new ArrayList<>();
        for (String varName : variables.keySet()) {
            if (!coveredVariables.contains(varName)) {
                unusedVariables.add(varName);
            }
        }
        
        System.out.println("DEBUG [COVERAGE]: Unused functions: " + unusedFunctions.size());
        System.out.println("DEBUG [COVERAGE]: Unused variables: " + unusedVariables.size());
        
        long totalSize = Files.size(jsFile);
        long unusedSize = estimateUnusedSizeFromInfo(functions, unusedFunctions, variables, unusedVariables);
        double unusedPercentage = (totalSize == 0) ? 0 : (unusedSize * 100.0 / totalSize);

        FileReport report = new FileReport();
        report.setAnalysis(session);
        report.setFilePath(jsFile.toString());
        report.setTotalSizeBytes(totalSize);
        report.setUnusedSizeBytes(unusedSize);
        report.setUnusedPercentage(unusedPercentage);
        report.setFileType(FileType.JS);

        fileReportRepository.save(report);

        String[] lines = content.split("\n");

        for (String func : unusedFunctions) {
            FunctionInfo info = functions.get(func);
            String snippet = findFunctionSnippet(lines, func, info.lineStart);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(func);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("JS функция не покрыта выполнением (Coverage-анализ)");
            fragment.setLineStart(info.lineStart);
            fragment.setLineEnd(info.lineEnd);

            deadCodeFragmentRepository.save(fragment);
        }

        for (String var : unusedVariables) {
            VariableInfo info = variables.get(var);
            String snippet = findVariableSnippet(lines, var, info.lineStart);

            DeadCodeFragment fragment = new DeadCodeFragment();
            fragment.setFileReport(report);
            fragment.setSelectorOrFunction(var);
            fragment.setCodeSnippet(snippet);
            fragment.setReason("JS переменная не покрыта выполнением (Coverage-анализ)");
            fragment.setLineStart(info.lineStart);
            fragment.setLineEnd(info.lineStart);

            deadCodeFragmentRepository.save(fragment);
        }
    }

    /**
     * Проверка покрытия функции с учётом различных паттернов вызова
     */
    private CoverageResult checkFunctionCoverage(String funcName, String allContent, String fileContent) {
        // 1. Event handlers в HTML
        Pattern htmlEventPattern = Pattern.compile("on\\w+=[\"'][^\"']*" + Pattern.quote(funcName) + "[^\"']*[\"']");
        if (htmlEventPattern.matcher(allContent).find()) {
            return new CoverageResult(true, "Вызывается из HTML event handler");
        }
        
        // 2. addEventListener - проверяем, что функция передаётся как параметр
        Pattern listenerPattern = Pattern.compile("addEventListener\\s*\\([^,]+,\\s*" + Pattern.quote(funcName) + "\\s*[,\\)]");
        if (listenerPattern.matcher(allContent).find()) {
            return new CoverageResult(true, "Используется в addEventListener");
        }
        
        // 3. Прямой вызов - НО исключаем объявление
        Pattern directCall = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*\\(");
        Matcher matcher = directCall.matcher(allContent);
        while (matcher.find()) {
            // Проверяем, что это не объявление функции
            int start = Math.max(0, matcher.start() - 10);
            String before = allContent.substring(start, matcher.start());
            if (!before.contains("function") && !before.contains("=")) {
                return new CoverageResult(true, "Вызывается напрямую");
            }
        }
        
        // 4. Экспорт (считается используемым)
        if (fileContent.contains("export") && fileContent.contains(funcName)) {
            return new CoverageResult(true, "Экспортируется (внешнее использование)");
        }
        
        // 5. Callback функции
        Pattern callbackPattern = Pattern.compile("(?:then|catch|finally|map|filter|forEach|reduce)\\s*\\(\\s*" + Pattern.quote(funcName) + "\\s*[,\\)]");
        if (callbackPattern.matcher(allContent).find()) {
            return new CoverageResult(true, "Используется как callback");
        }
        
        // 6. setTimeout/setInterval - проверяем, что функция передаётся как параметр
        Pattern timerPattern = Pattern.compile("(?:setTimeout|setInterval)\\s*\\(\\s*" + Pattern.quote(funcName) + "\\s*[,\\)]");
        if (timerPattern.matcher(allContent).find()) {
            return new CoverageResult(true, "Используется в таймере");
        }
        
        // УДАЛЕНО: проверка на глобальные функции - они НЕ должны автоматически считаться используемыми
        
        return new CoverageResult(false, "Не найдено использование");
    }

    /**
     * Проверка покрытия переменной
     */
    private CoverageResult checkVariableCoverage(String varName, String allContent, String fileContent) {
        // Подсчитываем использования (больше 1 = используется, 1 = только объявление)
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\b");
        Matcher matcher = pattern.matcher(allContent);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        if (count > 1) {
            return new CoverageResult(true, "Используется " + (count - 1) + " раз");
        }
        
        // Проверка на экспорт
        if (fileContent.contains("export") && fileContent.contains(varName)) {
            return new CoverageResult(true, "Экспортируется");
        }
        
        return new CoverageResult(false, "Только объявлена, не используется");
    }

    // Вспомогательный класс для результата coverage
    private static class CoverageResult {
        boolean isUsed;
        String reason;
        
        CoverageResult(boolean isUsed, String reason) {
            this.isUsed = isUsed;
            this.reason = reason;
        }
    }
}