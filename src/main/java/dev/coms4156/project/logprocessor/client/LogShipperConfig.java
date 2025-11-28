package dev.coms4156.project.logprocessor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * This file represents the configuration needed to run the LogShipper client class.
 */
@Configuration
public class LogShipperConfig {

  @Value("${logshipper.enabled:false}")
  private boolean enabled;

  @Value("${logshipper.client-id:local-agent}")
  private String clientId;

  @Value("${logshipper.url:http://localhost:8080/logs/upload}")
  private String url;

  @Value("${logshipper.log-file:./sampleLogs/sampleApacheSimple.log}")
  private String logFile;

  @Value("${logshipper.batch-size:20}")
  private int batchSize;

  @Value("${logshipper.interval-ms:10000}")
  private long intervalMs;

  public boolean isEnabled() {
    return enabled;
  }

  public String getClientId() {
    return clientId;
  }

  public String getUrl() {
    return url;
  }

  public String getLogFile() {
    return logFile;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public long getIntervalMs() {
    return intervalMs;
  }

  @Override
  public String toString() {
    return "LogShipperConfig{"
            + "enabled="
            + enabled
            + ", clientId='"
            + clientId
            + '\''
            + ", url='"
            + url
            + '\''
            + ", logFile='"
            + logFile
            + '\''
            + ", batchSize="
            + batchSize
            + ", intervalMs="
            + intervalMs
            + '}';
  }
}