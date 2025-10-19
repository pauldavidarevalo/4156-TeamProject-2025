package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.service.LogService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }


    /**For client log upload into service.
     * /
     * @param clientId ID input by the client to differentiate rows in the database
     * @param file apache log file in simple format to be parsed
     * @return confirmation message
     */
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
