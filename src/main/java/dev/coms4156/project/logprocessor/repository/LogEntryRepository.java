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
}