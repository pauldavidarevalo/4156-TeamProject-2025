package dev.coms4156.project.logprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.logprocessor.model.Book;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the Book class.
 */
@SpringBootTest
public class BookUnitTests {

  public static Book book;

  @BeforeEach
  public void setUpBookForTesting() {
    book = new Book("When Breath Becomes Air", 0);
  }

  @Test
  public void equalsBothAreTheSameAddressTest() {
    Book cmpBook = book;
    assertEquals(cmpBook, book);
  }

  @Test
  public void equalsBothSameIdTest() {
    Book cmpBook = new Book("When Breath Becomes Air", 0);
    assertEquals(cmpBook, book);
  }

  @Test
  public void equalsDifferentIdTest() {
    Book cmpBook = new Book("When Breath Becomes Air", 1);
    assertNotEquals(cmpBook, book);
  }

  @Test
  public void equalsNullTest() {
    assertNotEquals(book, null);
  }

  @Test
  public void equalsWrongClassTest() {
    String wrongClass = "No books here!";
    assertNotEquals(book, wrongClass);
  }

  @Test
  public void compareToSameAddressTest() {
    Book cmpBook = book;
    assertEquals(0, book.compareTo(cmpBook));
  }

  @Test
  public void compareToSameIdTest() {
    Book cmpBook = new Book("When Breath Becomes Air", 0);
    assertEquals(0, book.compareTo(cmpBook));
  }

  @Test
  public void compareToGreaterThanTest() {
    Book cmpBook = new Book("When Breath Becomes Air", -1);
    assertTrue(0 < book.compareTo(cmpBook));
  }

  @Test
  public void compareToLessThanTest() {
    Book cmpBook = new Book("When Breath Becomes Air", 1);
    assertTrue(0 > book.compareTo(cmpBook));
  }

  @Test
  public void toStringBasicTest() {
    String expectedResult = "(0)\tWhen Breath Becomes Air";
    assertEquals(expectedResult, book.toString());
  }

  @Test
  public void toStringNoTitleTest() {
    Book book = new Book();
    String expectedResult = "(0)\t";
    assertEquals(expectedResult, book.toString());
  }

  @Test
  public void hasCopyTest() {
    assertTrue(book.hasCopies());
  }

  @Test
  public void hasCopyEmptyTest() {
    book.setCopiesAvailable(0);
    assertFalse(book.hasCopies());
  }

  @Test
  public void hasCopyNegativeTest() {
    book.setCopiesAvailable(-1);
    assertFalse(book.hasCopies());
  }   

  @Test
  public void hasMultipleAuthorsSingleAuthorTest() {
    book.setAuthors(new java.util.ArrayList<>(java.util.Arrays.asList("Author 1")));
    assertFalse(book.hasMultipleAuthors());
  }

  @Test
  public void hasMultipleAuthorsMultipleAuthorsTest() {
    book.setAuthors(new java.util.ArrayList<>(java.util.Arrays.asList("Author 1", "Author 2")));
    assertTrue(book.hasMultipleAuthors());
  }

  @Test
  public void hasMultipleAuthorsEmptyTest() {
    book.setAuthors(new java.util.ArrayList<>());
    assertFalse(book.hasMultipleAuthors());
  }

  @Test
  public void deleteCopyNoneTest() {
    book.setTotalCopies(0);
    book.setCopiesAvailable(0);
    assertFalse(book.deleteCopy());
    assertEquals(0, book.getTotalCopies());
    assertEquals(0, book.getCopiesAvailable());
  }

  @Test
  public void deleteCopyNegativeTest() {
    book.setTotalCopies(-1);
    book.setCopiesAvailable(-1);
    assertFalse(book.deleteCopy());
    assertEquals(-1, book.getTotalCopies());
    assertEquals(-1, book.getCopiesAvailable());
  }

  @Test
  public void deleteCopySingleTest() {
    book.setTotalCopies(1);
    book.setCopiesAvailable(1);
    assertTrue(book.deleteCopy());
    assertEquals(0, book.getTotalCopies());
    assertEquals(0, book.getCopiesAvailable());
  }

  @Test
  public void deleteCopyMultipleTest() {
    book.setTotalCopies(2);
    book.setCopiesAvailable(2);
    assertTrue(book.deleteCopy());
    assertEquals(1, book.getTotalCopies());
    assertEquals(1, book.getCopiesAvailable());
    assertTrue(book.deleteCopy());
    assertEquals(0, book.getTotalCopies());
    assertEquals(0, book.getCopiesAvailable());
    assertFalse(book.deleteCopy());
    assertEquals(0, book.getTotalCopies());
    assertEquals(0, book.getCopiesAvailable());
  }

  @Test
  public void deleteCopyNoAvailableTest() {
    book.setTotalCopies(1);
    book.setCopiesAvailable(0);
    assertFalse(book.deleteCopy());
    assertEquals(1, book.getTotalCopies());
    assertEquals(0, book.getCopiesAvailable());
  }

  @Test
  public void deleteCopyNoCopiesTest() {
    book.setTotalCopies(0);
    book.setCopiesAvailable(1);
    assertFalse(book.deleteCopy());
    assertEquals(0, book.getTotalCopies());
    assertEquals(1, book.getCopiesAvailable());
  }

