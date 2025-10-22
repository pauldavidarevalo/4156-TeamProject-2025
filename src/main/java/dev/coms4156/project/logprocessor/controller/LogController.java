package dev.coms4156.project.logprocessor.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import dev.coms4156.project.logprocessor.service.LogService;
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
   *
   * @param clientId ID input by the client to differentiate rows in the database
   * @param file apache log file in simple format to be parsed
   * @return confirmation message
   */
  @PostMapping("/upload")
  public String uploadLog(@RequestParam("clientId") String clientId,
                          @RequestParam("file") MultipartFile file) {
    try {
      logService.processLogFile(file.getInputStream(), clientId);
      return "Log file processed successfully.";
    } catch (Exception e) {
      e.printStackTrace();
      return "Error processing log file: " + e.getMessage();
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
