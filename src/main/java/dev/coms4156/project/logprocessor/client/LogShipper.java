package dev.coms4156.project.logprocessor.client;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This file represents the LogShipper class that is utilized to run the client.
 */
@Component
@ConditionalOnProperty(name = "logshipper.enabled", havingValue = "true")
public class LogShipper {

  private static final Logger log = LoggerFactory.getLogger(LogShipper.class);
  private final LogShipperService service;

  public LogShipper(LogShipperService service) {
    this.service = service;
  }

  /**
   * This method initializes the LogShipper class.
   */
  @PostConstruct
  public void init() {
    service.loadPosition();
    if (log.isInfoEnabled()) {
      log.info("LogShipper STARTED | Client: {} | File: {}",
          service.getConfig().getClientId(),
          service.getConfig().getLogFile());
    }
  }
}