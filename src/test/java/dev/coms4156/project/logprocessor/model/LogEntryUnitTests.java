package dev.coms4156.project.logprocessor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * This class contains the unit tests for the LogEntry class.
 */
public class LogEntryUnitTests {

  private static final String TEST_IP_ADDRESS = "test.ip.address";

  @Test
  void noArgConstructorDefaultValues() {
    LogEntry logEntry = new LogEntry();

    assertNull(logEntry.getId());
    assertNull(logEntry.getClientId());
    assertNull(logEntry.getIpAddress());
    assertNull(logEntry.getMethod());
    assertNull(logEntry.getEndpoint());
    assertEquals(0, logEntry.getStatusCode());
    assertEquals(0, logEntry.getResponseSize());
    assertNull(logEntry.getTimestamp());
  }

  @Test
  void parameterizedConstructorWithAllFields() {
    String clientId = "testClient";
    String ipAddress = TEST_IP_ADDRESS;
    String method = "GET";
    String endpoint = "/api/endpoint1";
    int statusCode = 200;
    long responseSize = 1024;
    LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
    LogEntry logEntry = new LogEntry(clientId,
            ipAddress, method, endpoint, statusCode, responseSize, timestamp);
    final LocalDateTime expectedHourWindow = 
          timestamp.truncatedTo(java.time.temporal.ChronoUnit.HOURS);

    assertNull(logEntry.getId());
    assertEquals(clientId, logEntry.getClientId());
    assertEquals(ipAddress, logEntry.getIpAddress());
    assertEquals(method, logEntry.getMethod());
    assertEquals(endpoint, logEntry.getEndpoint());
    assertEquals(statusCode, logEntry.getStatusCode());
    assertEquals(responseSize, logEntry.getResponseSize());
    assertEquals(timestamp, logEntry.getTimestamp());
    assertEquals(expectedHourWindow, logEntry.getHourWindow());
  }
}