package dev.coms4156.project.logprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allows BeforeAll to be non-static, which is necessary for using restTemplate
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
  void uploadTestLogs() throws Exception {
    File testDb = new File(TEST_DB_PATH);
    if (testDb.exists()) {
      testDb.delete();
    }

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
  void cleanup() {
    // Leave behind target/test-logs.db as proof the test uses persistent storage
    // File testDb = new File(TEST_DB_PATH);
    // if (testDb.exists()) {
    //   testDb.delete();
    // }
  }

  @Test
  void testStatusCodeCountsForClientA() {
    ResponseEntity<Map<String, Integer>> statusResponseA =
    restTemplate.exchange(
        "/logs/statusCodeCounts?clientId=clientA",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<Map<String, Integer>>() {}
    );
    assertThat(statusResponseA.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Integer> statusCounts = statusResponseA.getBody();
    assertThat(statusCounts).containsEntry("200", 3);
    assertThat(statusCounts).containsEntry("302", 1);
    assertThat(statusCounts).containsEntry("500", 1);
  }

  @Test
  void testStatusCodeCountsForClientB() {
    ResponseEntity<Map<String, Integer>> statusResponseB =
    restTemplate.exchange(
        "/logs/statusCodeCounts?clientId=clientB",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<Map<String, Integer>>() {}
    );
    assertThat(statusResponseB.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Integer> statusCounts = statusResponseB.getBody();
    assertThat(statusCounts).containsEntry("200", 3);
    assertThat(statusCounts).containsEntry("302", 1);
    assertThat(statusCounts).containsEntry("500", 1);
  }

  @Test
  void testTimeseriesRequests() {
    ResponseEntity<Map<String, Integer>> timeseriesRequestsResponse =
    restTemplate.exchange(
        "/analytics/timeseries/requests/clientA",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<Map<String, Integer>>() {}
    );
    assertThat(timeseriesRequestsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Integer> requestsMap = timeseriesRequestsResponse.getBody();
    assertThat(requestsMap).containsEntry(EXPECTED_HOUR, 5);
  }

  @Test
  void testTimeseriesErrorCounts() {
    ResponseEntity<Map<String, Map<String, Integer>>> timeseriesErrorsResponse =
    restTemplate.exchange(
        "/analytics/timeseries/error-counts/clientA",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<Map<String, Map<String, Integer>>>() {}
    );
    assertThat(timeseriesErrorsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Map<String, Integer>> errorsMap = timeseriesErrorsResponse.getBody();
    Map<String, Integer> inner = errorsMap.get(EXPECTED_HOUR);
    assertThat(inner.getOrDefault("4xx", 0)).isEqualTo(0);
    assertThat(inner.getOrDefault("5xx", 0)).isEqualTo(1);
  }

  @Test
  void testSuspiciousIps() {
    ResponseEntity<List<Map<String, Object>>> suspiciousIpsResponse =
    restTemplate.exchange(
        "/security/suspicious-ips/susClient",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
    );
    assertThat(suspiciousIpsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<Map<String, Object>> suspiciousIpsList = suspiciousIpsResponse.getBody();

    Map<String, Object> expectedResults = new HashMap<>();
    expectedResults.put("ipAddress", "123.456.7.89");
    expectedResults.put("hourWindow", EXPECTED_HOUR);
    expectedResults.put("errorCount", 10);

    assertThat(suspiciousIpsList).containsExactly(expectedResults);
  }
}
