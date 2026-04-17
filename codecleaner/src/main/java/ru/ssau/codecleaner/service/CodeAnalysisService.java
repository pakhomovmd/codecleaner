package ru.ssau.codecleaner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ssau.codecleaner.entity.*;
import ru.ssau.codecleaner.repository.*;

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

            return session;

        } finally {
            // Удаляем временные файлы
            deleteDirectory(tempDir);
        }
    }

    private void analyzeWithSimpleTextSearch(List<Path> cssFiles, List<Path> jsFiles, 
                                            List<Path> htmlFiles, AnalysisSession session) throws IOException {
        // Анализируем CSS файлы
        for (Path cssFile : cssFiles) {
            analyzeCssFile(cssFile, htmlFiles, jsFiles, session);
        }

        // Анализируем JS файлы
        for (Path jsFile : jsFiles) {
            analyzeJsFile(jsFile, htmlFiles, jsFiles, session);
        }
    }

    private void analyzeWithAstAnalysis(List<Path> cssFiles, List<Path> jsFiles, 
                                       List<Path> htmlFiles, AnalysisSession session) throws IOException {
        // Анализируем CSS файлы (используем улучшенный метод)
        for (Path cssFile : cssFiles) {
            analyzeCssFileImproved(cssFile, htmlFiles, jsFiles, session);
        }

        // Анализируем JS файлы с AST-подобным подходом
        for (Path jsFile : jsFiles) {
            analyzeJsFileWithAst(jsFile, htmlFiles, jsFiles, session);
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
            List<Path> allFiles = new ArrayList<>();
            allFiles.addAll(cssFiles);
            allFiles.addAll(jsFiles);
            allFiles.addAll(htmlFiles);

            // 3. Анализируем CSS файлы
            for (Path cssFile : cssFiles) {
                analyzeCssFile(cssFile, htmlFiles, jsFiles, session);
            }

            // 4. Анализируем JS файлы
            for (Path jsFile : jsFiles) {
                analyzeJsFile(jsFile, htmlFiles, jsFiles, session);
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

    private void analyzeCssFile(Path cssFile, List<Path> htmlFiles, List<Path> jsFiles,
                                AnalysisSession session) throws IOException {
        String content = Files.readString(cssFile);
        List<String> selectors = extractCssSelectors(content);

        // Собираем весь контент для поиска
        String allContent = getAllContent(htmlFiles, jsFiles);

        // Находим используемые и неиспользуемые селекторы
        List<String> unusedSelectors = new ArrayList<>();

        for (String selector : selectors) {
            if (!allContent.contains(selector)) {
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
            fragment.setReason("CSS селектор не найден в HTML/JS файлах");
            fragment.setLineStart(lineNumber);
            fragment.setLineEnd(lineEnd);

            deadCodeFragmentRepository.save(fragment);
        }
    }

    private void analyzeJsFile(Path jsFile, List<Path> htmlFiles, List<Path> jsFiles,
                               AnalysisSession session) throws IOException {
        String content = Files.readString(jsFile);
        List<String> functions = extractJsFunctions(content);
        List<String> variables = extractJsVariables(content);

        // Собираем весь контент для поиска
        String allContent = getAllContent(htmlFiles, jsFiles);

        // Находим неиспользуемые функции
        List<String> unusedFunctions = new ArrayList<>();
        for (String func : functions) {
            if (!allContent.contains(func)) {
                unusedFunctions.add(func);
            }
        }

        // Находим неиспользуемые переменные
        List<String> unusedVariables = new ArrayList<>();
        for (String var : variables) {
            int usageCount = countOccurrences(allContent, var);
            if (usageCount <= 1) {
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

            deadCodeFragmentRepository.save(fragment);
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

            deadCodeFragmentRepository.save(fragment);
        }
    }

    private List<String> extractCssSelectors(String css) {
        Set<String> selectorsSet = new HashSet<>(); // используем Set для уникальности
        // Регулярное выражение для поиска CSS селекторов
        // Ищем текст до {, не включая комментарии и медиа-запросы
        Pattern pattern = Pattern.compile("([^{}]+)\\{");
        Matcher matcher = pattern.matcher(css);

        while (matcher.find()) {
            String selectorPart = matcher.group(1).trim();
            // Разделяем по запятым для множественных селекторов
            String[] parts = selectorPart.split(",");
            for (String part : parts) {
                String selector = part.trim();
                // Пропускаем пустые, медиа-запросы, псевдо-элементы
                if (!selector.isEmpty() &&
                        !selector.startsWith("@") &&
                        !selector.startsWith(":")) {
                    // Берём основной селектор (без псевдоклассов)
                    String mainSelector = selector.split(":")[0].trim();
                    if (!mainSelector.isEmpty()) {
                        selectorsSet.add(mainSelector);
                    }
                }
            }
        }

        return new ArrayList<>(selectorsSet);
    }

    private List<String> extractJsFunctions(String js) {
        List<String> functions = new ArrayList<>();
        // Ищем function name() или const name = function() или name() {}
        Pattern pattern = Pattern.compile("function\\s+(\\w+)\\s*\\(|(?:const|let|var)\\s+(\\w+)\\s*=\\s*function|(\\w+)\\s*\\([^)]*\\)\\s*\\{");
        Matcher matcher = pattern.matcher(js);

        while (matcher.find()) {
            String func = matcher.group(1);
            if (func == null) func = matcher.group(2);
            if (func == null) func = matcher.group(3);
            if (func != null && !func.isEmpty()) {
                functions.add(func);
            }
        }
        return functions;
    }

    private List<String> extractJsVariables(String js) {
        List<String> variables = new ArrayList<>();
        // Ищем объявления переменных
        Pattern pattern = Pattern.compile("(?:const|let|var)\\s+(\\w+)");
        Matcher matcher = pattern.matcher(js);

        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
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
    private void analyzeCssFileImproved(Path cssFile, List<Path> htmlFiles, List<Path> jsFiles,
                                       AnalysisSession session) throws IOException {
        String content = Files.readString(cssFile);
        List<String> selectors = extractCssSelectors(content);

        // Собираем весь контент для поиска
        String allContent = getAllContent(htmlFiles, jsFiles);

        // Находим неиспользуемые селекторы с улучшенной проверкой
        List<String> unusedSelectors = new ArrayList<>();

        for (String selector : selectors) {
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
    private void analyzeJsFileWithAst(Path jsFile, List<Path> htmlFiles, List<Path> jsFiles,
                                     AnalysisSession session) throws IOException {
        String content = Files.readString(jsFile);
        
        // Извлекаем все определения
        Map<String, FunctionInfo> functions = extractFunctionsWithInfo(content);
        Map<String, VariableInfo> variables = extractVariablesWithInfo(content);
        
        // Собираем весь контент для анализа использования
        String allContent = getAllContent(htmlFiles, jsFiles);
        
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
        
        // Проверяем использование переменных
        for (String varName : variables.keySet()) {
            int usageCount = countOccurrences(allContent, varName);
            if (usageCount > 1) { // Больше 1, т.к. 1 раз - это объявление
                usedVariables.add(varName);
            }
        }
        
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
        // Вызывается из HTML (onclick, addEventListener и т.д.)
        if (allContent.contains("onclick=\"" + funcName) || 
            allContent.contains("addEventListener") && allContent.contains(funcName)) {
            return true;
        }
        
        // Экспортируется
        if (fileContent.contains("export") && fileContent.contains(funcName)) {
            return true;
        }
        
        // Вызывается на верхнем уровне (не внутри другой функции)
        Pattern topLevelCall = Pattern.compile("^\\s*" + Pattern.quote(funcName) + "\\s*\\(", Pattern.MULTILINE);
        if (topLevelCall.matcher(fileContent).find()) {
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
        
        // Ищем вызовы других функций внутри этой функции
        for (String otherFunc : allFunctions.keySet()) {
            if (!usedFunctions.contains(otherFunc)) {
                // Проверяем, вызывается ли otherFunc внутри funcName
                if (content.contains(otherFunc + "(")) {
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
        // Анализируем CSS с учётом динамического применения
        for (Path cssFile : cssFiles) {
            analyzeCssWithCoverage(cssFile, htmlFiles, jsFiles, session);
        }

        // Анализируем JS с учётом реального выполнения
        for (Path jsFile : jsFiles) {
            analyzeJsWithCoverage(jsFile, htmlFiles, jsFiles, session);
        }
    }

    /**
     * Анализ CSS с имитацией coverage - учитывает динамическое применение стилей
     */
    private void analyzeCssWithCoverage(Path cssFile, List<Path> htmlFiles, List<Path> jsFiles,
                                       AnalysisSession session) throws IOException {
        String content = Files.readString(cssFile);
        List<String> selectors = extractCssSelectors(content);

        String allContent = getAllContent(htmlFiles, jsFiles);

        List<String> unusedSelectors = new ArrayList<>();
        Map<String, String> usageReasons = new HashMap<>();

        for (String selector : selectors) {
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
        
        // 1. Прямое использование в HTML
        if (allContent.matches("(?s).*class=[\"'][^\"']*\\b" + Pattern.quote(cleanSelector) + "\\b[^\"']*[\"'].*")) {
            return new CoverageResult(true, "Используется в HTML");
        }
        
        if (allContent.matches("(?s).*id=[\"']" + Pattern.quote(cleanSelector) + "[\"'].*")) {
            return new CoverageResult(true, "Используется как ID в HTML");
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
        
        // 5. Проверка на псевдо-классы и медиа-запросы (обычно используются)
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
    private void analyzeJsWithCoverage(Path jsFile, List<Path> htmlFiles, List<Path> jsFiles,
                                      AnalysisSession session) throws IOException {
        String content = Files.readString(jsFile);
        
        Map<String, FunctionInfo> functions = extractFunctionsWithInfo(content);
        Map<String, VariableInfo> variables = extractVariablesWithInfo(content);
        
        String allContent = getAllContent(htmlFiles, jsFiles);
        
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
        
        // Анализ покрытия переменных
        Set<String> coveredVariables = new HashSet<>();
        for (String varName : variables.keySet()) {
            CoverageResult coverage = checkVariableCoverage(varName, allContent, content);
            if (coverage.isUsed) {
                coveredVariables.add(varName);
            }
        }
        
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
        if (allContent.matches("(?s).*on\\w+=[\"'][^\"']*" + Pattern.quote(funcName) + "[^\"']*[\"'].*")) {
            return new CoverageResult(true, "Вызывается из HTML event handler");
        }
        
        // 2. addEventListener
        if (allContent.contains("addEventListener") && allContent.contains(funcName)) {
            return new CoverageResult(true, "Используется в addEventListener");
        }
        
        // 3. Прямой вызов
        Pattern directCall = Pattern.compile("\\b" + Pattern.quote(funcName) + "\\s*\\(");
        if (directCall.matcher(allContent).find()) {
            return new CoverageResult(true, "Вызывается напрямую");
        }
        
        // 4. Экспорт (считается используемым)
        if (fileContent.contains("export") && fileContent.contains(funcName)) {
            return new CoverageResult(true, "Экспортируется (внешнее использование)");
        }
        
        // 5. Callback функции
        if (allContent.matches("(?s).*(?:then|catch|finally|map|filter|forEach|reduce)\\s*\\([^)]*" + Pattern.quote(funcName) + "[^)]*\\).*")) {
            return new CoverageResult(true, "Используется как callback");
        }
        
        // 6. setTimeout/setInterval
        if (allContent.contains("setTimeout") && allContent.contains(funcName) ||
            allContent.contains("setInterval") && allContent.contains(funcName)) {
            return new CoverageResult(true, "Используется в таймере");
        }
        
        // 7. Глобальный scope (функции верхнего уровня могут вызываться извне)
        if (fileContent.matches("(?s).*^function\\s+" + Pattern.quote(funcName) + "\\s*\\(.*")) {
            return new CoverageResult(true, "Глобальная функция (потенциально используется)");
        }
        
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