package dev.coms4156.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
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
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
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
   * Gets error counts by hour from the /analytics/timeseries/error-counts endpoint.
   */
  public Map<String, Map<String, Integer>> getErrorCountsByHour(String clientId) {
    return restClient.get()
        .uri("/analytics/timeseries/error-counts/" + clientId)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Map<String, Integer>>>() {});
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
   * Plots hourly request counts, highlighting hours with suspicious activity and identified IPs.
   */
public static void plotSuspiciousHours(
        Map<String, Integer> hourly,
        List<Map<String, Object>> suspicious) {

    // Map each hour to a set of suspicious IPs
    Map<String, Set<String>> suspiciousMap = new HashMap<>();
    for (Map<String, Object> entry : suspicious) {
        String hour = (String) entry.get("hourWindow");
        String ip = (String) entry.get("ipAddress");
        suspiciousMap.computeIfAbsent(hour, k -> new HashSet<>()).add(ip);
    }

    // Dataset
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    hourly.forEach((hour, count) -> dataset.addValue(count, "Requests", hour));

    // Create chart
    JFreeChart chart = ChartFactory.createBarChart(
            "Requests per Hour (highlight suspicious hours)",
            "Hour",
            "Count",
            dataset
    );

    // Customize colors
    CategoryPlot plot = chart.getCategoryPlot();
    BarRenderer renderer = new BarRenderer() {
        @Override
        public Paint getItemPaint(int row, int column) {
            String hour = (String) dataset.getColumnKey(column);
            return suspiciousMap.containsKey(hour) ? Color.RED : Color.BLUE;
        }

        @Override
        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                             Rectangle2D dataArea, CategoryPlot plot,
                             CategoryAxis domainAxis, ValueAxis rangeAxis,
                             CategoryDataset dataset, int row, int column,
                             int pass) {

            super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass);

            // Draw IP labels above bars for suspicious hours
            String hour = (String) dataset.getColumnKey(column);
            if (suspiciousMap.containsKey(hour)) {
                Number value = dataset.getValue(row, column);
                String label = String.join(", ", suspiciousMap.get(hour));
                double x = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
                double y = rangeAxis.valueToJava2D(value.doubleValue(), dataArea, plot.getRangeAxisEdge()) - 5;
                g2.setFont(g2.getFont().deriveFont(10f));
                g2.setPaint(Color.BLACK);
                g2.drawString(label, (float) x - (label.length() * 2), (float) y);
            }
        }
    };

    // Create custom legend
    LegendItemCollection legendItems = new LegendItemCollection();
    legendItems.add(new LegendItem("Healthy activity", Color.BLUE));
    legendItems.add(new LegendItem("Suspicious IP identified", Color.RED));
    plot.setFixedLegendItems(legendItems);
    plot.setRenderer(renderer);

    // Show chart in a window
    JFrame frame = new JFrame("Requests Chart with Suspicious IPs");
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
    // Compute health score
    int total = statusCounts.values().stream().mapToInt(Integer::intValue).sum();
    int errorCount = statusCounts.entrySet().stream()
            .filter(e -> e.getKey().startsWith("4") || e.getKey().startsWith("5"))
            .mapToInt(Map.Entry::getValue)
            .sum();
    double healthScore = ((double)(total - errorCount) / total) * 100.0;

    Map<String, Integer> sorted = new TreeMap<>(statusCounts);

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    sorted.forEach((code, count) -> dataset.addValue(count, "HTTP Status Codes", code));

    JFreeChart chart = ChartFactory.createBarChart(
        "HTTP Status Codes Count - Health Score: " + String.format("%.2f", healthScore) 
        + "% successful requests",
        "Status Code",
        "Count",
        dataset);

    CategoryPlot plot = chart.getCategoryPlot();
    BarRenderer renderer = new BarRenderer() {
      @Override
      public Paint getItemPaint(int row, int column) {
        String code = (String) dataset.getColumnKey(column);
        if (code.startsWith("4") || code.startsWith("5")) {
          return Color.RED; // Error codes
        } else {
          return Color.BLUE; // Success codes
        }
      }
    };
    plot.setRenderer(renderer);

    // Create custom legend
    LegendItemCollection legendItems = new LegendItemCollection();
    legendItems.add(new LegendItem("Success (1xx-3xx)", Color.BLUE));
    legendItems.add(new LegendItem("Error (4xx-5xx)", Color.RED));
    plot.setFixedLegendItems(legendItems);

    // Show chart
    JFrame frame = new JFrame("Status Codes");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ChartPanel(chart));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public void plotTimeSeriesWithErrors(String clientId, Map<String, Map<String, Integer>> errorsByHour,
      Map<String, Integer> requestsByHour) {
    // Compute total errors per hour (sum of 4xx + 5xx)
    Map<String, Integer> totalErrorsByHour = new TreeMap<>();
    for (String hour : errorsByHour.keySet()) {
        Map<String, Integer> hourErrors = errorsByHour.get(hour);
        int sum = hourErrors.getOrDefault("4xx", 0) + hourErrors.getOrDefault("5xx", 0);
        totalErrorsByHour.put(hour, sum);
    }

    // Create dataset for plotting
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (String hour : requestsByHour.keySet()) {
        int reqCount = requestsByHour.get(hour);
        int errCount = totalErrorsByHour.getOrDefault(hour, 0);

        dataset.addValue(reqCount, "Total Requests", hour);
        dataset.addValue(errCount, "Total Errors", hour);
    }

    // Create chart
    JFreeChart chart = ChartFactory.createLineChart(
            "Hourly Requests vs Errors",
            "Hour",
            "Count",
            dataset
    );

    // Customize line colors
    CategoryPlot plot = chart.getCategoryPlot();
    LineAndShapeRenderer renderer = new LineAndShapeRenderer();
    renderer.setSeriesPaint(0, Color.BLUE); // Requests
    renderer.setSeriesPaint(1, Color.RED);  // Errors
    renderer.setSeriesStroke(0, new BasicStroke(2.0f));
    renderer.setSeriesStroke(1, new BasicStroke(2.0f));
    plot.setRenderer(renderer);

    // Show chart
    JFrame frame = new JFrame("Time Series Analysis");
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
        System.out.print("Enter path to log file (or type 'quit' to begin processing): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("quit") || input.isEmpty()) {
            System.out.println("Exiting log upload loop.");
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

    String response = client.resetLogs(clientId);
    System.out.println(response);

    client.uploadLogsLoop(scanner, clientId);
    scanner.close();

    Map<String, Integer> result = client.getStatusCodeCounts(clientId);
    plotStatusCodes(result);

    Map<String, Integer> hourly = client.getRequestCountsByHour(clientId);
    List<Map<String, Object>> suspicious = client.getSuspiciousIps(clientId);
    System.out.println("Suspicious IPs identified:");
    if (suspicious.isEmpty()) {
      System.out.println("No suspicious IPs identified:");
    } else {
      suspicious.forEach(entry -> {
        System.out.println(
            entry.get("hourWindow") + " | "
                + entry.get("ipAddress") + " | "
                + "total suspicious requests: " + entry.get("errorCount"));
      });
    }
    plotSuspiciousHours(hourly, suspicious);

    Map<String, Map<String, Integer>> errorsByHour = client.getErrorCountsByHour(clientId);
    client.plotTimeSeriesWithErrors(clientId, errorsByHour, hourly);
  }
}
