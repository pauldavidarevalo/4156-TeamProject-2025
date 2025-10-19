package dev.coms4156.project.logprocessor.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final Path logsDir;

    public LogService() {
        // Expect a pre-existing 'logs' directory relative to working directory.
        this.logsDir = Paths.get("logs");
    }

    /**
     * Save a whole uploaded multipart file to the logs directory. This method expects the directory to already exist. If a file with
     * the same
     * name already exists, returns error. Otherwise, returns the stored filename relative to the logs directory.
     */
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
     */
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
}