  @Test
  public void addCopyNoCopiesTest() {
    book.setTotalCopies(0);
    book.setCopiesAvailable(0);
    book.addCopy();
    assertEquals(1, book.getTotalCopies());
    assertEquals(1, book.getCopiesAvailable());
  }

  @Test
  public void addCopySingleTest() {
    book.addCopy();
    assertEquals(2, book.getTotalCopies());
    assertEquals(2, book.getCopiesAvailable());
  }

  @Test
  public void checkoutCopyNullTest() {
    book.setCopiesAvailable(0);
    assertNull(book.checkoutCopy());
  }

  @Test
  public void checkoutCopyDefaultTest() {
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.plusWeeks(2);
    String expectedResult = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    assertEquals(expectedResult, book.checkoutCopy());
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(1, book.getAmountOfTimesCheckedOut());
    assertEquals(expectedResult, book.getReturnDates().get(0));
  }

  @Test
  public void checkoutCopyMultipleTest() {
    book.setCopiesAvailable(2);
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.plusWeeks(2);
    String expectedResult = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    assertEquals(expectedResult, book.checkoutCopy());
    assertEquals(1, book.getCopiesAvailable());
    assertEquals(1, book.getAmountOfTimesCheckedOut());
    assertEquals(expectedResult, book.getReturnDates().get(0));

    assertEquals(expectedResult, book.checkoutCopy());
    assertEquals(0, book.getCopiesAvailable());
    assertEquals(2, book.getAmountOfTimesCheckedOut());
    assertEquals(expectedResult, book.getReturnDates().get(1));
  }

  @Test
  public void returnCopyNoCopyTest() {
    LocalDate today = LocalDate.now();
    String todayString = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
    assertFalse(book.returnCopy(todayString));
  }

  @Test
  public void returnCopySingleCopyTest() {
    book.checkoutCopy();
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.plusWeeks(2);
    String dueDateString = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    assertTrue(book.returnCopy(dueDateString));
    assertTrue(book.getReturnDates().isEmpty());
    assertEquals(1, book.getCopiesAvailable());
  }

  @Test
  public void returnCopyWrongDateTest() {
    book.checkoutCopy();
    LocalDate today = LocalDate.now();
    String wrongDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
    assertFalse(book.returnCopy(wrongDate));
    assertFalse(book.getReturnDates().isEmpty());
    assertEquals(0, book.getCopiesAvailable());
  }

  @Test
  public void compareToSameTest() {
    Book cmpBook = book;
    assertEquals(0, book.compareTo(cmpBook));
  }

  @Test
  public void setAuthorsEmptyTest() {
    assertTrue(book.getAuthors().isEmpty());
  }

  @Test
  public void setAuthorsNullTest() {
    book.setAuthors(null);
    assertTrue(book.getAuthors().isEmpty());
    assertFalse(book.getAuthors() == null);
  }

  @Test
  public void setAuthorsMultipleTest() {
    List<String> authors = new ArrayList<>(Arrays.asList("Author 1", "Author 2", "Author 3"));
    book.setAuthors(authors);
    assertEquals(authors, book.getAuthors());
  }

  @Test
  public void setSubjectsEmptyTest() {
    assertTrue(book.getSubjects().isEmpty());
  }

  @Test
  public void setSubjectsNullTest() {
    book.setSubjects(null);
    assertTrue(book.getSubjects().isEmpty());
    assertFalse(book.getSubjects() == null);
  }

  @Test
  public void setSubjectsMultipleTest() {
    List<String> subjects = new ArrayList<>(Arrays.asList("Subject 1", "Subject 2", "Subject 3"));
    book.setSubjects(subjects);
    assertEquals(subjects, book.getSubjects());
  }

  @Test
  public void setReturnDatesEmptyTest() {
    assertTrue(book.getReturnDates().isEmpty());
  }

  @Test
  public void setReturnDatesNullTest() {
    book.setReturnDates(null);
    assertTrue(book.getReturnDates().isEmpty());
    assertFalse(book.getReturnDates() == null);
  }

  @Test
  public void setReturnDatesMultipleTest() {
    List<String> returnDates = 
        new ArrayList<>(Arrays.asList("Return Date 1", "Return Date 2", "Return Date 3"));
    book.setReturnDates(returnDates);
    assertEquals(returnDates, book.getReturnDates());
  }

  @Test
  public void fullConstructorTest() {
    List<String> authors = new ArrayList<>(Arrays.asList("Author 1", "Author 2"));
    List<String> subjects = new ArrayList<>(Arrays.asList("Subject 1", "Subject 2"));
    Book fullBook = new Book("Full Book", authors, "English", "The Shelf", "2025", "Publisher Shmublisher",
        subjects, 51, 1, 3);
    assertEquals("Full Book", fullBook.getTitle());
    assertEquals(51, fullBook.getId());
    assertEquals(authors, fullBook.getAuthors());
    assertEquals("English", fullBook.getLanguage());
    assertEquals("The Shelf", fullBook.getShelvingLocation());
    assertEquals("2025", fullBook.getPublicationDate());
    assertEquals("Publisher Shmublisher", fullBook.getPublisher());
    assertEquals(subjects, fullBook.getSubjects());
    assertEquals(3, fullBook.getTotalCopies());
    assertEquals(1, fullBook.getCopiesAvailable());
  }
}
