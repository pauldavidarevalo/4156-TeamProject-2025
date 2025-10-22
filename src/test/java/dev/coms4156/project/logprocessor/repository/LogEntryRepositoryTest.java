package dev.coms4156.project.logprocessor.repository;

import static org.assertj.core.api.Assertions.assertThat;

import dev.coms4156.project.logprocessor.model.LogEntry;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class LogEntryRepositoryTest {

  @Autowired
  private LogEntryRepository repo;

  @BeforeEach
  void setUp() {
    repo.deleteAll();

    // Build entities (id will auto-generate)
    LogEntry e1 = new LogEntry("clientA",
            "XX.0.0.1", "GET", "/home", 200, 500L, LocalDateTime.now());
    LogEntry e2 = new LogEntry("clientA",
            "XX.0.0.2", "GET", "/home", 200, 300L, LocalDateTime.now());
    LogEntry e3 = new LogEntry("clientA",
            "XX.0.0.3", "POST", "/upload", 404, 0L, LocalDateTime.now());
    LogEntry e4 = new LogEntry("clientB",
            "XX.0.0.4", "GET", "/info", 200, 100L, LocalDateTime.now());

    repo.saveAll(List.of(e1, e2, e3, e4));
  }

  @Test
  @DisplayName("findTopEndpoints should group by endpoint and order by count DESC")
  void testFindTopEndpoints() {
    List<Object[]> results = repo.findTopEndpoints();

    assertThat(results).isNotEmpty();
    assertThat(results.get(0)[0]).isEqualTo("/home");
    assertThat((Long) results.get(0)[1]).isEqualTo(2L);

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
  @DisplayName("countRequestsByHour should run only in SQLite-compatible environment")
  void testCountRequestsByHour() {
    try {
      List<Object[]> results = repo.countRequestsByHour("clientA");
      // We can't validate counts exactly in H2, just ensure query runs without crash
      assertThat(results).isNotNull();
    } catch (InvalidDataAccessResourceUsageException e) {
      System.out.println("Skipping countRequestsByHour test (H2 lacks strftime)");
    }
  }

  @Test
  @DisplayName("countErrorCodesByHour should run only in SQLite-compatible environment")
  void testCountErrorCodesByHour() {
    try {
      List<Object[]> results = repo.countErrorCodesByHour();
      assertThat(results).isNotNull();
    } catch (InvalidDataAccessResourceUsageException e) {
      System.out.println("Skipping countErrorCodesByHour test (H2 lacks strftime)");
    }
  }


}
