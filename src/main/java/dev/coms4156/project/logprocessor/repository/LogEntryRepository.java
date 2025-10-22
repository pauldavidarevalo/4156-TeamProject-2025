package dev.coms4156.project.logprocessor.repository;

import dev.coms4156.project.logprocessor.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Interface for communicating with the database. Jpa framework allows for some default
 * queries without writing SQL.
 */
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    @Query("SELECT l.endpoint, COUNT(l) FROM LogEntry l GROUP BY l.endpoint ORDER BY COUNT(l) DESC")
    List<Object[]> findTopEndpoints();

    @Query("SELECT l.statusCode, COUNT(l) FROM LogEntry l WHERE l.clientId = :clientId GROUP BY l.statusCode")
    List<Object[]> countStatusCodesByClientId(String clientId);

    // Check whether any entries exist for a given clientId
    boolean existsByClientId(String clientId);

  @Query(value = """
  SELECT strftime('%Y-%m-%d %H:00:00', timestamp / 1000, 'unixepoch') AS hour,
         COUNT(*) 
  FROM log_entries 
  WHERE client_id = :clientId 
  GROUP BY hour 
  ORDER BY hour
  """, nativeQuery = true)
  List<Object[]> countRequestsByHour(String clientId);


  @Query(value = """
  SELECT strftime('%Y-%m-%d %H:00:00', timestamp / 1000, 'unixepoch') AS hour,
         SUM(CASE WHEN status_code BETWEEN 400 AND 499 THEN 1 ELSE 0 END) AS count_4xx,
         SUM(CASE WHEN status_code BETWEEN 500 AND 599 THEN 1 ELSE 0 END) AS count_5xx
  FROM log_entries 
  GROUP BY hour 
  ORDER BY hour
  """, nativeQuery = true)
  List<Object[]> countErrorCodesByHour();
}