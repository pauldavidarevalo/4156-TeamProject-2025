package dev.coms4156.project.logprocessor.controller;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.Arrays;
import java.util.Collections;
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

@WebMvcTest(SecurityController.class)
@Import(SecurityControllerTest.MockConfig.class)
class SecurityControllerTest {

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

  /** Test /security/suspicious-ips endpoint. */
  @Test
  void testGetSuspiciousIps_ReturnsDataSuccessfully() throws Exception {
    List<Map<String, Object>> mockData = Arrays.asList(
        new LinkedHashMap<>() {{
            put("ipAddress", "192.168.1.1");
            put("hourWindow", "2025-10-22 12:00:00");
            put("count", 10L);
        }},
        new LinkedHashMap<>() {{
            put("ipAddress", "10.0.0.1");
            put("hourWindow", "2025-10-22 13:00:00");
            put("count", 7L);
        }}
    );

    when(logService.getIpsWithManyAuthErrors()).thenReturn(mockData);

    mockMvc.perform(get("/security/suspicious-ips"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                  {"ipAddress":"192.168.1.1","hourWindow":"2025-10-22 12:00:00","count":10},
                  {"ipAddress":"10.0.0.1","hourWindow":"2025-10-22 13:00:00","count":7}
                ]
            """));

    verify(logService, times(1)).getIpsWithManyAuthErrors();
  }

  /** Test /security/suspicious-ips endpoint with no data. */
  @Test
  void testGetSuspiciousIps_ReturnsEmptyWhenNoData() throws Exception {
    when(logService.getIpsWithManyAuthErrors()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/security/suspicious-ips"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

    verify(logService, times(1)).getIpsWithManyAuthErrors();
  }
}
