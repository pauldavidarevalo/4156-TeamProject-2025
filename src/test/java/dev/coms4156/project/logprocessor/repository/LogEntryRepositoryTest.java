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
  @DisplayName("countRequestsByHour should aggregate request counts by hourWindow")
  void testCountRequestsByHour() {
    LocalDateTime hour1 = LocalDateTime.of(2025, 10, 19, 12, 0);
    LocalDateTime hour2 = LocalDateTime.of(2025, 10, 19, 13, 0);
    
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/home", 200, 100, hour1));
    repo.save(new LogEntry("client1", "123.456.7.8", "POST", "/upload", 201, 200, hour1));
    repo.save(new LogEntry("client1", "123.456.7.9", "GET", "/data", 200, 150, hour1));

    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/home", 200, 100, hour2));
    repo.save(new LogEntry("client1", "123.456.7.9", "DELETE", "/resource", 204, 0, hour2));

    List<Object[]> results = repo.countRequestsByHour("client1");
    assertThat(results).hasSize(2);
    
    Object[] row1 = results.get(0);
    assertThat((LocalDateTime) row1[0]).isEqualTo(hour1);
    assertThat((Long) row1[1]).isEqualTo(3L);

    Object[] row2 = results.get(1);
    assertThat((LocalDateTime) row2[0]).isEqualTo(hour2);
    assertThat((Long) row2[1]).isEqualTo(2L);
  }

  @Test
  @DisplayName("countErrorCodesByHour should aggregate 4xx and 5xx errors by hourWindow")
  void testCountErrorCodesByHour() {
    LocalDateTime hour1 = LocalDateTime.of(2025, 10, 19, 12, 0);
    LocalDateTime hour2 = LocalDateTime.of(2025, 10, 19, 13, 0);
    // Hour1: 2 4xx, 1 5xx
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/bad1", 404, 100, hour1));
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/bad2", 400, 50, hour1));
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/error", 500, 0, hour1));

    // Hour2: 1 4xx, 2 5xx
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/bad1", 404, 100, hour2));
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/bad2", 500, 50, hour2));
    repo.save(new LogEntry("client1", "123.456.7.8", "GET", "/error", 500, 0, hour2));
    
    List<Object[]> results = repo.countErrorCodesByHour("client1");
    assertThat(results).hasSize(2);

    Object[] row1 = results.get(0);
    assertThat((LocalDateTime) row1[0]).isEqualTo(hour1);
    assertThat((Long) row1[1]).isEqualTo(2L);
    assertThat((Long) row1[2]).isEqualTo(1L);

    Object[] row2 = results.get(1);
    assertThat((LocalDateTime) row2[0]).isEqualTo(hour2);
    assertThat((Long) row2[1]).isEqualTo(1L);
    assertThat((Long) row2[2]).isEqualTo(2L);
  }

  @Test
  @DisplayName(
      "findIpsWithManyAuthErrors should find IPs "
      + "with >= threshold 401/403 errors per hour"
  )
  void testFindIpsWithManyAuthErrors() {
    LocalDateTime hour1 = LocalDateTime.of(2025, 10, 19, 12, 0);
    LocalDateTime hour2 = LocalDateTime.of(2025, 10, 19, 13, 0);

    // Hour1: 5 401 from 123.456.7.89
    for (int i = 0; i < 5; i++) {
      repo.save(new LogEntry(
          "clientA", "123.456.7.89", "GET", "/login", 401, 100, hour1.plusMinutes(i)));
    }
    repo.save(new LogEntry("clientA", "000.000.0.XX", "GET", "/login", 200, 50, hour1));
    repo.save(new LogEntry("clientA", "000.000.0.XX", "GET", "/data", 200, 50, hour1));

    // Hour2: 5 401 from 987.654.3.21
    for (int i = 0; i < 5; i++) {
      repo.save(new LogEntry(
          "clientA", "987.654.3.21", "GET", "/login", 401, 100, hour2.plusMinutes(i)));
    }
    repo.save(new LogEntry("clientA", "000.000.0.XX", "GET", "/login", 200, 50, hour2));
    repo.save(new LogEntry("clientA", "000.000.0.XX", "GET", "/data", 200, 50, hour2));

    List<Object[]> results = repo.findIpsWithManyAuthErrors(3, "clientA");
    assertThat(results).hasSize(2);
    
    Object[] row1 = results.get(0);
    assertThat((String) row1[0]).isEqualTo("123.456.7.89");
    assertThat((LocalDateTime) row1[1]).isEqualTo(hour1);
    assertThat((Long) row1[2]).isEqualTo(5L);

    Object[] row2 = results.get(1);
    assertThat((String) row2[0]).isEqualTo("987.654.3.21");
    assertThat((LocalDateTime) row2[1]).isEqualTo(hour2);
    assertThat((Long) row2[2]).isEqualTo(5L);
  }
}