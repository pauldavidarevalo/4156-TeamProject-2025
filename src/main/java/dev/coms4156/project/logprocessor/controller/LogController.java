package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Upload a whole log file. Expects multipart/form-data with a "file" part.
     * Returns the stored filename.
     */
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadLogFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>("No file provided.", HttpStatus.BAD_REQUEST);
        }

        try {
            String stored = logService.saveUploadedFile(file);
            return new ResponseEntity<>("Stored as: " + stored, HttpStatus.OK);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>("Failed to store uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Accept plain text body and store as a new log file named by timestamp.
     */
    @PostMapping(value = "/uploadText", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> receiveRawLog(@RequestBody String rawLog) {
        try {
            String filename = logService.saveRawAsFile(rawLog);
            return new ResponseEntity<>("Stored as: " + filename, HttpStatus.OK);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>("Failed to store log.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * List all stored log files.
     */
    @GetMapping("/logs")
    public ResponseEntity<?> listLogs() {
        try {
            List<String> files = logService.listLogFiles();
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Logs directory not found.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get the content of a specific log file.
     */
    @GetMapping(path = "/logs/{filename}")
    public ResponseEntity<?> getLog(@PathVariable String filename) {
        try {
            String content = logService.readLogFile(filename);
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);
        } catch (IOException e) {
            return new ResponseEntity<>("Log not found.", HttpStatus.NOT_FOUND);
        }
    }

    //For upload using new Log Entry class and new LogService logic
    @PostMapping("/upload")
    public String uploadLog(@RequestParam("clientId") String clientId, @RequestParam("file") MultipartFile file) {
        try {
            logService.processLogFile(file.getInputStream(), clientId);
            return "Log file processed successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing log file: " + e.getMessage();
        }
    }
}
