package dev.coms4156.project.logprocessor.controller;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@Import(AnalyticsControllerTest.MockConfig.class)
class AnalyticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LogService logService;

  /** Modern Spring Boot 3.4+ mock configuration (replaces @MockBean). */
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

  /** Retrieve top endpoints with normal response and populated data. */
  @Test
  void testGetTopEndpointsReturnsDataSuccessfully() throws Exception {
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

  /** Service returns an empty list. */
  @Test
  void testGetTopEndpointsEmptyList() throws Exception {
    when(logService.getTopEndpoints()).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/analytics/top-endpoints"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

    verify(logService, times(1)).getTopEndpoints();
  }
}
