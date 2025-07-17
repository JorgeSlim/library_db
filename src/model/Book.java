// src/model/Book.java
package model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private int publicationYear;
    private int quantity;
    private int availableQuantity;
    private String createdAt;

    // Constructors
    public Book() {}

    public Book(String title, String author, String isbn, String genre, int publicationYear, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.quantity = quantity;
        this.availableQuantity = quantity; // Initially available equals total quantity
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", genre='" + genre + '\'' +
                ", publicationYear=" + publicationYear +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                '}';
    }

    // Business logic methods
    public boolean isAvailable() {
        return availableQuantity > 0;
    }

    public void incrementAvailableQuantity() {
        if (availableQuantity < quantity) {
            availableQuantity++;
        }
    }

    public void decrementAvailableQuantity() {
        if (availableQuantity > 0) {
            availableQuantity--;
        }
    }
}
