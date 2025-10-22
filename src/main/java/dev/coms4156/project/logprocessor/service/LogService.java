package dev.coms4156.project.logprocessor.service;

import dev.coms4156.project.logprocessor.model.LogEntry;
import dev.coms4156.project.logprocessor.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
public class LogService {


    private final LogEntryRepository repo;

    public LogService(LogEntryRepository repo) {
        this.repo = repo;
    }

    // Simple Apache log pattern (combined format)
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[(.+?)\\] \"(\\S+) (\\S+) \\S+\" (\\d{3}) (\\d+|-)"
    );

    /**
     * Parses each log file extracting each component and creates a new LogEntry object.
     * That LogEntry is saved into a LogEntryRepository which transfers its data into the
     * database.
     * @param fileStream input stream of the file given by the client
     * @param clientId ID input by the client
     * @throws Exception thrown if input stream cannot be read
     */
    public void processLogFile(InputStream fileStream, String clientId) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    String time = matcher.group(2);
                    String method = matcher.group(3);
                    String endpoint = matcher.group(4);
                    int status = Integer.parseInt(matcher.group(5));
                    long size = matcher.group(6).equals("-") ? 0 : Long.parseLong(matcher.group(6));

                    // Simplified timestamp parsing
                    LocalDateTime timestamp = LocalDateTime.now();

                    LogEntry entry = new LogEntry(clientId, ip, method, endpoint, status, size, timestamp);
                    repo.save(entry);
                }
            }
        }
    }

    /**
     * Sample method analytics to be replaced by security endpoint features
     * @return object
     */
    public Object getTopEndpoints() {
        return repo.findTopEndpoints();
    }

    /**
     * Count status codes for entries matching the provided clientId.
     * Returns a mapping from status code to count.
     */
    public Map<Integer, Integer> countStatusCodesForClient(String clientId) {
        Map<Integer, Integer> result = new HashMap<>();
        List<Object[]> rows = repo.countStatusCodesByClientId(clientId);
        for (Object[] row : rows) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            result.put(status, count.intValue());
        }
        return result;
    }

    /**
     * Returns true if at least one LogEntry exists for the provided clientId.
     */
    public boolean clientExists(String clientId) {
        return repo.existsByClientId(clientId);
    }

}