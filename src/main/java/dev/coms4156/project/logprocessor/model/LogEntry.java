package dev.coms4156.project.logprocessor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This file represents the structure for a typical LogEntry.
 */
@Entity
@Table(name = "log_entries")
public class LogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String clientId;

  @Column(nullable = false)
  private String ipAddress;

  @Column(nullable = false)
  private String method;

  @Column(nullable = false)
  private String endpoint;

  @Column(nullable = false)
  private LocalDateTime hourWindow;


  private int statusCode;
  private long responseSize;
  private LocalDateTime timestamp;

  /**
   * This is the empty LogEntry constructor.
   */
  public LogEntry() {
      // Empty constructor with no arguments.
  }

  /**
   * Parameterized LogEntry constructor.
   *
   * @param clientId ID input by the client.
   * @param ipAddress IP Address provided by the client.
   * @param method Method being invoked.
   * @param endpoint Endpoint being invoked.
   * @param statusCode Status code when the log entry is processed.
   * @param responseSize Size of the response after the log entry is processed.
   * @param timestamp Time when the log entry is processed.
   */
  public LogEntry(String clientId, String ipAddress, String method, String endpoint,
                  int statusCode, long responseSize, LocalDateTime timestamp) {
    this.clientId = clientId;
    this.ipAddress = ipAddress;
    this.method = method;
    this.endpoint = endpoint;
    this.statusCode = statusCode;
    this.responseSize = responseSize;
    this.timestamp = timestamp;
    this.hourWindow = timestamp.truncatedTo(ChronoUnit.HOURS);
  }

  // --- Getters ---
  public Long getId() {
    return id;
  }

  public String getClientId() {
    return clientId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getMethod() {
    return method;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public long getResponseSize() {
    return responseSize;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public LocalDateTime getHourWindow() {
    return hourWindow;
  }

  // --- Setters (optional but safer for JPA) ---
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setResponseSize(long responseSize) {
    this.responseSize = responseSize;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
