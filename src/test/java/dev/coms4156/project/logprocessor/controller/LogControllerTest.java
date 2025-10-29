package dev.coms4156.project.logprocessor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LogController.class)
@Import(LogControllerTest.MockConfig.class)
class LogControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LogService logService;

  /** Replacement for deprecated @MockBean. */
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

  /** Test successful file upload. */
  @Test
  void testUploadLogSuccess() throws Exception {
    MockMultipartFile mockFile =
            new MockMultipartFile("file", "log.log", "text/plain", "dummy data".getBytes());

    doNothing().when(logService).processLogFile(any(), eq("clientA"));

    mockMvc.perform(multipart("/logs/upload")
                    .file(mockFile)
                    .param("clientId", "clientA"))
            .andExpect(status().isOk())
            .andExpect(content().string("Log file processed successfully."));

    verify(logService, times(1)).processLogFile(any(), eq("clientA"));
  }

  /** Blank clientId should return 400 and error message. */
  @Test
  void testUploadLogMissingClientId() throws Exception {
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "log.log", "text/plain", "dummy data".getBytes());

    mockMvc.perform(multipart("/logs/upload")
          .file(mockFile)
          .param("clientId", "  "))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("clientId cannot be blank."));
  }

  /** Blank file part should return 400 and error message. */
  @Test
  void testUploadLogMissingFilePart() throws Exception {
    MockMultipartFile mockFile =
            new MockMultipartFile("file", "log.log", "text/plain", "".getBytes());

    mockMvc.perform(multipart("/logs/upload")
          .file(mockFile)
          .param("clientId", "clientA"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("file cannot be empty."));
  }

  /** Wrong extension should return 400 and error message. */
  @Test
  void testUploadLogWrongExtension() throws Exception {
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "file.txt", "text/plain", "dummy data".getBytes());

    mockMvc.perform(multipart("/logs/upload")
        .file(mockFile)
        .param("clientId", "clientA"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(org.hamcrest.Matchers.is("only .log files are accepted.")));
  }

  /** LogService throws an exception. */
  @Test
  void testUploadLogException() throws Exception {
    MockMultipartFile mockFile =
            new MockMultipartFile("file", "log.log", "text/plain", "invalid".getBytes());

    doThrow(new RuntimeException("Parsing failed"))
            .when(logService)
            .processLogFile(any(), eq("clientB"));

    mockMvc.perform(multipart("/logs/upload")
                    .file(mockFile)
                    .param("clientId", "clientB"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(
                    org.hamcrest.Matchers.containsString(
                            "Error processing log file: Parsing failed")));

    verify(logService, times(1)).processLogFile(any(), eq("clientB"));
  }

  /** GET /statusCodeCounts returns OK with data. */
  @Test
  void testGetStatusCodeCountsFound() throws Exception {
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

  /** GET /statusCodeCounts returns 404 when client not found. */
  @Test
  void testGetStatusCodeCountsNotFound() throws Exception {
    when(logService.clientExists("missingClient")).thenReturn(false);

    mockMvc.perform(get("/logs/statusCodeCounts")
                    .param("clientId", "missingClient"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Error: clientId not found"));

    verify(logService, times(1)).clientExists("missingClient");
    verify(logService, never()).countStatusCodesForClient(any());
  }
}
