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
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

## Установка и запуск

### 1. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE codecleaner;
```

### 2. Настройка Backend

Перейдите в папку `codecleaner` и настройте подключение к БД в `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/codecleaner
spring.datasource.username=postgres
spring.datasource.password=admin
```

Запустите backend:

```bash
cd codecleaner
mvn clean install
mvn spring-boot:run
```

Backend будет доступен на `http://localhost:8080`

### 3. Настройка Frontend

Перейдите в папку `codecleaner-ui` и установите зависимости:

```bash
cd codecleaner-ui
npm install
```

Запустите frontend:

```bash
npm start
```

Frontend будет доступен на `http://localhost:4200`

### 4. Быстрый запуск (Windows)

Используйте скрипт `rebuild.bat` в корне проекта для автоматической сборки и запуска:

```bash
rebuild.bat
```

## Использование

1. **Регистрация/Вход**
   - Откройте `http://localhost:4200`
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

## Разработка

### Backend тесты

```bash
cd codecleaner
mvn test
```

### Frontend тесты

```bash
cd codecleaner-ui
npm test
```

### Сборка production

```bash
# Frontend
cd codecleaner-ui
npm run build

# Backend (включает frontend)
cd ../codecleaner
mvn clean package
```

JAR файл будет создан в `codecleaner/target/codecleaner-0.0.1-SNAPSHOT.jar`

## Лицензия

Учебный проект для курсовой работы по веб-разработке.

## Автор

Курсовая работа по теме "Исследование эффективности методов поиска неиспользуемого кода в веб приложениях"
