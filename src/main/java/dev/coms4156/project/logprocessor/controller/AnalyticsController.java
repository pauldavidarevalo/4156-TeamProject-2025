package dev.coms4156.project.logprocessor.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import dev.coms4156.project.logprocessor.service.LogService;
import org.springframework.http.ResponseEntity;
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
   * Returns 200 OK if clientExists, 404 NOT FOUND if not.
   * Example:
   * GET /analytics/timeseries/requests/clientA
   *
   * @return A JSON object mapping hour strings to request counts
   */
  @GetMapping("/timeseries/requests/{clientId}")
  public ResponseEntity<?> getRequestCountsByHour(@PathVariable String clientId) {
    if (!logService.clientExists(clientId)) {
      return new ResponseEntity<>("Error: clientId not found", NOT_FOUND);
    }
    return new ResponseEntity<>(logService.getRequestCountsByHour(clientId), OK);
  }

  /**
   * Returns hourly 4xx and 5xx error counts for a specific client.
   * Returns 200 OK if clientExists, 404 NOT FOUND if not.
   * Example:
   * GET /analytics/timeseries/error-counts/clientA
   *
   * @return A JSON object mapping hour strings to {4xx, 5xx} error counts
   */
  @GetMapping("/timeseries/error-counts/{clientId}")
  public ResponseEntity<?> getErrorCountsByHour(@PathVariable String clientId) {
    if (!logService.clientExists(clientId)) {
      return new ResponseEntity<>("Error: clientId not found", NOT_FOUND);
    }
    return new ResponseEntity<>(logService.getErrorCountsByHour(clientId), OK);
  }
}
