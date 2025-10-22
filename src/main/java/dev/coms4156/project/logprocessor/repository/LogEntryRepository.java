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


  // Count total requests by hour for a specific client
  @Query("""
       SELECT strftime('%Y-%m-%d %H:00:00', l.timestamp), COUNT(l)
       FROM LogEntry l
       WHERE l.clientId = :clientId
       GROUP BY strftime('%Y-%m-%d %H:00:00', l.timestamp)
       ORDER BY strftime('%Y-%m-%d %H:00:00', l.timestamp)
       """)
  List<Object[]> countRequestsByHour(String clientId);

  // Count 4xx/5xx errors by hour (system-wide)
  @Query("""
       SELECT strftime('%Y-%m-%d %H:00:00', l.timestamp),
              SUM(CASE WHEN l.statusCode BETWEEN 400 AND 499 THEN 1 ELSE 0 END),
              SUM(CASE WHEN l.statusCode BETWEEN 500 AND 599 THEN 1 ELSE 0 END)
       FROM LogEntry l
       GROUP BY strftime('%Y-%m-%d %H:00:00', l.timestamp)
       ORDER BY strftime('%Y-%m-%d %H:00:00', l.timestamp)
       """)
  List<Object[]> countErrorCodesByHour();

    // Check whether any entries exist for a given clientId
    boolean existsByClientId(String clientId);
}