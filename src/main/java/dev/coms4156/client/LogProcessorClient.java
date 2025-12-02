package dev.coms4156.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.UnknownContentTypeException;
import java.util.Set;
import java.util.TreeMap;
import java.awt.*;


// This class, especially the plots, where aided by ChatGPT
public class LogProcessorClient {

  private final RestClient restClient;

  public LogProcessorClient(String baseUrl, String apiKey) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("x-api-key", apiKey)
        .build();
  }

  public Map<String, Integer> getStatusCodeCounts(String clientId) {
    return restClient.get()
        .uri("/logs/statusCodeCounts?clientId=" + clientId)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Integer>>() {
        });
  }

  public Map<String, Integer> getRequestCountsByHour(String clientId) {
    return restClient.get()
        .uri("/analytics/timeseries/requests/" + clientId)
        .retrieve()
        .body(new ParameterizedTypeReference<Map<String, Integer>>() {
        });
  }

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

  public static void plotSuspiciousHours(Map<String, Integer> hourly, List<Map<String, Object>> suspicious) {
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

  public static void main(String[] args) {
    LogProcessorClient client = new LogProcessorClient(
        "https://logprocessor-service-445982800820.us-central1.run.app",
        "0f701e92a83f616ce4dbe35f9213d976dc7806c0f081017b1fc4ede0a43e566c");

    Map<String, Integer> result = client.getStatusCodeCounts("fullDayClient5");
    System.out.println("Status Code Counts:");
    result.forEach((code, count) -> System.out.println(code + ": " + count));
    plotStatusCodes(result);

    Map<String, Integer> hourly = client.getRequestCountsByHour("fullDayClient5");
    System.out.println("Hourly Request Counts:");
    hourly.forEach((hour, count) -> System.out.println(hour + ": " + count));

    List<Map<String, Object>> suspicious = client.getSuspiciousIps("fullDayClient5");
    System.out.println("Suspicious IPs (5+ auth errors in an hour window):");
    if (suspicious.isEmpty()) {
      System.out.println("No suspicious IPs found.");
    } else {
      suspicious.forEach(entry -> {
        System.out.println(
            entry.get("hourWindow") + " | " +
                entry.get("ipAddress") + " | " +
                entry.get("errorCount"));
      });
    }
    plotSuspiciousHours(hourly, suspicious);
  }
}
