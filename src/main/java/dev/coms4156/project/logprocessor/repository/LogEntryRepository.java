package dev.coms4156.project.logprocessor.repository;

import dev.coms4156.project.logprocessor.model.LogEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for communicating with the database. Jpa framework allows for some
 * default
 * queries without writing SQL.
 */
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {



  @Query("SELECT l.endpoint, COUNT(l) FROM LogEntry l GROUP BY l.endpoint ORDER BY COUNT(l) DESC")
  List<Object[]> findTopEndpoints();

  @Query("SELECT l.statusCode, COUNT(l) "
        + "FROM LogEntry l WHERE l.clientId = :clientId GROUP BY l.statusCode")
  List<Object[]> countStatusCodesByClientId(String clientId);

  // Check whether any entries exist for a given clientId
  boolean existsByClientId(String clientId);
  
  @Transactional
  @Modifying
  void deleteByClientId(String clientId);

  @Query("""
        SELECT l.hourWindow, COUNT(l) 
        FROM LogEntry l 
        WHERE l.clientId = :clientId 
        GROUP BY l.hourWindow 
        ORDER BY l.hourWindow
        """)
  List<Object[]> countRequestsByHour(@Param("clientId") String clientId);

  @Query("""
        SELECT l.hourWindow, SUM(CASE WHEN l.statusCode BETWEEN 400 AND 499 THEN 1 ELSE 0 END), 
        SUM(CASE WHEN l.statusCode BETWEEN 500 AND 599 THEN 1 ELSE 0 END)
        FROM LogEntry l
        WHERE l.clientId = :clientId
        GROUP BY l.hourWindow
        ORDER BY l.hourWindow
        """)
  List<Object[]> countErrorCodesByHour(@Param("clientId") String clientId);

  // Generated with ChatGPT
  @Query("""
      SELECT l.ipAddress, l.hourWindow, COUNT(l)
      FROM LogEntry l
      WHERE l.statusCode IN (401, 403) AND l.clientId = :clientId
      GROUP BY l.ipAddress, l.hourWindow
      HAVING COUNT(l) >= :threshold
      """)
  List<Object[]> findIpsWithManyAuthErrors(@Param("threshold") int threshold,
      @Param("clientId") String clientId);

  @Query("""
    SELECT l.endpoint,
           l.hourWindow,
           COUNT(l),
           SUM(CASE WHEN l.statusCode BETWEEN 400 AND 599 THEN 1 ELSE 0 END)
    FROM LogEntry l
    WHERE l.clientId = :clientId
    GROUP BY l.endpoint, l.hourWindow
    ORDER BY l.hourWindow
    """)
  List<Object[]> countEndpointHealthByHour(@Param("clientId") String clientId);
}
