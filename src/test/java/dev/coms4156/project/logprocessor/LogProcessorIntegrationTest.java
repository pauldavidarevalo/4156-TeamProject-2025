package dev.coms4156.project.logprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
  private static final String EXPECTED_HOUR = "2025-10-19T12:00";

  @BeforeAll
  static void cleanupBeforeAllTests() {
    // Clean up test DB before all tests run
    File testDb = new File(TEST_DB_PATH);
    if (testDb.exists()) {
      testDb.delete();
    }
  }

  @BeforeEach
  void uploadTestLogs() throws Exception {
    Path sample = Path.of("sampleLogs", "sampleApacheSimple.log");
    
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
  void testStatusCodeCountsForClientA() {
    @SuppressWarnings("unchecked")
    ResponseEntity<Map<String, Integer>> statusResponseA =
        (ResponseEntity<Map<String, Integer>>) (ResponseEntity<?>) 
        restTemplate.getForEntity("/logs/statusCodeCounts?clientId=clientA",
            Map.class);
    assertThat(statusResponseA.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Integer> statusCounts = statusResponseA.getBody();
    assertThat(statusCounts).containsEntry("200", 3);
    assertThat(statusCounts).containsEntry("302", 1);
    assertThat(statusCounts).containsEntry("500", 1);
  }

  @Test
  void testStatusCodeCountsForClientB() {
    @SuppressWarnings("unchecked")
    ResponseEntity<Map<String, Integer>> statusResponseB =
        (ResponseEntity<Map<String, Integer>>) (ResponseEntity<?>) 
        restTemplate.getForEntity("/logs/statusCodeCounts?clientId=clientB",
            Map.class);
    assertThat(statusResponseB.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Integer> statusCounts = statusResponseB.getBody();
    assertThat(statusCounts).containsEntry("200", 3);
    assertThat(statusCounts).containsEntry("302", 1);
    assertThat(statusCounts).containsEntry("500", 1);
  }

  @Test
  void testTimeseriesRequests() {
    @SuppressWarnings("unchecked")
    ResponseEntity<Map<String, Integer>> timeseriesRequestsResponse =
      (ResponseEntity<Map<String, Integer>>) (ResponseEntity<?>)
      restTemplate.getForEntity("/analytics/timeseries/requests/clientA", Map.class);
    assertThat(timeseriesRequestsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Integer> requestsMap = timeseriesRequestsResponse.getBody();
    assertThat(requestsMap).containsEntry(EXPECTED_HOUR, 5);
  }

  @Test
  void testTimeseriesErrorCounts() {
    @SuppressWarnings("unchecked")
    ResponseEntity<Map<String, Map<String, Integer>>> timeseriesErrorsResponse =
        (ResponseEntity<Map<String, Map<String, Integer>>>) (ResponseEntity<?>)
        restTemplate.getForEntity("/analytics/timeseries/error-counts/clientA", Map.class);
    assertThat(timeseriesErrorsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Map<String, Integer>> errorsMap = timeseriesErrorsResponse.getBody();
    Map<String, Integer> inner = errorsMap.get(EXPECTED_HOUR);
    assertThat(inner.getOrDefault("4xx", 0)).isEqualTo(0);
    assertThat(inner.getOrDefault("5xx", 0)).isEqualTo(1);
  }

  @Test
  void testSuspiciousIps() {
    @SuppressWarnings("unchecked")
    ResponseEntity<List<Map<String, Object>>> suspiciousIpsResponse =
        (ResponseEntity<List<Map<String, Object>>>) (ResponseEntity<?>)
        restTemplate.getForEntity("/security/suspicious-ips/susClient", List.class);
    assertThat(suspiciousIpsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<Map<String, Object>> suspiciousIpsList = suspiciousIpsResponse.getBody();

    Map<String, Object> expectedResults = new HashMap<>();
    expectedResults.put("ipAddress", "123.456.7.89");
    expectedResults.put("hourWindow", EXPECTED_HOUR);
    expectedResults.put("errorCount", 10);

    assertThat(suspiciousIpsList).containsExactly(expectedResults);
  }
}
