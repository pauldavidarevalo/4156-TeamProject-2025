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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  /** Test normal response with populated data. */
  @Test
  void testGetTopEndpoints_ReturnsDataSuccessfully() throws Exception {
    List<Object[]> mockData = new ArrayList<>();
    mockData.add(new Object[] { "endpoint1", 10L });
    mockData.add(new Object[] { "endpoint2", 5L });
    when(logService.getTopEndpoints()).thenReturn(mockData);

    mockMvc.perform(get("/analytics/top-endpoints"))
        .andExpect(status().isOk())
        // Compare against JSON, not Java toString()
        .andExpect(content().json("[[\"endpoint1\",10],[\"endpoint2\",5]]"));

    verify(logService, times(1)).getTopEndpoints();
  }

  /** Test when the service returns an empty list. */
  @Test
  void testGetTopEndpoints_EmptyList() throws Exception {
    when(logService.getTopEndpoints()).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/analytics/top-endpoints"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(logService, times(1)).getTopEndpoints();
  }

  /** Test /analytics/timeseries/requests/{clientId} endpoint. */
  @Test
  void testGetRequestCountsByHour_ReturnsDataSuccessfully() throws Exception {
    Map<String, Integer> mockData = new LinkedHashMap<>();
    mockData.put("2025-10-20 13:00:00", 5);
    mockData.put("2025-10-20 14:00:00", 2);

    when(logService.getRequestCountsByHour("clientA")).thenReturn(mockData);

    mockMvc.perform(get("/analytics/timeseries/requests/clientA"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"2025-10-20 13:00:00\":5,\"2025-10-20 14:00:00\":2}"));

    verify(logService, times(1)).getRequestCountsByHour("clientA");
  }

  /** Test /analytics/timeseries/error-counts endpoint. */
  @Test
  void testGetErrorCountsByHour_ReturnsDataSuccessfully() throws Exception {
    final Map<String, Map<String, Integer>> mockData = new LinkedHashMap<>();
    Map<String, Integer> hour1 = new HashMap<>();
    hour1.put("4xx", 3);
    hour1.put("5xx", 1);
    Map<String, Integer> hour2 = new HashMap<>();
    hour2.put("4xx", 2);
    hour2.put("5xx", 0);
    mockData.put("2025-10-20 13:00:00", hour1);
    mockData.put("2025-10-20 14:00:00", hour2);

    when(logService.getErrorCountsByHour("clientA")).thenReturn(mockData);

    mockMvc.perform(get("/analytics/timeseries/error-counts/clientA"))
        .andExpect(status().isOk())
        .andExpect(content()
            .json("{\"2025-10-20 13:00:00\":{\"4xx\":3,\"5xx\":1}, "
            + "\"2025-10-20 14:00:00\":{\"4xx\":2,\"5xx\":0}}"));

    verify(logService, times(1)).getErrorCountsByHour("clientA");
  }
}
