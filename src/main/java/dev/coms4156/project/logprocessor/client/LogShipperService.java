package dev.coms4156.project.logprocessor.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * This file represents the LogShipperService class for the client.
 * Some functionality related to saving and loading position files
 * was developed with ChatGPT.
 */
@Service
public class LogShipperService {

  private static final Logger log = LoggerFactory.getLogger(LogShipperService.class);
  private final LogShipperConfig config;
  private final RestTemplate restTemplate = new RestTemplate();
  private long lastPosition = 0L;
  private final Path positionFile = Paths.get("logshipper-position.txt");

  public LogShipperService(LogShipperConfig config) {
    this.config = config;
  }

  /**
   * This method ships client logs to the database.
   */
  @Scheduled(fixedDelayString = "${logshipper.interval-ms}")
  public void shipLogs() {
    if (!config.isEnabled()) {
      return;
    }

    Path logFile = Paths.get(config.getLogFile());
    if (!Files.exists(logFile)) {
      log.warn("Log file not found: {}", logFile);
      return;
    }

    List<String> newLines = readNewLines(logFile);
    if (newLines.isEmpty()) {
      return;
    }

    if (upload(newLines)) {
      savePosition();
      if (log.isInfoEnabled()) {
        log.info("Successfully shipped {} lines from client {}",
                newLines.size(),
                config.getClientId());
      }
    } else {
      if (log.isErrorEnabled()) {
        log.error("Failed to upload {} lines — will retry later", newLines.size());
      }
    }
  }

  private List<String> readNewLines(Path file) {
    List<String> lines = new ArrayList<>();
    try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
      raf.seek(lastPosition);
      String line;
      while ((line = raf.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          lines.add(line);
        }
      }
      long newPosition = raf.getFilePointer();
      if (Files.size(file) < newPosition) {
        log.info("Log rotation detected — starting from beginning");
        lastPosition = 0;
      } else {
        lastPosition = newPosition;
      }
    } catch (IOException e) {
      log.error("Error reading log file: {}", file, e);
    }
    return lines;
  }

  private boolean upload(List<String> lines) {
    if (lines.isEmpty()) {
      return true;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("clientId", config.getClientId());
    body.add("file", new ByteArrayResource(String.join("\n", lines).getBytes()) {
      @Override
      public String getFilename() {
        return "access.log";
      }
    });

    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
                config.getUrl(),
                HttpMethod.POST,
                request,
                String.class
      );
      return response.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Upload failed: {}", e.getMessage());
      }
      return false;
    }
  }

  private void savePosition() {
    try {
      Files.writeString(positionFile, String.valueOf(lastPosition));
    } catch (IOException e) {
      log.error("Failed to save position file", e);
    }
  }

  /**
   * This method loads the position file for the client.
   */
  public void loadPosition() {
    if (Files.exists(positionFile)) {
      try {
        String content = Files.readString(positionFile).trim();
        lastPosition = content.isEmpty() ? 0L : Long.parseLong(content);
        log.info("Loaded last position: {}", lastPosition);
      } catch (Exception e) {
        log.warn("Failed to load position, starting from 0", e);
        lastPosition = 0L;
      }
    }
  }

  public LogShipperConfig getConfig() {
    return config;
  }
}