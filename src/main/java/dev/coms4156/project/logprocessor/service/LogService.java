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

    public Object getTopEndpoints() {
        return repo.findTopEndpoints();
    }

}