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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogController {

  private final LogService logService;

  public LogController(LogService logService) {
    this.logService = logService;
  }

  @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<?> receiveLog(@RequestBody String body) {
    try {
      logService.appendToDefaultLog(body.trim());
      return new ResponseEntity<>("Log stored.", HttpStatus.OK);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return new ResponseEntity<>("Failed to store log.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping
  public ResponseEntity<List<String>> listLogs() {
    return new ResponseEntity<>(logService.listLogFiles(), HttpStatus.OK);
  }

  @GetMapping(path = "/{filename}")
  public ResponseEntity<?> getLog(@PathVariable String filename) {
    try {
      String content = logService.readLogFile(filename);
      return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);
    } catch (IOException e) {
      return new ResponseEntity<>("Log not found.", HttpStatus.NOT_FOUND);
    }
  }
}
