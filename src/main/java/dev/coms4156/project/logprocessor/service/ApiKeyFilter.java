package dev.coms4156.project.logprocessor.service;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This file enforces API key authentication usage for all incoming requests.
 */
@Component
public class ApiKeyFilter implements Filter {

  @Value("${API_KEY}")
  private String apiKey;

  @Override
  public void doFilter(
      ServletRequest request,
      ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpReq = (HttpServletRequest) request;
    String providedKey = httpReq.getHeader("x-api-key");

    if (apiKey != null && apiKey.equals(providedKey)) {
      chain.doFilter(request, response);
    } else {
      HttpServletResponse httpRes = (HttpServletResponse) response;
      httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      httpRes.getWriter().write("Unauthorized: missing or invalid API key");
    }
  }
}
