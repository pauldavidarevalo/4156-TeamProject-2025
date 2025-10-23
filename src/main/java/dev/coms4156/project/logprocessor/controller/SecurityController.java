package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class handles all security-related endpoints.
 */
@RestController
@RequestMapping("/security")
public class SecurityController {

  private final LogService logService;

  public SecurityController(LogService logService) {
    this.logService = logService;
  }

  /**
  * Get IPs with 5 or more authentication errors (401 or 403 status codes).
  *
  * @return autoformatted JSON of entries containing hourWindow, ipAddress, 
  *     and count of auth errors in that window
  */
  @GetMapping("/suspicious-ips/{clientId}")
  public Object getSuspiciousIps(@PathVariable String clientId) {
    return logService.getIpsWithManyAuthErrors(clientId);
  }
}

