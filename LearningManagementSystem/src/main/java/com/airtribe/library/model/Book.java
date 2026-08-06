package com.airtribe.library.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Book {
    private final String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private boolean available;
    private final Set<String> genres;

    public Book(String isbn, String title, String author, int publicationYear) {
        this(isbn, title, author, publicationYear, Set.of());
    }

    public Book(String isbn, String title, String author, int publicationYear, Set<String> genres) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.available = true;
        this.genres = genres == null ? new HashSet<>() : new HashSet<>(genres);
    }

    public String getIsbn() {
        return isbn;
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

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Set<String> getGenres() {
        return Collections.unmodifiableSet(genres);
    }

    public void addGenre(String genre) {
        if (genre != null && !genre.isBlank()) {
            genres.add(genre);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publicationYear=" + publicationYear +
                ", available=" + available +
                ", genres=" + genres +
                '}';
    }
}
