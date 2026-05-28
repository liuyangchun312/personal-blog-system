package com.example.blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BackupService {
    @Value("${blog.backup-dir}")
    private String backupDir;

    public String backup() throws IOException, InterruptedException {
        Files.createDirectories(Path.of(backupDir));
        String file = "backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".sql";
        Path target = Path.of(backupDir, file);
        String host = System.getenv().getOrDefault("MYSQL_HOST", "mysql");
        String user = System.getenv().getOrDefault("MYSQL_USER", "blog");
        String password = System.getenv().getOrDefault("MYSQL_PASSWORD", "blog_pass");
        String database = System.getenv().getOrDefault("MYSQL_DATABASE", "enterprise_blog");
        String dump = "mysqldump -h " + host + " -u" + user + " -p" + password + " " + database + " > " + target;
        var command = java.util.List.of("sh", "-c", dump);
        Process process = new ProcessBuilder(command).inheritIO().start();
        int code = process.waitFor();
        if (code != 0) throw new IllegalStateException("数据库备份失败，请确认容器内存在 mysqldump");
        return target.toString();
    }
}
