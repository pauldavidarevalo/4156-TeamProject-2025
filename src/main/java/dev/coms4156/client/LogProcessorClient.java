package dev.coms4156.client;

import java.awt.Color;
import java.awt.Paint;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.UnknownContentTypeException;

/**
 * This client uses the timeseries and security endpoints of the LogProcessor
 * service.
 * It combines fetching data from these endpoints to generate plots.
 * This class, especially the plots, were aided by ChatGPT.
 *
 */
public class LogProcessorClient {
  private final RestClient restClient;

  /**
   * Constructor for LogProcessorClient.
   */
  public LogProcessorClient(String baseUrl, String apiKey) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("x-api-key", apiKey)
        .build();
  }

  /**
   * Resets log entries for a specific clientId
   */
  public String resetLogs(String clientId) {
    return restClient.post()
        .uri("/logs/reset?clientId={clientId}", clientId)
        .retrieve()
        .body(String.class);
  }

  /**
   * Uploads a log file to the /logs/upload endpoint.
   */
  public String uploadLogFile(String clientId, Path logFilePath) {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("clientId", clientId);
      body.add("file", new FileSystemResource(logFilePath.toFile()));

      return restClient.post()
          .uri("/logs/upload")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(body)
          .retrieve()
          .body(String.class);
  }

  /**
   * Gets status code counts from the /logs/statusCodeCounts endpoint.
   */
  public Map<String, Integer> getStatusCodeCounts(String clientId) {
    return restClient.get()
        .uri("/logs/statusCodeCounts?clientId=" + clientId)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Integer>>() {
        });
  }

  /**
   * Gets request counts by hour from the /analytics/timeseries/requests endpoint.
   */
  public Map<String, Integer> getRequestCountsByHour(String clientId) {
    return restClient.get()
        .uri("/analytics/timeseries/requests/" + clientId)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Integer>>() {
        });
  }

  /**
   * Gets suspicious IPs from the /security/suspicious-ips endpoint.
   */
  public List<Map<String, Object>> getSuspiciousIps(String clientId) {
    try {
      return restClient.get()
          .uri("/security/suspicious-ips/" + clientId)
          .retrieve()
          .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
          });
    } catch (HttpClientErrorException.NotFound e) {
      System.err.println("ClientId not found: " + clientId);
      return List.of();
    } catch (UnknownContentTypeException e) {
      System.out.println("No suspicious IPs found for client: " + clientId);
      return List.of();
    }
  }

  /**
   * Plots hourly request counts, highlighting hours with suspicious activity.
   */
  public static void plotSuspiciousHours(
        Map<String, Integer> hourly, List<Map<String, Object>> suspicious) {
    Set<String> suspiciousHours = suspicious.stream()
        .map(entry -> (String) entry.get("hourWindow"))
        .collect(Collectors.toSet());

    // Dataset
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    hourly.forEach((hour, count) -> dataset.addValue(count, "Requests", hour));

    // Create chart
    JFreeChart chart = ChartFactory.createBarChart(
        "Requests per Hour (highlight suspicious hours)",
        "Hour",
        "Count",
        dataset);

    // Customize colors
    CategoryPlot plot = chart.getCategoryPlot();
    BarRenderer renderer = new BarRenderer() {
      @Override
      public Paint getItemPaint(int row, int column) {
        String hour = (String) dataset.getColumnKey(column);
        if (suspiciousHours.contains(hour)) {
          return Color.RED; // highlight suspicious
        } else {
          return Color.BLUE; // normal
        }
      }
    };
    plot.setRenderer(renderer);

    // Show chart in a window
    JFrame frame = new JFrame("Requests Chart");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ChartPanel(chart));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Plots status code counts as a bar chart, coloring 4xx/5xx codes red.
   */
  public static void plotStatusCodes(Map<String, Integer> statusCounts) {
    // Sort the status codes numerically (low → high)
    Map<String, Integer> sorted = new TreeMap<>(statusCounts);

    // Create dataset
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    sorted.forEach((code, count) -> dataset.addValue(count, "Status Codes", code));

    // Create chart
    JFreeChart chart = ChartFactory.createBarChart(
        "HTTP Status Codes Count",
        "Status Code",
        "Count",
        dataset);

    // Customize colors: red for 4xx/5xx, blue for others
    CategoryPlot plot = chart.getCategoryPlot();
    BarRenderer renderer = new BarRenderer() {
      @Override
      public Paint getItemPaint(int row, int column) {
        String code = (String) dataset.getColumnKey(column);
        if (code.startsWith("4") || code.startsWith("5")) {
          return Color.RED;
        } else {
          return Color.BLUE;
        }
      }
    };
    plot.setRenderer(renderer);

    // Show chart
    JFrame frame = new JFrame("Status Codes");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ChartPanel(chart));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Loop to upload log files based on user input.
   */
  public void uploadLogsLoop(Scanner scanner, String clientId) {
    while (true) {
        System.out.print("Enter path to log file (or type 'quit' to stop): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("quit") || input.isEmpty()) {
            System.out.println("Exiting log upload loop.");
            scanner.close();
            break;
        }

        Path logFile = Path.of(input);
        if (!Files.exists(logFile)) {
            System.out.println("File does not exist. Try again.");
            continue;
        }

        try {
            String response = uploadLogFile(clientId, logFile);
            System.out.println("Upload response: " + response);
        } catch (Exception e) {
            System.out.println("Failed to upload log file: " + e.getMessage());
        }
    }
    scanner.close();
}

  /**
   * Main method to run the client and display plots.
   */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Client ID: ");
    String clientId = scanner.nextLine().trim();
    System.out.print("Enter API Key: ");
    String apiKey = scanner.nextLine().trim();

    LogProcessorClient client = new LogProcessorClient(
        "https://logprocessor-service-445982800820.us-central1.run.app",
        apiKey);

    String response = client.resetLogs("fullDayClient");
    System.out.println(response);

    client.uploadLogsLoop(scanner, clientId);
    scanner.close();

    response = client.uploadLogFile(
        "fullDayClient",
        Path.of("sampleLogs/sampleApacheSimpleFullDay.log"));
    System.out.println(response);

    Map<String, Integer> result = client.getStatusCodeCounts(clientId);
    System.out.println("Status Code Counts:");
    result.forEach((code, count) -> System.out.println(code + ": " + count));
    plotStatusCodes(result);

    Map<String, Integer> hourly = client.getRequestCountsByHour(clientId);
    System.out.println("Hourly Request Counts:");
    hourly.forEach((hour, count) -> System.out.println(hour + ": " + count));

    List<Map<String, Object>> suspicious = client.getSuspiciousIps(clientId);
    System.out.println("Suspicious IPs (5+ auth errors in an hour window):");
    if (suspicious.isEmpty()) {
      System.out.println("No suspicious IPs found.");
    } else {
      suspicious.forEach(entry -> {
        System.out.println(
            entry.get("hourWindow") + " | "
                + entry.get("ipAddress") + " | "
                + entry.get("errorCount"));
      });
    }
    plotSuspiciousHours(hourly, suspicious);
  }
}
