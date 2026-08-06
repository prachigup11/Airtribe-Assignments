package com.airtribe.library.service;

import com.airtribe.library.model.Book;

import java.util.Set;

public class BookFactory {

    public static Book createBook(String isbn, String title, String author, int publicationYear) {
        return new Book(isbn, title, author, publicationYear);
    }

    public static Book createBook(String isbn, String title, String author, int publicationYear, Set<String> genres) {
        return new Book(isbn, title, author, publicationYear, genres);
    }
}
