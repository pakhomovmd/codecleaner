@echo off
echo ========================================
echo Rebuilding CodeCleaner Project
echo ========================================

echo.
echo [1/4] Building Angular frontend...
cd codecleaner-ui
call npm run build
if %errorlevel% neq 0 (
    echo ERROR: Angular build failed!
    pause
    exit /b 1
)

echo.
echo [2/4] Clearing old static files...
cd ..
rmdir /s /q codecleaner\src\main\resources\static 2>nul
mkdir codecleaner\src\main\resources\static

echo.
echo [3/4] Copying new static files...
xcopy /s /e /y codecleaner-ui\dist\codecleaner-ui\browser\* codecleaner\src\main\resources\static\

echo.
echo [4/4] Building Spring Boot JAR...
cd codecleaner
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build completed successfully!
echo JAR location: codecleaner\target\codecleaner-0.0.1-SNAPSHOT.jar
echo ========================================
echo.
echo To run: java -jar codecleaner\target\codecleaner-0.0.1-SNAPSHOT.jar --server.port=8080
pause
