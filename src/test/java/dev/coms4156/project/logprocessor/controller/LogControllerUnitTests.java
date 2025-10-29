package dev.coms4156.project.logprocessor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import dev.coms4156.project.logprocessor.service.LogService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

/**
 * This class contains the unit tests for the LogController class.
 */
@ExtendWith(MockitoExtension.class)
public class LogControllerUnitTests {

  @Mock
  private LogService logService;

  @InjectMocks
  private LogController logController;

  @Test
  void uploadLogSuccessfulProcessing() throws Exception {
    String clientId = "testClient";
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.log",
            "text/plain",
            "log content".getBytes()
    );
    doNothing().when(logService).processLogFile(any(ByteArrayInputStream.class), eq(clientId));

    ResponseEntity<?> result = logController.uploadLog(clientId, file);
    assertEquals("Log file processed successfully.", result.getBody());
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  void uploadLogErrorWithProcessing() throws Exception {
    String clientId = "testClient";
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.log",
            "text/plain",
            "log content".getBytes()
    );
    String errorMessage = "Invalid log format";
    doThrow(new IOException(errorMessage)).when(logService)
            .processLogFile(any(ByteArrayInputStream.class), eq(clientId));

    ResponseEntity<?> result = logController.uploadLog(clientId, file);
    assertEquals("Error processing log file: " + errorMessage, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }
}