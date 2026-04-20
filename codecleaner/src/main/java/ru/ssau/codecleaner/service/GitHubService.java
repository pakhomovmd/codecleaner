package ru.ssau.codecleaner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GitHubService {

    /**
     * Клонирует GitHub репозиторий и создает ZIP архив
     * @param repoUrl URL репозитория (например: https://github.com/user/repo)
     * @return MultipartFile с ZIP архивом
     */
    public MultipartFile cloneAndZipRepository(String repoUrl) throws IOException, InterruptedException {
        // Создаем временную директорию для клонирования
        Path tempDir = Files.createTempDirectory("github_clone_");
        Path zipPath = null;

        try {
            // Извлекаем имя репозитория из URL
            String repoName = extractRepoName(repoUrl);
            Path repoPath = tempDir.resolve(repoName);

            // Клонируем репозиторий через git clone
            ProcessBuilder processBuilder = new ProcessBuilder(
                "git", "clone", "--depth", "1", repoUrl, repoPath.toString()
            );
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Читаем вывод процесса и сохраняем его
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Git: " + line);
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String outputStr = output.toString();
                // Проверяем тип ошибки по выводу
                if (outputStr.contains("Repository not found") || 
                    outputStr.contains("not found")) {
                    throw new IOException("Репозиторий GitHub не найден. Проверьте URL и убедитесь, что репозиторий существует и является публичным.");
                } else if (outputStr.contains("Authentication failed") || 
                           outputStr.contains("Permission denied")) {
                    throw new IOException("Доступ запрещен. Репозиторий может быть приватным или требовать аутентификации.");
                } else {
                    throw new IOException("Не удалось клонировать репозиторий: " + outputStr);
                }
            }

            // Удаляем .git директорию (не нужна для анализа)
            Path gitDir = repoPath.resolve(".git");
            if (Files.exists(gitDir)) {
                deleteDirectory(gitDir);
            }

            // Создаем ZIP архив
            zipPath = tempDir.resolve(repoName + ".zip");
            zipDirectory(repoPath, zipPath);

            // Конвертируем в MultipartFile
            byte[] zipBytes = Files.readAllBytes(zipPath);
            return new InMemoryMultipartFile(repoName + ".zip", zipBytes);

        } finally {
            // Очищаем временные файлы
            if (zipPath != null && Files.exists(zipPath)) {
                Files.deleteIfExists(zipPath);
            }
            deleteDirectory(tempDir);
        }
    }

    /**
     * Извлекает имя репозитория из URL
     */
    private String extractRepoName(String repoUrl) {
        // https://github.com/user/repo -> repo
        // https://github.com/user/repo.git -> repo
        String[] parts = repoUrl.split("/");
        String repoName = parts[parts.length - 1];
        if (repoName.endsWith(".git")) {
            repoName = repoName.substring(0, repoName.length() - 4);
        }
        return repoName;
    }

    /**
     * Создает ZIP архив из директории
     */
    private void zipDirectory(Path sourceDir, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(sourceDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        String zipEntryName = sourceDir.relativize(path).toString();
                        zos.putNextEntry(new ZipEntry(zipEntryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        }
    }

    /**
     * Рекурсивно удаляет директорию
     */
    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted((a, b) -> -a.compareTo(b)) // Обратный порядок для удаления файлов перед папками
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Игнорируем ошибки удаления
                    }
                });
        }
    }

    /**
     * Реализация MultipartFile для in-memory файла
     */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        public InMemoryMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return "application/zip";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}
