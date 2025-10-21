package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
@Import(LogControllerTest.MockConfig.class)
class LogControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LogService logService;

  /** ✅ Replacement for deprecated @MockBean (Spring Boot 3.4+) */
  static class MockConfig {
    @Bean
    LogService logService() {
      return Mockito.mock(LogService.class);
    }
  }

  @BeforeEach
  void resetMocks() {
    reset(logService);
  }

  /** ✅ Test successful file upload */
  @Test
  void testUploadLog_Success() throws Exception {
    MockMultipartFile mockFile =
            new MockMultipartFile("file", "log.txt", "text/plain", "dummy data".getBytes());

    // No exception thrown by service
    doNothing().when(logService).processLogFile(any(), eq("clientA"));

    mockMvc.perform(multipart("/logs/upload")
                    .file(mockFile)
                    .param("clientId", "clientA"))
            .andExpect(status().isOk())
            .andExpect(content().string("Log file processed successfully."));

    verify(logService, times(1)).processLogFile(any(), eq("clientA"));
  }

  /** ✅ Test when LogService throws an exception */
  @Test
  void testUploadLog_Exception() throws Exception {
    MockMultipartFile mockFile =
            new MockMultipartFile("file", "log.txt", "text/plain", "invalid".getBytes());

    doThrow(new RuntimeException("Parsing failed"))
            .when(logService)
            .processLogFile(any(), eq("clientB"));

    mockMvc.perform(multipart("/logs/upload")
                    .file(mockFile)
                    .param("clientId", "clientB"))
            .andExpect(status().isOk()) // Controller always returns 200 with error message
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error processing log file: Parsing failed")));

    verify(logService, times(1)).processLogFile(any(), eq("clientB"));
  }

  /** ✅ Test GET /statusCodeCounts returns OK with data */
  @Test
  void testGetStatusCodeCounts_Found() throws Exception {
    when(logService.clientExists("clientC")).thenReturn(true);

    Map<Integer, Integer> counts = new HashMap<>();
    counts.put(200, 5);
    counts.put(404, 1);
    when(logService.countStatusCodesForClient("clientC")).thenReturn(counts);

    mockMvc.perform(get("/logs/statusCodeCounts")
                    .param("clientId", "clientC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.200").value(5))
            .andExpect(jsonPath("$.404").value(1));

    verify(logService, times(1)).clientExists("clientC");
    verify(logService, times(1)).countStatusCodesForClient("clientC");
  }

  /** ✅ Test GET /statusCodeCounts returns 404 when client not found */
  @Test
  void testGetStatusCodeCounts_NotFound() throws Exception {
    when(logService.clientExists("missingClient")).thenReturn(false);

    mockMvc.perform(get("/logs/statusCodeCounts")
                    .param("clientId", "missingClient"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Error: clientId not found"));

    verify(logService, times(1)).clientExists("missingClient");
    verify(logService, never()).countStatusCodesForClient(any());
  }
}
