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
  void testGetSuspiciousIpsReturnsDataSuccessfully() throws Exception {
    Map<String, Object> mockMap1 = new LinkedHashMap<>();
    mockMap1.put("ipAddress", "192.168.1.X");
    mockMap1.put("hourWindow", "2025-10-22 12:00:00");
    mockMap1.put("count", 10L);
    mockMap1.put("clientId", "clientA");
    Map<String, Object> mockMap2 = new LinkedHashMap<>();
    mockMap2.put("ipAddress", "10.0.0.X");
    mockMap2.put("hourWindow", "2025-10-22 13:00:00");
    mockMap2.put("count", 7L);
    mockMap2.put("clientId", "clientA");
    List<Map<String, Object>> mockData = new ArrayList<>();
    mockData.add(mockMap1);
    mockData.add(mockMap2);

    when(logService.getIpsWithManyAuthErrors("clientA")).thenReturn(mockData);
    when(logService.clientExists("clientA")).thenReturn(true);

    mockMvc.perform(get("/security/suspicious-ips/clientA"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
                [
                  {"ipAddress":"192.168.1.X","hourWindow":"2025-10-22 12:00:00","count":10},
                  {"ipAddress":"10.0.0.X","hourWindow":"2025-10-22 13:00:00","count":7}
                ]
            """));

    verify(logService, times(1)).getIpsWithManyAuthErrors("clientA");
  }

  /** Test /security/suspicious-ips endpoint with no data. */
  @Test
  void testGetSuspiciousIpsReturnsEmptyWhenNoData() throws Exception {
    when(logService.getIpsWithManyAuthErrors("clientB")).thenReturn(Collections.emptyList());
    when(logService.clientExists("clientB")).thenReturn(true);

    mockMvc.perform(get("/security/suspicious-ips/clientB"))
        .andExpect(status().isOk())
        .andExpect(content().string("No suspicious IPs found"));

    verify(logService, times(1)).getIpsWithManyAuthErrors("clientB");
  }

  /** Test /security/suspicious-ips endpoint with client not existing. */
  @Test
  void testGetSuspiciousIpsReturnsEmptyWhenClientDoesNotExist() throws Exception {
    when(logService.clientExists("clientB")).thenReturn(false);

    mockMvc.perform(get("/security/suspicious-ips/clientB"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Error: clientId not found"));

    verify(logService, times(0)).getIpsWithManyAuthErrors("clientB");
  }

  @Test
  void testGetSuspiciousIpsEmptyDatabaseReturnsNoSuspiciousMessage() throws Exception {
    when(logService.clientExists("cleanClient")).thenReturn(true);
    when(logService.getIpsWithManyAuthErrors("cleanClient")).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/security/suspicious-ips/cleanClient"))
            .andExpect(status().isOk())
            .andExpect(content().string("No suspicious IPs found"));

    verify(logService, times(1)).getIpsWithManyAuthErrors("cleanClient");
  }

  @Test
  void testGetSuspiciousIpsWithInvalidClientIdCharacters() throws Exception {
    String badId = "client with spaces";
    when(logService.clientExists(badId)).thenReturn(false);

    mockMvc.perform(get("/security/suspicious-ips/" + badId))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Error: clientId not found"));
  }

  @Test
  void testGetSuspiciousIpsWithSpecialCharactersInPath() throws Exception {
    when(logService.clientExists("client@123!")).thenReturn(true);
    when(logService.getIpsWithManyAuthErrors("client@123!")).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/security/suspicious-ips/client@123!"))
            .andExpect(status().isOk())
            .andExpect(content().string("No suspicious IPs found"));
  }
}
