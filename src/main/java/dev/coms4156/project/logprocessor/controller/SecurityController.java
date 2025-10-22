package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security")
public class SecurityController {

  private final LogService logService;

  public SecurityController(LogService logService) {
      this.logService = logService;
  }

  @GetMapping("/suspicious-ips")
  public Object getSuspiciousIps() {
      return logService.getIpsWithManyAuthErrors();
  }
}

