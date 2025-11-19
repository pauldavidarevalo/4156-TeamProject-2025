package dev.coms4156.project.logprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite:target/test-logs.db",
    "spring.datasource.driver-class-name=org.sqlite.JDBC",
    "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
    "spring.jpa.hibernate.ddl-auto=create"
})
class LogProcessorIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  private static final String TEST_DB_PATH = "target/test-logs.db";

  @BeforeEach
  void cleanupTestDb() {
    // Remove test DB before each test to ensure clean state
    File testDb = new File(TEST_DB_PATH);
    if (testDb.exists()) {
      testDb.delete();
    }
  }

  @AfterEach
  void cleanupAfterTest() {
    // Remove test DB after test completes
    // Comment out to inspect DB file after test
    /*
    File testDb = new File(TEST_DB_PATH);
    if (testDb.exists()) {
      testDb.delete();
    }
    */
  }

  @Test
  void uploadSampleLogAndVerifyEndpoints() throws Exception {
    Path sample = Path.of("sampleLogs", "sampleApacheSimple.log");
    
    // Upload the log file for clientId 'clientA'
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("clientId", "clientA");
    body.add("file", new FileSystemResource(sample.toFile()));

    ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
        "/logs/upload",
        body,
        String.class
    );
    assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(uploadResponse.getBody()).contains("Log file processed successfully.");

    body = new LinkedMultiValueMap<>();
    body.add("clientId", "clientB");
    body.add("file", new FileSystemResource(sample.toFile()));
    uploadResponse = restTemplate.postForEntity(
        "/logs/upload",
        body,
        String.class
    );
    assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(uploadResponse.getBody()).contains("Log file processed successfully.");

    Path suspiciousIpsPath = Path.of("sampleLogs", "suspicousIps.log");
    
    // Upload the log file for clientId 'susClient'
    body = new LinkedMultiValueMap<>();
    body.add("clientId", "susClient");
    body.add("file", new FileSystemResource(suspiciousIpsPath.toFile()));

    uploadResponse = restTemplate.postForEntity(
        "/logs/upload",
        body,
        String.class
    );
    assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(uploadResponse.getBody()).contains("Log file processed successfully.");

    @SuppressWarnings("unchecked")
    ResponseEntity<java.util.Map<String, Integer>> statusResponseA =
        (ResponseEntity<java.util.Map<String, Integer>>) (ResponseEntity<?>) 
        restTemplate.getForEntity("/logs/statusCodeCounts?clientId=clientA",
            java.util.Map.class);
    assertThat(statusResponseA.getStatusCode()).isEqualTo(HttpStatus.OK);
    java.util.Map<String, Integer> statusCounts = statusResponseA.getBody();
    assertThat(statusCounts).containsEntry("200", 3);
    assertThat(statusCounts).containsEntry("302", 1);
    assertThat(statusCounts).containsEntry("500", 1);

    @SuppressWarnings("unchecked")
    ResponseEntity<java.util.Map<String, Integer>> statusResponseB =
        (ResponseEntity<java.util.Map<String, Integer>>) (ResponseEntity<?>) 
        restTemplate.getForEntity("/logs/statusCodeCounts?clientId=clientB",
            java.util.Map.class);
    assertThat(statusResponseB.getStatusCode()).isEqualTo(HttpStatus.OK);
    statusCounts = statusResponseB.getBody();
    assertThat(statusCounts).containsEntry("200", 3);
    assertThat(statusCounts).containsEntry("302", 1);
    assertThat(statusCounts).containsEntry("500", 1);

    // Calculate current hour truncated (service uses upload time)
    java.time.LocalDateTime nowLocal = java.time.LocalDateTime.now();
    java.time.LocalDateTime hourTruncated = nowLocal.truncatedTo(java.time.temporal.ChronoUnit.HOURS);
    java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
    String expectedHour = hourTruncated.format(df);

    // Verify timeseries/requests endpoint returns 200 and contains expected total (5 requests)
    @SuppressWarnings("unchecked")
    ResponseEntity<java.util.Map<String, Integer>> timeseriesRequestsResponse =
        (ResponseEntity<java.util.Map<String, Integer>>) (ResponseEntity<?>)
        restTemplate.getForEntity("/analytics/timeseries/requests/clientA", java.util.Map.class);
    assertThat(timeseriesRequestsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    java.util.Map<String, Integer> requestsMap = timeseriesRequestsResponse.getBody();
    assertThat(requestsMap).isNotNull();
    assertThat(requestsMap).containsEntry(expectedHour, 5);

    // Verify timeseries/error-counts endpoint returns 200 and contains expected counts (one 5xx, zero 4xx)
    @SuppressWarnings("unchecked")
    ResponseEntity<java.util.Map<String, java.util.Map<String, Integer>>> timeseriesErrorsResponse =
        (ResponseEntity<java.util.Map<String, java.util.Map<String, Integer>>>) (ResponseEntity<?>)
        restTemplate.getForEntity("/analytics/timeseries/error-counts/clientA", java.util.Map.class);
    assertThat(timeseriesErrorsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    java.util.Map<String, java.util.Map<String, Integer>> errorsMap = timeseriesErrorsResponse.getBody();
    assertThat(errorsMap).isNotNull();
    java.util.Map<String, Integer> inner = errorsMap.get(expectedHour);
    assertThat(inner).isNotNull();
    assertThat(inner.getOrDefault("4xx", 0)).isEqualTo(0);
    assertThat(inner.getOrDefault("5xx", 0)).isEqualTo(1);

    // Verify suspicious IPs endpoint returns list of suspicious IP objects
    @SuppressWarnings("unchecked")
    ResponseEntity<java.util.List<java.util.Map<String, Object>>> suspiciousIpsResponse =
        (ResponseEntity<java.util.List<java.util.Map<String, Object>>>) (ResponseEntity<?>)
        restTemplate.getForEntity("/security/suspicious-ips/susClient", java.util.List.class);
    assertThat(suspiciousIpsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    java.util.List<java.util.Map<String, Object>> suspiciousIpsList = suspiciousIpsResponse.getBody();
    assertThat(suspiciousIpsList).isNotNull().isNotEmpty();
    System.out.println("\n\n*************\n" + suspiciousIpsList + "\n***********");
    
    // Build expected hourWindow for suspicious IPs endpoint (ISO format: "yyyy-MM-dd'T'HH:00:00")
    java.time.format.DateTimeFormatter hourWindowFormatter = 
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00:00");
    String expectedHourWindow = hourTruncated.format(hourWindowFormatter);
    
    // Expected: 123.456.7.89 has 10 × 401 errors, 987.654.3.21 has 10 × 403 errors
    java.util.Map<String, Object> expected1 = new java.util.LinkedHashMap<>();
    expected1.put("ipAddress", "123.456.7.89");
    expected1.put("hourWindow", expectedHourWindow);
    expected1.put("errorCount", 10);
    
    java.util.Map<String, Object> expected2 = new java.util.LinkedHashMap<>();
    expected2.put("ipAddress", "987.654.3.21");
    expected2.put("hourWindow", expectedHourWindow);
    expected2.put("errorCount", 10);
    
    assertThat(suspiciousIpsList).containsExactly(expected1, expected2);
  }
}
