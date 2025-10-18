package dev.coms4156.project.individualproject.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the Book entry model.
 */
public class Book implements Comparable<Book> {
  private String title;
  private List<String> authors;
  private String language;
  private String shelvingLocation;
  private String publicationDate;
  private String publisher;
  private List<String> subjects;
  private int id;
  private int amountOfTimesCheckedOut;
  private int copiesAvailable;
  private List<String> returnDates;
  private int totalCopies;

  /**
   * Very basic Book constructor.
   *
   * @param title The title of the book.
   * @param id The id of the book.
   */
  public Book(String title, int id) {
    this.title = title;
    this.id = id;
    this.authors = new ArrayList<>();
    this.language = "";
    this.shelvingLocation = "";
    this.publicationDate = "";
    this.publisher = "";
    this.subjects = new ArrayList<>();
    this.amountOfTimesCheckedOut = 0;
    this.copiesAvailable = 1;
    this.returnDates = new ArrayList<>();
    this.totalCopies = 1;
  }

  /**
   * Complete Book constructor.
   *
   * @param title title of the book.
   * @param authors list of author(s).
   * @param language language of the book.
   * @param shelvingLocation shelving location of the book.
   * @param publicationDate publication date of the book.
   * @param publisher publisher of the book.
   * @param subjects list of subject(s) of the book.
   * @param id unique id of the book.
   * @param copiesAvailable number of copies available of the book.
   * @param totalCopies number of available and checked-out copies of the book.
   */
  public Book(String title, List<String> authors, String language, String shelvingLocation,
              String publicationDate, String publisher, List<String> subjects,
              int id, int copiesAvailable, int totalCopies) {
    this.title = title;
    this.authors = authors;
    this.language = language;
    this.shelvingLocation = shelvingLocation;
    this.publicationDate = publicationDate;
    this.publisher = publisher;
    this.subjects = subjects;
    this.id = id;
    this.amountOfTimesCheckedOut = 0;
    this.copiesAvailable = copiesAvailable;
    this.returnDates = new ArrayList<>();
    this.totalCopies = totalCopies;
  }

  /**
   * No args constructor for Jackson.
   */
  public Book() {
    this.authors = new ArrayList<>();
    this.subjects = new ArrayList<>();
    this.returnDates = new ArrayList<>();
    this.language = "";
    this.shelvingLocation = "";
    this.publicationDate = "";
    this.publisher = "";
    this.title = "";
    this.amountOfTimesCheckedOut = 0;
    this.copiesAvailable = 1;
    this.totalCopies = 1;
    this.id = 0;
  }

  public boolean hasCopies() {
    return copiesAvailable >= 1;
  }

  public boolean hasMultipleAuthors() {
    return authors.size() > 1;
  }

  /**
   * Deletes a single copy of the book if at least one copy exists and is available.
   *
   * @return {@code true} if a copy was successfully deleted; {@code false} if no copies
   *         are available or exist to delete.
   */

  public boolean deleteCopy() {
    if (totalCopies > 0 && copiesAvailable > 0) {
      totalCopies--;
      copiesAvailable--;
      return true;
    }
    return false;
  }

  /**
   * Adds a single copy of the book and increases the total copies count.
   *
   */
  
  public void addCopy() {
    totalCopies++;
    copiesAvailable++;
  }

  /**
   * Checks out a copy of the book if available and generates a due date two weeks from today.
   *
   * @return A {@code String} representing the due date if the checkout is successful;
   *         otherwise, {@code null} if no copies are available.
   */

  public String checkoutCopy() {
    if (copiesAvailable > 0) {
      copiesAvailable--;
      amountOfTimesCheckedOut++;
      LocalDate today = LocalDate.now();
      LocalDate dueDate = today.plusWeeks(2);
      String dueDateStr = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
      returnDates.add(dueDateStr);
      return dueDateStr;
    }

    return null;
  }

  /**
   * Returns a previously checked-out copy of the book corresponding to the given due date.
   *
   * @param date A {@code String} representing the due date of the book being returned.
   * @return {@code true} if the return was successful and a matching date was removed;
   *         {@code false} if no matching due date is found.
   */
  public boolean returnCopy(String date) {
    if (!returnDates.isEmpty()) {
      for (int i = 0; i < returnDates.size(); i++) {
        if (returnDates.get(i).equals(date)) {
          returnDates.remove(i);
          copiesAvailable++;
          return true;
        }
      }
    }

    return false;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = null == authors ? new ArrayList<>() : authors;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getShelvingLocation() {
    return shelvingLocation;
  }

  public void setShelvingLocation(String shelvingLocation) {
    this.shelvingLocation = shelvingLocation;
  }

  public String getPublicationDate() {
    return publicationDate;
  }

  public void setPublicationDate(String publicationDate) {
    this.publicationDate = publicationDate;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public List<String> getSubjects() {
    return subjects;
  }

  public void setSubjects(List<String> subjects) {
    this.subjects = subjects != null ? subjects : new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getAmountOfTimesCheckedOut() {
    return amountOfTimesCheckedOut;
  }

  public int getCopiesAvailable() {
    return copiesAvailable;
  }

  public void setCopiesAvailable(int copiesAvailable) {
    this.copiesAvailable = copiesAvailable;
  }

  public List<String> getReturnDates() {
    return returnDates;
  }

  public void setReturnDates(List<String> returnDates) {
    this.returnDates = returnDates != null ? returnDates : new ArrayList<>();
  }

  public int getTotalCopies() {
    return totalCopies;
  }

  public void setTotalCopies(int totalCopies) {
    this.totalCopies = totalCopies;
  }

  @Override
  public int compareTo(Book other) {
    return Integer.compare(this.id, other.id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Book cmpBook = (Book) obj;
    return cmpBook.id == this.id;
  }

  @Override
  public String toString() {
    return String.format("(%d)\t%s", this.id, this.title);
  }
}

