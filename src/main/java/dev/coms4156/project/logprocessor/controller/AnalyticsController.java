package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This file represents the AnalyticsController that provides insights about the top endpoints.
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
   * @return Object topEndpoints
   */
  @GetMapping("/top-endpoints")
  public Object getTopEndpoints() {
    return logService.getTopEndpoints();
  }
}

