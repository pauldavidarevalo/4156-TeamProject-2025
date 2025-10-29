package dev.coms4156.project.logprocessor.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import dev.coms4156.project.logprocessor.service.LogService;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * This file represents the endpoints associated with the LogService controller.
 */
@RestController
@RequestMapping("/logs")
public class LogController {

  private final LogService logService;

  public LogController(LogService logService) {
    this.logService = logService;
  }

  /**
   * For client log upload into service.
   * Returns 200 OK on success, 400 BAD REQUEST if clientId or file are blank or file
   * cannot be parsed as a .log file.
   *
   * @param clientId ID input by the client to differentiate rows in the database
   * @param file apache log file in simple format to be parsed
   * @return confirmation message
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadLog(@RequestParam("clientId") String clientId,
                          @RequestParam("file") MultipartFile file) {
    if (clientId.isBlank()) {
      return new ResponseEntity<>("clientId cannot be blank.", BAD_REQUEST);
    }

    if (file.isEmpty()) {
      return new ResponseEntity<>("file cannot be empty.", BAD_REQUEST);
    }

    String filename = file.getOriginalFilename();
    if (filename == null || !filename.toLowerCase(Locale.US).endsWith(".log")) {
      return new ResponseEntity<>("only .log files are accepted.", BAD_REQUEST);
    }
    try {
      logService.processLogFile(file.getInputStream(), clientId);
      return new ResponseEntity<>("Log file processed successfully.", OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Error processing log file: " + e.getMessage(), BAD_REQUEST);
    }
  }

  /**
   * This endpoint stores the frequency of each status code shown after processing.
   *
   * @param clientId ID input by the client to filter status codes.
   * @return ResponseEntity containing counts of status codes for the given clientId,
   *     or an error message if the clientId is not found.
   */
  @GetMapping("/statusCodeCounts")
  public ResponseEntity<?> getStatusCodeCounts(@RequestParam("clientId") String clientId) {
    if (!logService.clientExists(clientId)) {
      return new ResponseEntity<>("Error: clientId not found", NOT_FOUND);
    }
    Map<Integer, Integer> counts = logService.countStatusCodesForClient(clientId);
    return new ResponseEntity<>(counts, OK);
  }
}
