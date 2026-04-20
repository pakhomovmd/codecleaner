# CodeCleaner

Веб-приложение для анализа неиспользуемого кода (dead code) в проектах на HTML/CSS/JavaScript.

## Описание

CodeCleaner - это система для исследования эффективности различных методов поиска неиспользуемого кода в веб-приложениях. Проект реализует три метода анализа:

- **Simple Text Search** - простой текстовый поиск (аналог PurgeCSS)
- **AST Analysis** - AST-анализ с построением графа зависимостей (аналог Webpack Tree Shaking)
- **Coverage-based** - имитация инструментов покрытия кода (аналог Chrome DevTools Coverage)

## Технологии

**Backend:**
- Spring Boot 4.0.5
- Java 17
- PostgreSQL
- Spring Security + JWT
- JPA/Hibernate

**Frontend:**
- Angular 19
- TypeScript
- Tailwind CSS
- RxJS

## Требования

- Java 17+
- Node.js 18+ (только для разработки frontend)
- PostgreSQL 14+
- Maven 3.8+

## Установка и запуск

### Быстрый старт (Production)

Проект использует **multi-module Maven структуру** - одна команда собирает frontend и backend в единый JAR файл.

#### 1. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE codecleaner;
```

Настройте подключение в `codecleaner/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/codecleaner
spring.datasource.username=postgres
spring.datasource.password=admin
```

#### 2. Сборка проекта

Из корневой директории выполните:

```bash
cd C:\study\webKurs
mvn clean package -DskipTests
```

Эта команда:
- Собирает Angular frontend (`codecleaner-ui`)
- Копирует статические файлы в `codecleaner/target/classes/static/`
- Собирает Spring Boot backend
- Создает единый JAR файл с встроенным frontend

#### 3. Запуск приложения

```bash
java -jar codecleaner\target\codecleaner-0.0.1-SNAPSHOT.jar
```

Или на кастомном порту:

```bash
java -jar codecleaner\target\codecleaner-0.0.1-SNAPSHOT.jar --server.port=9090
```

Приложение будет доступно на `http://localhost:8080` (или указанном порту)

### Разработка (Development)

Для разработки с hot-reload запускайте frontend и backend отдельно:

#### Backend:

```bash
cd codecleaner
mvn spring-boot:run
```

Backend: `http://localhost:8080`

#### Frontend:

```bash
cd codecleaner-ui
npm install
npm start
```

Frontend: `http://localhost:4200`

## Использование

1. **Регистрация/Вход**
   - Откройте `http://localhost:8080`
   - Зарегистрируйтесь или войдите (тестовый пользователь: `test@mail.com` / `123`)

2. **Создание проекта**
   - Создайте новый проект с названием и URL репозитория

3. **Анализ кода**
   - Загрузите ZIP-архив с кодом проекта (HTML/CSS/JS файлы)
   - Выберите метод анализа
   - Дождитесь завершения анализа

4. **Просмотр результатов**
   - Изучите отчеты по файлам
   - Просмотрите найденные фрагменты неиспользуемого кода
   - Оцените "здоровье проекта" (0-100%)

## Роли пользователей

- **VIEWER** - обычный пользователь (создание проектов, запуск анализов)
- **ADMIN** - администратор (доступ к админ-панели, управление пользователями)

## API Endpoints

### Аутентификация
- `POST /api/auth/register` - регистрация
- `POST /api/auth/login` - вход
- `GET /api/auth/me` - получить текущего пользователя

### Проекты
- `GET /api/projects` - список проектов
- `POST /api/projects` - создать проект
- `GET /api/projects/{id}` - получить проект
- `DELETE /api/projects/{id}` - удалить проект

### Анализ
- `POST /api/analysis/upload/{projectId}` - загрузить и проанализировать код
- `GET /api/analysis/{sessionId}` - получить результаты анализа
- `GET /api/analysis/project/{projectId}` - список анализов проекта

### Администрирование
- `GET /api/admin/users` - список пользователей (только ADMIN)
- `DELETE /api/admin/users/{id}` - удалить пользователя (только ADMIN)

## Структура проекта

```
webKurs/
├── codecleaner/              # Backend (Spring Boot)
│   ├── src/main/java/
│   │   └── ru/ssau/codecleaner/
│   │       ├── controller/   # REST контроллеры
│   │       ├── service/      # Бизнес-логика
│   │       ├── entity/       # JPA сущности
│   │       ├── repository/   # Репозитории
│   │       ├── dto/          # Data Transfer Objects
│   │       ├── config/       # Конфигурация
│   │       └── filter/       # JWT фильтры
│   └── pom.xml
├── codecleaner-ui/           # Frontend (Angular)
│   ├── src/app/
│   │   ├── components/       # UI компоненты
│   │   ├── services/         # HTTP сервисы
│   │   └── interceptors/     # HTTP interceptors
│   └── package.json
├── pom.xml                   # Parent POM
└── README.md
```

## Методы анализа

### 1. Simple Text Search
- Простой текстовый поиск определений и упоминаний
- Быстрый, но может давать ложные срабатывания
- Точность: ~75%

### 2. AST Analysis
- Парсинг кода в синтаксическое дерево
- Построение графа зависимостей функций
- Точность: ~82%

### 3. Coverage-based
- Имитация инструментов покрытия кода
- Проверка 15+ паттернов использования
- Точность: ~88%

## Тестирование

### Запуск тестов

Проект включает 13 unit-тестов с использованием **H2 in-memory базы данных** - PostgreSQL не требуется для тестирования.

```bash
mvn test
```

Тесты покрывают:
- Аутентификацию и JWT токены
- CRUD операции с проектами
- Анализ кода (все три метода)
- Управление пользователями (admin функции)

### Отчет о покрытии кода (JaCoCo)

После запуска тестов отчет доступен в:
```
codecleaner/target/site/jacoco/index.html
```

## Особенности проекта

### Multi-module Maven структура
- Parent POM управляет версиями зависимостей
- Frontend автоматически собирается и встраивается в backend JAR
- Единый артефакт для деплоя

### Тестирование с H2
- Тесты работают без внешней БД
- Быстрое выполнение
- Изолированная среда для каждого теста

### Гибкая конфигурация
- Запуск на любом порту через `--server.port`
- Настройка БД через `application.properties`
- Profile-based конфигурация (dev/prod)