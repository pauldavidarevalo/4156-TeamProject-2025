package dev.coms4156.project.logprocessor.controller;

import dev.coms4156.project.logprocessor.model.Book;
import dev.coms4156.project.logprocessor.service.MockApiService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class defines the Route Controller model.
 */
@RestController
public class RouteController {

  private final MockApiService mockApiService;

  public RouteController(MockApiService mockApiService) {
    this.mockApiService = mockApiService;
  }

  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the home page! In order to make an API call direct your browser"
        + "or Postman to an endpoint.";
  }

  /**
   * Returns the details of the specified book.
   *
   * @param id An {@code int} representing the unique identifier of the book to retrieve.
   *
   * @return A {@code ResponseEntity} containing either the matching {@code Book} object with an
   *         HTTP 200 response, or a message indicating that the book was not
   *         found with an HTTP 404 response.
   */
  @GetMapping({"/book/{id}"})
  public ResponseEntity<?> getBook(@PathVariable int id) {
    for (Book book : mockApiService.getBooks()) {
      if (book.getId() == id) {
        return new ResponseEntity<>(book, HttpStatus.OK);
      }
    }

    return new ResponseEntity<>("Book not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Get and return a list of all the books with available copies.
   *
   * @return A {@code ResponseEntity} containing a list of available {@code Book} objects with an
   *         HTTP 200 response if sucessful, or a message indicating an error occurred with an
   *         HTTP 500 response.
   */
  @GetMapping({"/books/available"})
  public ResponseEntity<?> getAvailableBooks() {
    try {
      List<Book> availableBooks = new ArrayList<>();

      for (Book book : mockApiService.getBooks()) {
        if (book.hasCopies()) {
          availableBooks.add(book);
        }
      }

      return new ResponseEntity<>(availableBooks, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting all available books.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Adds a copy to the {@code} Book object if it exists.
   *
   * @param bookId An {@code Integer} representing the unique id of the book.
   * @return A {@code ResponseEntity} containing the updated {@code Book} object with an
   *         HTTP 200 response if successful or HTTP 404 if the book is not found,
   *         or a message indicating an error occurred with an HTTP 500 code.
   */
  @PatchMapping({"/book/{bookId}/add"})
  public ResponseEntity<?> addCopy(@PathVariable Integer bookId) {
    try {
      for (Book book : mockApiService.getBooks()) {
        if (bookId.equals(book.getId())) {
          book.addCopy();
          return new ResponseEntity<>(book, HttpStatus.OK);
        }
      }
      return new ResponseEntity<>("Book not found.", HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when adding a copy.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Get and return a list of 10 recommended books. 
   *
   * @return A {@code ResponseEntity} containing a list of 10 recommended book {@code Book}
   *        objects. The first 5 books should be the most checked out and the other 5 
   *        should be randomly selected. If less than 10 books exist, return as many as possible.
   *        HTTP 200 response if successful.
   *        HTTP 404 if no books are found.
   *        HTTP 500 response if an error occurs.
   */
  @GetMapping({"/books/recommendation"})
  public ResponseEntity<?> getRecommendations() {
    try {
      List<Book> books = mockApiService.getBooks();
      if (books.isEmpty()) {
        return new ResponseEntity<>("No books found.", HttpStatus.NOT_FOUND);
      }
      Collections.sort(books, Comparator.comparingInt(Book::getAmountOfTimesCheckedOut).reversed());
      List<Book> recommendations = books.subList(0, Math.min(books.size(), 5));

      while (recommendations.size() < 10 && recommendations.size() < books.size()) {
        int randomId = (int) (Math.random() * books.size());
        if (!recommendations.contains(books.get(randomId))) {
          recommendations.add(books.get(randomId));
        }
      }
      return new ResponseEntity<>(recommendations, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting recommended books.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Updates internal booklist to reflect a specified books has been checked out. 
   *
   * @param bookId An {@code Integer} representing the unique id of the book.
   * @return A {@code ResponseEntity} containing either the updated {@code Book} object with an
  *         HTTP 200 response. If no book is found, HTTP 404 response. 
   *        If an error occurs getting the internal books list, a HTTP 505 response.
   *        If less than 10 books exist, return as many as possible.
   */
  @PostMapping("/checkout")
  public ResponseEntity<?> checkout(@RequestParam int bookId) {
    try {
      for (Book book : mockApiService.getBooks()) {
        if (book.getId() == bookId) {
          String returnDate = book.checkoutCopy();
          if (returnDate == null) {
            return new ResponseEntity<>("No copies available to checkout.", HttpStatus.CONFLICT);
          } else {
            return new ResponseEntity<>(book, HttpStatus.OK);
          }
        }
      }
      return new ResponseEntity<>("No books found with id " + bookId + ".", HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when checking out a book.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
