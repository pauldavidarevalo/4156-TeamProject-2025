package dev.coms4156.project.logprocessor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String method;
    private String endpoint;
    private int statusCode;
    private long responseSize;
    private LocalDateTime timestamp;
    private String clientId;

    public LogEntry() {}

    public LogEntry(String clientId, String ipAddress, String method, String endpoint, int statusCode, long responseSize, LocalDateTime timestamp) {
        this.clientId = clientId;
        this.ipAddress = ipAddress;
        this.method = method;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.responseSize = responseSize;
        this.timestamp = timestamp;
    }

    // getters and setters
    public Long getId() { return id; }
    public String getIpAddress() { return ipAddress; }
    public String getMethod() { return method; }
    public String getEndpoint() { return endpoint; }
    public int getStatusCode() { return statusCode; }
    public long getResponseSize() { return responseSize; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getClientId() { return clientId; }


}
