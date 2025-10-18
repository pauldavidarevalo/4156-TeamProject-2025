package dev.coms4156.project.logprocessor.service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final Path logsDir;
    private final Path defaultLogFile;

    public LogService() {
        // Store logs in a folder named "logs" in the working directory
        this.logsDir = Paths.get("logs");
        this.defaultLogFile = logsDir.resolve("apache.log");
        // Defer creation to methods to avoid throwing during bean construction
        try {
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }
            if (!Files.exists(defaultLogFile)) {
                Files.createFile(defaultLogFile);
            }
        } catch (IOException e) {
            // If creation fails, we'll attempt again lazily when writing/reading
            System.err.println("Warning: could not create logs directory or file: " + e.getMessage());
        }
    }

    /**
     * Appends a single line (or multiple lines) representing an apache log entry to
     * the default
     * log file. If the provided content contains newlines, they will be preserved.
     *
     * @param logContent the log content to append
     * @throws IOException when writing fails
     */
    public void appendToDefaultLog(String logContent) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(defaultLogFile.toFile(), true))) {
            writer.write(logContent);
            writer.newLine();
        }
    }

    /**
     * Save a whole uploaded multipart file to the logs directory. If a file with
     * the same name
     * already exists, the method will append a timestamp to the filename to avoid
     * clobbering.
     * Returns the stored filename relative to the logs directory.
     */
    public String saveUploadedFile(org.springframework.web.multipart.MultipartFile file) throws IOException {
        ensureLogsDirExists();
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.log";
        Path target = logsDir.resolve(original).normalize();
        if (Files.exists(target)) {
            String base = original;
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot > 0) {
                base = original.substring(0, dot);
                ext = original.substring(dot);
            }
            String candidate;
            do {
                candidate = String.format("%s-%d%s", base, System.currentTimeMillis(), ext);
                target = logsDir.resolve(candidate);
            } while (Files.exists(target));
        }

        try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(target.toFile())) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        return target.getFileName().toString();
    }

    /**
     * Save the provided raw text as a new file named by timestamp.
     * Returns the filename used.
     */
    public String saveRawAsFile(String raw) throws IOException {
        ensureLogsDirExists();
        String filename = String.format("upload-%d.log", System.currentTimeMillis());
        Path target = logsDir.resolve(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(target.toFile(), false))) {
            writer.write(raw != null ? raw : "");
            writer.newLine();
        }
        return filename;
    }

    private void ensureLogsDirExists() throws IOException {
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }
        if (!Files.exists(defaultLogFile)) {
            Files.createFile(defaultLogFile);
        }
    }

    public List<String> listLogFiles() {
        List<String> files = new ArrayList<>();
        try {
            Files.list(logsDir).forEach(p -> files.add(p.getFileName().toString()));
        } catch (IOException e) {
            // ignore and return empty list
        }
        return files;
    }

    public String readLogFile(String filename) throws IOException {
        Path p = logsDir.resolve(filename);
        if (!Files.exists(p) || Files.isDirectory(p)) {
            throw new IOException("Log file not found: " + filename);
        }
        return new String(Files.readAllBytes(p));
    }
}
