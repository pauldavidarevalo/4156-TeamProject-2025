package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This file represents endpoints with the LogService controller for
 * analytics on the uploaded logs.
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
  private final LogService logService;

  public AnalyticsController(LogService logService) {
    this.logService = logService;
  }

  /**
   * Sample analytics endpoint to be replaced by more logic for security endpoints
   * and features.
   *
   * @return A JSON array of [endpoint, count] arrays sorted by frequency descending (most to least)
   */
  @GetMapping("/top-endpoints")
  public Object getTopEndpoints() {
    return logService.getTopEndpoints();
  }

  /**
   * Returns hourly request counts for a specific client.
   * Example:
   * GET /analytics/timeseries/requests/clientA
   *
   * @return A JSON object mapping hour strings to request counts
   */
  @GetMapping("/timeseries/requests/{clientId}")
  public Object getRequestCountsByHour(@PathVariable String clientId) {
    return logService.getRequestCountsByHour(clientId);
  }

  /**
   * Returns hourly 4xx and 5xx error counts for a specific client.
   * Example:
   * GET /analytics/timeseries/error-counts/clientA
   *
   * @return A JSON object mapping hour strings to {4xx, 5xx} error counts
   */
  @GetMapping("/timeseries/error-counts/{clientId}")
  public Object getErrorCountsByHour(@PathVariable String clientId) {
    return logService.getErrorCountsByHour(clientId);
  }
}
