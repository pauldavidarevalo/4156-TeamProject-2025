package dev.coms4156.project.logprocessor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This class contains the unit tests for the AnalyticsController class.
 */
@ExtendWith(MockitoExtension.class)
public class AnalyticsControllerUnitTests {

  @Mock
  private LogService logService;

  @InjectMocks
  private AnalyticsController analyticsController;

  @Test
  void getTopEndpointsAndReturnFromService() {
    List<Object[]> mockTopEndpoints = Arrays.asList(
            new Object[]{"/api/endpoint1", 100L},
            new Object[]{"/api/endpoint2", 50L}
    );
    when(logService.getTopEndpoints()).thenReturn(mockTopEndpoints);

    Object result = analyticsController.getTopEndpoints();
    assertEquals(mockTopEndpoints, result);
    verify(logService, times(1)).getTopEndpoints();
  }
}