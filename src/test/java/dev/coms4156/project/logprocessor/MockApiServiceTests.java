package dev.coms4156.project.logprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.coms4156.project.logprocessor.model.Book;
import dev.coms4156.project.logprocessor.service.MockApiService;
import org.junit.jupiter.api.Test;

/**
 * This class contains the unit tests for the MockApiService class.
 */
public class MockApiServiceTests {
    
  @Test
  public void constructorTest() {
    MockApiService mockApiService = new MockApiService();
    assertNotNull(mockApiService.getBooks());
    assertEquals(50, mockApiService.getBooks().size());
  }

  @Test
  public void updateBookDefaultTest() {
    MockApiService mockApiService = new MockApiService();
    Book newBook = new Book("We got a new book in town!", 1);
    mockApiService.updateBook(newBook);
    assertEquals("We got a new book in town!", mockApiService.getBooks().get(0).getTitle());
  }

  @Test
  public void updateBookUnknownIdTest() {
    MockApiService mockApiService = new MockApiService();
    Book newBook = new Book("We got a new book in town!", 51);
    mockApiService.updateBook(newBook);
    assertEquals("Hanʼguk kŭndae ŭihak ŭi kiwŏn /", 
        mockApiService.getBooks().get(0).getTitle());
  }
}
