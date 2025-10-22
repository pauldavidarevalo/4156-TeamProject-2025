package dev.coms4156.project.logprocessor.repository;

import dev.coms4156.project.logprocessor.model.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LogEntryRepositoryTest {

  @Autowired
  private LogEntryRepository repo;

  private LogEntry e1, e2, e3, e4;

  @BeforeEach
  void setUp() {
    repo.deleteAll();

    // Build entities (id will auto-generate)
    e1 = new LogEntry("clientA", "10.0.0.1", "GET", "/home", 200, 500L, LocalDateTime.now());
    e2 = new LogEntry("clientA", "10.0.0.2", "GET", "/home", 200, 300L, LocalDateTime.now());
    e3 = new LogEntry("clientA", "10.0.0.3", "POST", "/upload", 404, 0L, LocalDateTime.now());
    e4 = new LogEntry("clientB", "10.0.0.4", "GET", "/info", 200, 100L, LocalDateTime.now());

    repo.saveAll(List.of(e1, e2, e3, e4));
  }

  @Test
  @DisplayName("findTopEndpoints should group by endpoint and order by count DESC")
  void testFindTopEndpoints() {
    List<Object[]> results = repo.findTopEndpoints();

    assertThat(results).isNotEmpty();
    assertThat(results.get(0)[0]).isEqualTo("/home");
    assertThat(((Long) results.get(0)[1])).isEqualTo(2L);

    // Ensure all endpoints are present
    assertThat(results.stream().map(r -> (String) r[0]))
            .containsExactlyInAnyOrder("/home", "/upload", "/info");
  }

  @Test
  @DisplayName("countStatusCodesByClientId should group status codes for the given client")
  void testCountStatusCodesByClientId() {
    List<Object[]> results = repo.countStatusCodesByClientId("clientA");

    assertThat(results).hasSize(2); // 200 and 404
    long totalCount = results.stream()
            .mapToLong(r -> (Long) r[1])
            .sum();
    assertThat(totalCount).isEqualTo(3);

    // Verify one of the pairs explicitly
    boolean has404 = results.stream().anyMatch(r -> (int) r[0] == 404 && (Long) r[1] == 1L);
    assertThat(has404).isTrue();
  }

  @Test
  @DisplayName("existsByClientId should correctly detect existing and missing clients")
  void testExistsByClientId() {
    assertThat(repo.existsByClientId("clientA")).isTrue();
    assertThat(repo.existsByClientId("clientB")).isTrue();
    assertThat(repo.existsByClientId("nope")).isFalse();
  }

  @Test
  @DisplayName("Repository should persist and retrieve entities correctly")
  void testSaveAndFindAll() {
    List<LogEntry> all = repo.findAll();
    assertThat(all).hasSize(4);

    LogEntry first = all.get(0);
    assertThat(first.getId()).isNotNull();
    assertThat(first.getClientId()).isIn("clientA", "clientB");
  }

  @Test
  @DisplayName("Repository should delete all entities successfully")
  void testDeleteAll() {
    repo.deleteAll();
    assertThat(repo.count()).isZero();
  }

  @Test
  @DisplayName("countRequestsByHour should return hourly request counts for a client")
  void testCountRequestsByHour() {
    List<Object[]> results = repo.countRequestsByHour("clientA");

    assertThat(results).isNotEmpty();
    // Each element = [hourString, count]
    Object[] firstRow = results.get(0);
    assertThat(firstRow[0]).isInstanceOf(String.class);
    assertThat(firstRow[1]).isInstanceOf(Long.class);

    long total = results.stream()
            .mapToLong(r -> (Long) r[1])
            .sum();
    assertThat(total).isEqualTo(3); // clientA made 3 requests
  }

  @Test
  @DisplayName("countErrorCodesByHour should return hourly 4xx and 5xx error counts")
  void testCountErrorCodesByHour() {
    List<Object[]> results = repo.countErrorCodesByHour();

    assertThat(results).isNotEmpty();
    // Each element = [hourString, count4xx, count5xx]
    Object[] firstRow = results.get(0);
    assertThat(firstRow[0]).isInstanceOf(String.class);
    assertThat(firstRow[1]).isInstanceOf(Long.class);
    assertThat(firstRow[2]).isInstanceOf(Long.class);

    long total4xx = results.stream()
            .mapToLong(r -> (Long) r[1])
            .sum();
    long total5xx = results.stream()
            .mapToLong(r -> (Long) r[2])
            .sum();

    assertThat(total4xx).isEqualTo(1); // one 404 error
    assertThat(total5xx).isZero();     // no 5xx errors
  }
}
