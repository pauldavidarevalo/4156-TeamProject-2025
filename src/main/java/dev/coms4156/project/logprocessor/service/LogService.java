package dev.coms4156.project.logprocessor.service;

import java.io.BufferedWriter;
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
   * Appends a single line (or multiple lines) representing an apache log entry to the default
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
