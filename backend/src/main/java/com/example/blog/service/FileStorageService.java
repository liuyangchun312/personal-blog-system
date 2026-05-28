package com.example.blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final Path uploadDir;

    public FileStorageService(@Value("${blog.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir);
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty() || !ALLOWED.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 jpg/png/webp/gif 图片");
        }
        Path dayDir = uploadDir.resolve(LocalDate.now().toString());
        Files.createDirectories(dayDir);
        String ext = extension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dayDir.resolve(filename));
        return "/api/uploads/" + LocalDate.now() + "/" + filename;
    }

    private String extension(String name) {
        if (name == null || !name.contains(".")) return ".bin";
        return name.substring(name.lastIndexOf('.')).toLowerCase();
    }
}
