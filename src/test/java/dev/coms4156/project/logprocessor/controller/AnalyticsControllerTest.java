package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import(AnalyticsControllerTest.MockConfig.class)
class AnalyticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LogService logService;

  /** Modern Spring Boot 3.4+ mock configuration (replaces @MockBean) */
  static class MockConfig {
    @Bean
    LogService logService() {
      return Mockito.mock(LogService.class);
    }
  }

  /** Reset the mock between tests so previous interactions don’t interfere. */
  @BeforeEach
  void resetMocks() {
    reset(logService);
  }

  /** ✅ Test normal response with populated data */
  @Test
  void testGetTopEndpoints_ReturnsDataSuccessfully() throws Exception {
    List<Object[]> mockData = new ArrayList<>();
    mockData.add(new Object[]{"endpoint1", 10L});
    mockData.add(new Object[]{"endpoint2", 5L});
    when(logService.getTopEndpoints()).thenReturn(mockData);

    mockMvc.perform(get("/analytics/top-endpoints"))
            .andExpect(status().isOk())
            // Compare against JSON, not Java toString()
            .andExpect(content().json("[[\"endpoint1\",10],[\"endpoint2\",5]]"));

    verify(logService, times(1)).getTopEndpoints();
  }

  /** ✅ Test when the service returns an empty list */
  @Test
  void testGetTopEndpoints_EmptyList() throws Exception {
    when(logService.getTopEndpoints()).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/analytics/top-endpoints"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

    verify(logService, times(1)).getTopEndpoints();
  }
}
