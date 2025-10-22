package dev.coms4156.project.logprocessor.controller;



import dev.coms4156.project.logprocessor.service.LogService;



import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final LogService logService;

    public AnalyticsController(LogService logService) {
        this.logService = logService;
    }

    /** Sample analytics endpoint to be replaced by more logic for security endpoints
     * and features.
     * /
     * @return
     */
    @GetMapping("/top-endpoints")
    public Object getTopEndpoints() {
        return logService.getTopEndpoints();
    }
  /**
   * Returns hourly request counts for a specific client.
   * Example:
   *   GET /analytics/timeseries/requests/clientA
   */
  @GetMapping("/timeseries/requests/{clientId}")
  public Object getRequestCountsByHour(@PathVariable String clientId) {
    return logService.getRequestCountsByHour(clientId);
  }

  /**
   * Returns hourly 4xx and 5xx error counts system-wide.
   * Example:
   *   GET /analytics/timeseries/error-counts
   */
  @GetMapping("/timeseries/error-counts")
  public Object getErrorCountsByHour() {
    return logService.getErrorCountsByHour();
  }

}

