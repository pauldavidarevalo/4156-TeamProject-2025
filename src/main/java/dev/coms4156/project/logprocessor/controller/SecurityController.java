package dev.coms4156.project.logprocessor.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
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
   * Calls the internal log service's retrieval of IPs with authorization errors
   * function (major unit of code).
   * Get IPs with 5 or more authentication errors (401 or 403 status codes).
   * Returns a list of maps, each containing an hourWindow, ipAddress, and count of auth errors.
   * Returns 200 OK if clientExists, 404 NOT FOUND if not.
   *
   * @return autoformatted JSON of entries containing hourWindow, ipAddress,
   *         and count of auth errors in that window
   */
  @GetMapping("/suspicious-ips/{clientId}")
  public ResponseEntity<?> getSuspiciousIps(@PathVariable String clientId) {
    if (!logService.clientExists(clientId)) {
      return new ResponseEntity<>("Error: clientId not found", NOT_FOUND);
    }
    List<Map<String, Object>> ips = logService.getIpsWithManyAuthErrors(clientId);
    if (ips.isEmpty()) {
      return new ResponseEntity<>("No suspicious IPs found", OK);
    }
    return new ResponseEntity<>(ips, OK);
  }
}
