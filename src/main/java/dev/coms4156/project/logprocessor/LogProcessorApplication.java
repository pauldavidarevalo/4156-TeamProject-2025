package dev.coms4156.project.logprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is where the Spring Boot application for the Log Processor service is run.
 */
@SpringBootApplication
public class LogProcessorApplication {

  public static void main(String[] args) {
    SpringApplication.run(LogProcessorApplication.class, args);
  }

}
