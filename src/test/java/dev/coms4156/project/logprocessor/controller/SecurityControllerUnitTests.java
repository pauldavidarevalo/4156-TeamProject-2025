package dev.coms4156.project.logprocessor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This class contains the unit tests for the SecurityController class.
 */
@ExtendWith(MockitoExtension.class)
public class SecurityControllerUnitTests {

  @Mock
  private LogService logService;

  @InjectMocks
  private SecurityController securityController;

  @Test
  void getSuspiciousIpsAndReturnFromService() {
    Map<String, Object> mockMap = new HashMap<>();
    mockMap.put("ipAddress", "10.0.0.X");
    mockMap.put("hourWindow", "2025-10-22 13:00:00");
    mockMap.put("count", 7L);
    mockMap.put("clientId", "clientA");
    List<Map<String, Object>> mockSuspiciousIps = new ArrayList<>();
    mockSuspiciousIps.add(mockMap);

    when(logService.getIpsWithManyAuthErrors("clientA")).thenReturn(mockSuspiciousIps);

    Object result = securityController.getSuspiciousIps("clientA");
    assertEquals(mockSuspiciousIps, result);
    verify(logService, times(1)).getIpsWithManyAuthErrors("clientA");
  }
}