package dev.coms4156.project.logprocessor.service;

import dev.coms4156.project.logprocessor.model.LogEntry;
import dev.coms4156.project.logprocessor.repository.LogEntryRepository;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Service layer for processing log files and querying log data.
 * Each method here may be called by a controller to handle specific
 * endpoints.
 */
@Service
public class LogService {
  private final LogEntryRepository repo;

  public LogService(LogEntryRepository repo) {
    this.repo = repo;
  }

  // Simple Apache log pattern (combined format)
  private static final Pattern LOG_PATTERN = Pattern.compile(
      "^(\\S+) \\S+ \\S+ \\[(.+?)\\] \"(\\S+) (\\S+) \\S+\" (\\d{3}) (\\d+|-)");

  /**
   * Major unit of code.
   * Parses each log file extracting each component and creates a new LogEntry
   * object.
   * That LogEntry is saved into a LogEntryRepository which transfers its data
   * into the
   * database.
   *
   * @param fileStream input stream of the file given by the client
   * @param clientId   ID input by the client
   * @throws Exception thrown if input stream cannot be read
   */
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
          long size = "-".equals(matcher.group(6)) ? 0 : Long.parseLong(matcher.group(6));

          OffsetDateTime offsetDateTime;
          try {
            DateTimeFormatter dtf =
                  DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            offsetDateTime = OffsetDateTime.parse(time, dtf);
          } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse timestamp: " + time, e);
          }
          // drops timezone
          LocalDateTime timestamp = offsetDateTime.toLocalDateTime();

          LogEntry entry = new LogEntry(clientId, ip, method, endpoint, status, size, timestamp);
          repo.save(entry);
        }
      }
    }
  }

  /**
   * Deletes all log entries from the database for a specific clientId.
   */
  public void resetClientLogs(String clientId) {
    repo.deleteByClientId(clientId);
  }

  /**
   * Sample method analytics to be replaced by security endpoint features.
   *
   * @return array sorted by frequency descending (most to least) endpoints
   */
  public Object getTopEndpoints() {
    return repo.findTopEndpoints();
  }

  /**
   * Major unit of code.
   * Count status codes for entries matching the provided clientId.
   * Returns a mapping from status code to count.
   */
  public Map<Integer, Integer> countStatusCodesForClient(String clientId) {
    Map<Integer, Integer> result = new HashMap<>();
    List<Object[]> rows = repo.countStatusCodesByClientId(clientId);
    for (Object[] row : rows) {
      Integer status = (Integer) row[0];
      Long count = (Long) row[1];
      result.put(status, count.intValue());
    }
    return result;
  }

  /**
   * Returns true if at least one LogEntry exists for the provided clientId.
   */
  public boolean clientExists(String clientId) {
    return repo.existsByClientId(clientId);
  }

  // this function is debugged/fixed with chatgpt generated code
  /**
   * Major unit of code.
   * Returns a mapping of hour → request count for a given clientId.
   * Example:
   * {
   * "2025-10-20T13:00:00": 15,
   * "2025-10-20T14:00:00": 28
   * }
   */
  public Map<String, Integer> getRequestCountsByHour(String clientId) {
    List<Object[]> rows = repo.countRequestsByHour(clientId);
    Map<String, Integer> result = new LinkedHashMap<>();

    for (Object[] row : rows) {
      Object hourObj = row[0];
      String hourStr = hourObj.toString();
      Number count = (Number) row[1]; // Works for Integer or Long
      result.put(hourStr, count.intValue());
    }

    return result;
  }

  // this function is debugged/fixed with chatgpt generated code
  /**
   * Major unit of code.
   * Returns a mapping of hour → {4xx, 5xx} error counts system-wide.
   * Example:
   * {
   * "2025-10-20T13:00:00": {"4xx": 3, "5xx": 1}
   * }
   */
  public Map<String, Map<String, Integer>> getErrorCountsByHour(String clientId) {
    List<Object[]> rows = repo.countErrorCodesByHour(clientId);
    Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
    for (Object[] row : rows) {
      Object hourObj = row[0];
      String hourStr = hourObj.toString();

      Long count4xx = ((Number) row[1]).longValue();
      Long count5xx = ((Number) row[2]).longValue();

      Map<String, Integer> inner = new HashMap<>();
      inner.put("4xx", count4xx.intValue());
      inner.put("5xx", count5xx.intValue());
      result.put(hourStr, inner);
    }

    return result;
  }

  /**
   * Major unit of code.
   * Returns a map of IP addresses with their corresponding hour windows and
   * counts of
   * authentication errors.
   * Example:
   * [
   * {"ipAddress": "192.168.1.1", "hourWindow": "2025-10-20T13:00:00",
   * "errorCount": 7}
   * ]
   */
  public List<Map<String, Object>> getIpsWithManyAuthErrors(String clientId) {
    int threshold = 5;
    List<Object[]> results = repo.findIpsWithManyAuthErrors(threshold, clientId);
    return results.stream()
        .map(row -> Map.of(
            "ipAddress", row[0],
            "hourWindow", row[1].toString(),
            "errorCount", row[2]))
        .toList();
  }

  /**
   * Major unit of code.
   * Performs endpoint-level health detection for a given client.
   *
   * For each (endpoint, hourWindow) pair, this method:
   * 1. Computes total requests
   * 2. Computes total error requests (4xx + 5xx)
   * 3. Derives an error rate
   * 4. Flags the endpoint as UNHEALTHY if thresholds are exceeded
   *
   * This transforms raw log data into an actionable health signal,
   * rather than returning simple counts.
   *
   * Example output:
   * [
   *   {
   *     "endpoint": "/login",
   *     "hourWindow": "2025-10-20T14:00:00",
   *     "totalRequests": 50,
   *     "errorCount": 20,
   *     "errorRate": 0.4,
   *     "status": "UNHEALTHY"
   *   }
   * ]
   */
  public List<Map<String, Object>> getEndpointHealth(String clientId) {

    // Thresholds defining unhealthy behavior
    final double ERROR_RATE_THRESHOLD = 0.30; // 30%
    final int MIN_REQUEST_THRESHOLD = 10;

    List<Object[]> rows = repo.countEndpointHealthByHour(clientId);

    return rows.stream()
            // Ignore low-traffic endpoint windows to try to avoid noisy false positives
            .filter(row -> ((Number) row[2]).intValue() >= MIN_REQUEST_THRESHOLD)
            .map(row -> {
              String endpoint = (String) row[0];
              String hourWindow = row[1].toString();
              int totalRequests = ((Number) row[2]).intValue();
              int errorCount = ((Number) row[3]).intValue();

              double errorRate =
                      totalRequests == 0 ? 0.0 : (double) errorCount / totalRequests;

              boolean unhealthy =
                      errorRate >= ERROR_RATE_THRESHOLD;

              Map<String, Object> result = new HashMap<>();
              result.put("endpoint", endpoint);
              result.put("hourWindow", hourWindow);
              result.put("totalRequests", totalRequests);
              result.put("errorCount", errorCount);
              result.put("errorRate",
                      Math.round(errorRate * 100.0) / 100.0);
              result.put("status", unhealthy ? "UNHEALTHY" : "HEALTHY");

              return result;
            })
            // Only return the unhealthy problematic endpoints
            .filter(m -> "UNHEALTHY".equals(m.get("status")))
            .toList();
  }
}