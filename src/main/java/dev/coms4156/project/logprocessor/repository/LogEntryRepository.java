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
     SELECT FUNCTION('FORMATDATETIME', l.timestamp, 'yyyy-MM-dd HH:00:00'), COUNT(l)
     FROM LogEntry l
     WHERE l.clientId = :clientId
     GROUP BY FUNCTION('FORMATDATETIME', l.timestamp, 'yyyy-MM-dd HH:00:00')
     ORDER BY FUNCTION('FORMATDATETIME', l.timestamp, 'yyyy-MM-dd HH:00:00')
     """)
  List<Object[]> countRequestsByHour(String clientId);

  // Count 4xx/5xx errors by hour (system-wide)
  @Query("""
     SELECT FUNCTION('FORMATDATETIME', l.timestamp, 'yyyy-MM-dd HH:00:00'),
            SUM(CASE WHEN l.statusCode BETWEEN 400 AND 499 THEN 1 ELSE 0 END),
            SUM(CASE WHEN l.statusCode BETWEEN 500 AND 599 THEN 1 ELSE 0 END)
     FROM LogEntry l
     GROUP BY FUNCTION('FORMATDATETIME', l.timestamp, 'yyyy-MM-dd HH:00:00')
     ORDER BY FUNCTION('FORMATDATETIME', l.timestamp, 'yyyy-MM-dd HH:00:00')
     """)
  List<Object[]> countErrorCodesByHour();

    // Check whether any entries exist for a given clientId
    boolean existsByClientId(String clientId);
}