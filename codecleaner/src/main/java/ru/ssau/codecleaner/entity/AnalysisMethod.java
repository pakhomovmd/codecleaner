package ru.ssau.codecleaner.entity;

public enum AnalysisMethod {
    SIMPLE_TEXT_SEARCH("Простой текстовый поиск", 
                       "Базовый метод: ищет определения селекторов/функций и проверяет их упоминания в других файлах. Быстрый, но может давать ложные срабатывания при динамической генерации кода."),
    
    AST_ANALYSIS("AST-анализ", 
                 "Продвинутый метод: парсит JavaScript в синтаксическое дерево, строит граф зависимостей между функциями и переменными. Более точный, учитывает scope и реальные вызовы."),
    
    COVERAGE_BASED("Coverage-based анализ",
                   "Имитация инструментов покрытия кода: анализирует паттерны использования, учитывает динамическую загрузку, условную логику и event handlers. Наиболее точный метод.");

    private final String displayName;
    private final String description;

    AnalysisMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
