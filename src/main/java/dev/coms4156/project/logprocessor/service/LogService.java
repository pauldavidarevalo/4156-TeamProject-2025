package dev.coms4156.project.logprocessor.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//new imports
import org.springframework.stereotype.Service;

import dev.coms4156.project.logprocessor.model.LogEntry;
import dev.coms4156.project.logprocessor.repository.LogEntryRepository;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



@Service
public class LogService {

    /**
    private final Path logsDir;

    public LogService() {
        // Expect a pre-existing 'logs' directory relative to working directory.
        this.logsDir = Paths.get("logs");
    }

    /**
     * Save a whole uploaded multipart file to the logs directory. This method expects the directory to already exist. If a file with
     * the same
     * name already exists, returns error. Otherwise, returns the stored filename relative to the logs directory.

    public String saveUploadedFile(org.springframework.web.multipart.MultipartFile file) throws IOException {
        validateLogsDir();

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.log";
        Path target = logsDir.resolve(original).normalize();

        if (Files.exists(target)) {
            throw new IOException("A file with the name '" + original + "' already exists.");
        }

        try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(target.toFile())) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        }

        return target.getFileName().toString();
    }

    /**
     * Save the provided raw text as a new file named by timestamp. Returns the
     * filename used.

    public String saveRawAsFile(String raw) throws IOException {
        validateLogsDir();
        String filename = String.format("upload-%d.log", System.currentTimeMillis());
        Path target = logsDir.resolve(filename);
        Files.writeString(target, raw != null ? raw : "");
        return filename;
    }

    public List<String> listLogFiles() throws IOException {
        validateLogsDir();
        List<String> files = new ArrayList<>();
        Files.list(logsDir).forEach(p -> files.add(p.getFileName().toString()));
        return files;
    }

    public String readLogFile(String filename) throws IOException {
        validateLogsDir();
        Path p = logsDir.resolve(filename);
        if (!Files.exists(p)) {
            throw new IOException("Log file not found: " + filename);
        }
        return new String(Files.readAllBytes(p));
    }

    private void validateLogsDir() throws IOException {
        if (!Files.exists(logsDir) || !Files.isDirectory(logsDir)) {
            throw new IOException("Logs directory does not exist. Please create a 'logs' directory in the working directory.");
        }
    }
     */
    private final LogEntryRepository repo;

    public LogService(LogEntryRepository repo) {
        this.repo = repo;
    }

    // Simple Apache log pattern (combined format)
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[(.+?)\\] \"(\\S+) (\\S+) \\S+\" (\\d{3}) (\\d+|-)"
    );

    public void processLogFile(InputStream fileStream, String clientId) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    String time = matcher.group(2);
                    String method = matcher.group(3);
                    String endpoint = matcher.group(4);
                    int status = Integer.parseInt(matcher.group(5));
                    long size = matcher.group(6).equals("-") ? 0 : Long.parseLong(matcher.group(6));

                    // Simplified timestamp parsing
                    LocalDateTime timestamp = LocalDateTime.now();

                    LogEntry entry = new LogEntry(clientId, ip, method, endpoint, status, size, timestamp);
                    repo.save(entry);
                }
            }
        }
    }

    public Object getTopEndpoints() {
        return repo.findTopEndpoints();
    }

}