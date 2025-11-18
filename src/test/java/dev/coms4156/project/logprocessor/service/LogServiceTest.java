package dev.coms4156.project.logprocessor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.logprocessor.model.LogEntry;
import dev.coms4156.project.logprocessor.repository.LogEntryRepository;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LogServiceTest {

  private LogEntryRepository repo;
  private LogService service;

  @BeforeEach
  void setUp() {
    repo = mock(LogEntryRepository.class);
    service = new LogService(repo);
  }

  @Test
  void testClientExistsTrue() {
    when(repo.existsByClientId("abc")).thenReturn(true);
    assertTrue(service.clientExists("abc"));
    verify(repo).existsByClientId("abc");
  }

  @Test
  void testClientExistsFalse() {
    when(repo.existsByClientId("xyz")).thenReturn(false);
    assertFalse(service.clientExists("xyz"));
    verify(repo).existsByClientId("xyz");
  }

  @Test
  void testGetTopEndpoints() {
    List<Object[]> mockResult = new ArrayList<>();
    mockResult.add(new Object[]{"endpoint1", 5L});

    when(repo.findTopEndpoints()).thenReturn(mockResult);

    Object result = service.getTopEndpoints();

    assertEquals(mockResult, result);
    verify(repo).findTopEndpoints();
  }

  @Test
  void testCountStatusCodesForClient() {
    List<Object[]> rows = new ArrayList<>();
    rows.add(new Object[]{200, 3L});
    rows.add(new Object[]{404, 1L});

    when(repo.countStatusCodesByClientId("clientA")).thenReturn(rows);

    Map<Integer, Integer> result = service.countStatusCodesForClient("clientA");

    assertEquals(2, result.size());
    assertEquals(3, result.get(200));
    assertEquals(1, result.get(404));
    verify(repo).countStatusCodesByClientId("clientA");
  }

  @Test
  void testProcessLogFileParsesAndSaves() throws Exception {
    String logLine = "XXX.0.0.1 - - [12/Oct/2025:06:25:24 +0000] \"GET /home HTTP/1.1\" 200 512";
    String rawTimestamp = "12/Oct/2025:06:25:24 +0000";
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
    OffsetDateTime odt = OffsetDateTime.parse(rawTimestamp, dtf);
    LocalDateTime expectedTimestamp = odt.toLocalDateTime();
    String expectedTimestampString = expectedTimestamp.toString();  // ISO-8601 string

    InputStream stream = new ByteArrayInputStream(logLine.getBytes());
    service.processLogFile(stream, "client123");

    ArgumentCaptor<LogEntry> captor = ArgumentCaptor.forClass(LogEntry.class);
    verify(repo).save(captor.capture());

    LogEntry entry = captor.getValue();
    assertEquals("client123", entry.getClientId());
    assertEquals("XXX.0.0.1", entry.getIpAddress());
    assertEquals("GET", entry.getMethod());
    assertEquals("/home", entry.getEndpoint());
    assertEquals(200, entry.getStatusCode());
    assertEquals(512L, entry.getResponseSize());
    assertEquals(expectedTimestamp, entry.getTimestamp());
    assertEquals(expectedTimestampString, entry.getTimestampString());
    assertNotNull(entry.getTimestamp());
  }

  @Test
  void testProcessLogFileBadTimestamp() throws Exception {
    String logLine = "XXX.0.0.1 - - [12/Oct/2025:06:25:24] \"GET /home HTTP/1.1\" 200 512";
    InputStream stream = new ByteArrayInputStream(logLine.getBytes());

    verify(repo, never()).save(any());
        // Act & Assert
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.processLogFile(stream, "badTimestampClient")
    );
    verify(repo, never()).save(any());

    assertTrue(
        exception.getMessage().contains("Failed to parse timestamp")
    );
  }

  @Test
  void testProcessLogFileHandlesEmptyStream() throws Exception {
    InputStream stream = new ByteArrayInputStream(new byte[0]);
    service.processLogFile(stream, "client456");
    verify(repo, never()).save(any());
  }

  @Test
  void testProcessLogFileInvalidFormatDoesNotSave() throws Exception {
    String invalidLine = "not a valid log";
    InputStream stream = new ByteArrayInputStream(invalidLine.getBytes());
    service.processLogFile(stream, "client789");
    verify(repo, never()).save(any());
  }

  @Test
  void testProcessLogFileWithDashSize() throws Exception {
    String logLine = "127.0.0.1 - - [12/Oct/2025:06:25:24 +0000] \"GET /page HTTP/1.1\" 404 -";
    InputStream stream = new ByteArrayInputStream(logLine.getBytes());

    service.processLogFile(stream, "client999");

    ArgumentCaptor<LogEntry> captor = ArgumentCaptor.forClass(LogEntry.class);
    verify(repo).save(captor.capture());

    LogEntry entry = captor.getValue();
    assertEquals(0L, entry.getResponseSize());
  }

  @Test
  void testGetRequestCountsByHour() {
    List<Object[]> mockRows = new ArrayList<>();
    mockRows.add(new Object[]{LocalDateTime.of(2025, 10, 20, 13, 0), 5L});
    mockRows.add(new Object[]{LocalDateTime.of(2025, 10, 20, 14, 0), 2L});

    when(repo.countRequestsByHour("clientA")).thenReturn(mockRows);

    Map<String, Integer> result = service.getRequestCountsByHour("clientA");

    assertEquals(2, result.size());
    assertEquals(5, result.get("2025-10-20 13:00:00"));
    assertEquals(2, result.get("2025-10-20 14:00:00"));
    verify(repo).countRequestsByHour("clientA");
  }

  @Test
  void testGetErrorCountsByHour() {
    List<Object[]> mockRows = new ArrayList<>();
    mockRows.add(new Object[]{LocalDateTime.of(2025, 10, 20, 13, 0), 3L, 1L});
    mockRows.add(new Object[]{LocalDateTime.of(2025, 10, 20, 14, 0), 2L, 0L});

    when(repo.countErrorCodesByHour("clientA")).thenReturn(mockRows);

    Map<String, Map<String, Integer>> result = service.getErrorCountsByHour("clientA");

    assertEquals(2, result.size());
    assertEquals(3, result.get("2025-10-20 13:00:00").get("4xx"));
    assertEquals(1, result.get("2025-10-20 13:00:00").get("5xx"));
    assertEquals(2, result.get("2025-10-20 14:00:00").get("4xx"));
    assertEquals(0, result.get("2025-10-20 14:00:00").get("5xx"));
    verify(repo).countErrorCodesByHour("clientA");
  }
}
