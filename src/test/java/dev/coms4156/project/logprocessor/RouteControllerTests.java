package dev.coms4156.project.logprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

import dev.coms4156.project.logprocessor.model.Book;
import dev.coms4156.project.logprocessor.service.MockApiService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * This class contains tests for the RouteController class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RouteControllerTests {
  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @SpyBean
  private MockApiService mock; // wraps the real bean

  @BeforeEach
  void resetBooks() {
    // Resets the books list before each test. Preferable to restarting the service each test
    ReflectionTestUtils.setField(mock, "books", new MockApiService().getBooks());
  }

  @Test
  void getBook2Test() {
    String url = "http://localhost:" + port + "/book/2";
    ResponseEntity<Book> response = restTemplate.getForEntity(url, Book.class);
    Book book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(2, book.getId());
    assertEquals("All the mighty world :", book.getTitle());
  }
  
  @Test
  void getBookBadIdTest() {
    String url = "http://localhost:" + port + "/book/-1";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    String responseMessage = response.getBody();
    assertEquals(404, response.getStatusCode().value());
    assertEquals("Book not found.", responseMessage);
  }

  @Test
  void indexSlashTest() {
    String url = "http://localhost:" + port + "/";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    String responseMessage = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals("Welcome to the home page! In order to make an API call direct your browser"
        + "or Postman to an endpoint.", responseMessage);
  }

  @Test
  void indexSlashIndexTest() {
    String url = "http://localhost:" + port + "/index";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    String responseMessage = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals("Welcome to the home page! In order to make an API call direct your browser"
        + "or Postman to an endpoint.", responseMessage);
  }

  @Test
  void getAvailableBooksTest() {
    String url = "http://localhost:" + port + "/books/available";
    ResponseEntity<List<Book>> response = restTemplate.exchange(
        url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Book>>() {});
    List<Book> books = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(50, books.size());
  }

  @Test
  void getAvailableBooksAfterRemovedCopyTest() {
    mock.getBooks().get(0).setCopiesAvailable(0);
    String url = "http://localhost:" + port + "/books/available";
    ResponseEntity<List<Book>> response = restTemplate.exchange(
        url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Book>>() {});
    List<Book> books = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(49, books.size());
  }

  @Test
  void getAvailableBooksErrorTest() {
    doThrow(new RuntimeException("Mocked error message")).when(mock).getBooks();
    String url = "http://localhost:" + port + "/books/available";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    String responseMessage = response.getBody();
    assertEquals("Error occurred when getting all available books.", responseMessage);
    assertEquals(500, response.getStatusCode().value());
  }

  @Test
  void addCopyTest() {
    String url = "http://localhost:" + port + "/book/1/add";
    ResponseEntity<Book> response = restTemplate.exchange(
        url, HttpMethod.PATCH, null, Book.class);
    Book book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(2, book.getCopiesAvailable());
    assertEquals(2, book.getTotalCopies());
  }

  @Test
  void addCopyMultipleTest() {
    String url = "http://localhost:" + port + "/book/1/add";
    ResponseEntity<Book> response = restTemplate.exchange(
        url, HttpMethod.PATCH, null, Book.class);
    Book book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(2, book.getCopiesAvailable());
    assertEquals(2, book.getTotalCopies());

    response = restTemplate.exchange(url, HttpMethod.PATCH, null, Book.class);
    book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(3, book.getCopiesAvailable());
    assertEquals(3, book.getTotalCopies());
  }

  @Test
  void addCopyBadIdTest() {
    String url = "http://localhost:" + port + "/book/-1/add";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.PATCH, null, String.class);
    String responseMessage = response.getBody();
    assertEquals(404, response.getStatusCode().value());
    assertEquals("Book not found.", responseMessage);
  }

  @Test
  void addCopyErrorTest() {
    doThrow(new RuntimeException("Mocked error message")).when(mock).getBooks();
    String url = "http://localhost:" + port + "/book/1/add";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.PATCH, null, String.class);
    String responseMessage = response.getBody();
    assertEquals("Error occurred when adding a copy.", responseMessage);
    assertEquals(500, response.getStatusCode().value());
  }

  @Test
  void getRecommendationsDefaultTest() {
    List<Book> books = mock.getBooks();
    final Book mostPopular = books.stream().max(
        Comparator.comparingInt(Book::getAmountOfTimesCheckedOut)).orElse(null);

    String url = "http://localhost:" + port + "/books/recommendation";
    ResponseEntity<List<Book>> response = restTemplate.exchange(
        url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Book>>() {});
    List<Book> recommendations = response.getBody();
    long uniqueRecommendations = recommendations.stream().map(Book::getId).distinct().count();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(10, recommendations.size());
    assertEquals(10, uniqueRecommendations);
    assertTrue(recommendations.contains(mostPopular));
  }

  @Test
  void getRecommendationsSmallListTest() {
    List<Book> books = mock.getBooks();
    mock.getBooks().removeIf(book -> book.getId() <= 45);
    final Book mostPopular = books.stream().max(
        Comparator.comparingInt(Book::getAmountOfTimesCheckedOut)).orElse(null);

    String url = "http://localhost:" + port + "/books/recommendation";
    ResponseEntity<List<Book>> response = restTemplate.exchange(
        url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Book>>() {});
    List<Book> recommendations = response.getBody();
    long uniqueRecommendations = recommendations.stream().map(Book::getId).distinct().count();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(5, recommendations.size());
    assertEquals(5, uniqueRecommendations);
    assertTrue(recommendations.contains(mostPopular));
  }

  @Test
  void getRecommendationsNoBooksTest() {
    List<Book> books = mock.getBooks();
    books.clear();

    String url = "http://localhost:" + port + "/books/recommendation";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.GET, null, String.class);
    String responseMessage = response.getBody();
    assertEquals("No books found.", responseMessage);
    assertEquals(404, response.getStatusCode().value());
  }

  @Test
  void getRecommendationsInternalErrorTest() {
    doThrow(new RuntimeException("Mocked error message")).when(mock).getBooks();
    
    String url = "http://localhost:" + port + "/books/recommendation";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.GET, null, String.class);
    String responseMessage = response.getBody();
    assertEquals("Error occurred when getting recommended books.", responseMessage);
    assertEquals(500, response.getStatusCode().value());
  }

  @Test
  void checkoutDefaultTest() {
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.plusWeeks(2);
    final String expectedReturnDate = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

    String url = "http://localhost:" + port + "/checkout?bookId=1";
    ResponseEntity<Book> response = restTemplate.exchange(
        url, HttpMethod.POST, null, Book.class);
    Book book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(expectedReturnDate, book.getReturnDates().get(0));
  }

  @Test
  void checkoutPreexistingReturnDatesTest() {
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.plusWeeks(2);
    final String expectedReturnDate = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

    String url = "http://localhost:" + port + "/checkout?bookId=50";
    ResponseEntity<Book> response = restTemplate.exchange(
        url, HttpMethod.POST, null, Book.class);
    Book book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(50, book.getId());
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(expectedReturnDate, book.getReturnDates().get(book.getReturnDates().size() - 1));
  }

  @Test
  void checkoutBookWithoutCopiesTest() {
    Book unavailableBook = mock.getBooks().get(0);
    unavailableBook.setCopiesAvailable(0);
    mock.updateBook(unavailableBook);
    String url = "http://localhost:" + port + "/checkout?bookId=1";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.POST, null, String.class);
    String responseMessage = response.getBody();
    assertEquals(409, response.getStatusCode().value());
    assertEquals("No copies available to checkout.", responseMessage);
  }

  @Test
  void checkoutAddCheckoutTest() {
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.plusWeeks(2);
    final String expectedReturnDate = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

    String url = "http://localhost:" + port + "/checkout?bookId=1";
    ResponseEntity<Book> response = restTemplate.exchange(
        url, HttpMethod.POST, null, Book.class);
    Book book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(expectedReturnDate, book.getReturnDates().get(0));

    url = "http://localhost:" + port + "/book/1/add";
    response = restTemplate.exchange(
        url, HttpMethod.PATCH, null, Book.class);
    book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(1, book.getCopiesAvailable());
    assertEquals(2, book.getTotalCopies());


    url = "http://localhost:" + port + "/checkout?bookId=1";
    response = restTemplate.exchange(
        url, HttpMethod.POST, null, Book.class);
    book = response.getBody();
    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, book.getId());
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(2, book.getTotalCopies());
    assertEquals(expectedReturnDate, book.getReturnDates().get(1));
  }

  @Test
  void checkoutBookMissingIdTest() {
    mock.getBooks().clear();

    String url = "http://localhost:" + port + "/checkout?bookId=1";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.POST, null, String.class);
    String responseMessage = response.getBody();
    assertEquals("No books found with id 1.", responseMessage);
    assertEquals(404, response.getStatusCode().value());
  }

  @Test
  void checkoutInternalServerErrorTest() {
    doThrow(new RuntimeException("Mocked error message")).when(mock).getBooks();

    String url = "http://localhost:" + port + "/checkout?bookId=1";
    ResponseEntity<String> response = restTemplate.exchange(
        url, HttpMethod.POST, null, String.class);
    String responseMessage = response.getBody();
    assertEquals("Error occurred when checking out a book.", responseMessage);
    assertEquals(500, response.getStatusCode().value());
  }
}