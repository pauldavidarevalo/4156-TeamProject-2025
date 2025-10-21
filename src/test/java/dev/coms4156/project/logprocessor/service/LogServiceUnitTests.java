package dev.coms4156.project.logprocessor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.logprocessor.model.LogEntry;
import dev.coms4156.project.logprocessor.repository.LogEntryRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This class contains the unit tests for the LogService class.
 */
@ExtendWith(MockitoExtension.class)
public class LogServiceUnitTests {

    private static final String TEST_IP_ADDRESS = "test.ip.address";

    @Mock
    private LogEntryRepository repo;

    @InjectMocks
    private LogService logService;

    @Test
    void processLogFileWithValidLogLines() throws Exception {
        String clientId = "testClient";
        String logLine = TEST_IP_ADDRESS + " - - [01/Jan/2025:12:00:00 +0000] \""
                + "GET /api/endpoint1 HTTP/1.1\" 200 1024";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(logLine.getBytes());

        when(repo.save(any(LogEntry.class))).thenReturn(new LogEntry());
        logService.processLogFile(inputStream, clientId);
        verify(repo, times(1)).save(any(LogEntry.class));
    }

    @Test
    void processLogFileWithInvalidLogLines() throws Exception {
        String clientId = "testClient";
        String invalidLogLine = "invalid log line";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(invalidLogLine.getBytes());

        logService.processLogFile(inputStream, clientId);
        verify(repo, times(0)).save(any(LogEntry.class));
    }

    @Test
    void processLogFileWithEmptyLogFile() throws Exception {
        String clientId = "testClient";
        ByteArrayInputStream emptyInputStream = new ByteArrayInputStream("".getBytes());

        logService.processLogFile(emptyInputStream, clientId);
        verify(repo, times(0)).save(any(LogEntry.class));
    }

    @Test
    void processLogFileWhereResponseSizeDashSetsZeroSize() throws Exception {
        String clientId = "testClient";
        String logLine = TEST_IP_ADDRESS + " - - [01/Jan/2025:12:00:00 +0000] \""
                + "GET /api/endpoint1 HTTP/1.1\" 200 -";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(logLine.getBytes());
        when(repo.save(any(LogEntry.class))).thenReturn(new LogEntry());

        logService.processLogFile(inputStream, clientId);
        verify(repo, times(1)).save(any(LogEntry.class));
    }

    @Test
    void processLogFileWithException() throws Exception {
        String clientId = "testClient";
        try (InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Read error");
            }
        }) {
            Exception exception = assertThrows(Exception.class, ()
                    -> logService.processLogFile(inputStream, clientId));
            assertEquals("Read error", exception.getMessage());
            verify(repo, times(0)).save(any(LogEntry.class));
        }
    }

    @Test
    void getTopEndpointsReturnRepoResult() {
        List<Object[]> mockTopEndpoints = Arrays.asList(
                new Object[]{"/api/endpoint1", 100L},
                new Object[]{"/api/endpoint2", 50L}
        );
        when(repo.findTopEndpoints()).thenReturn(mockTopEndpoints);

        Object result = logService.getTopEndpoints();
        assertEquals(mockTopEndpoints, result);
        verify(repo, times(1)).findTopEndpoints();
    }
}