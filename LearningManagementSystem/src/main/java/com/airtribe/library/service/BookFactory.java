package com.airtribe.library.service;

import com.airtribe.library.model.Book;

public class BookFactory {

    public static Book createBook(String isbn, String title, String author, int publicationYear) {
        return new Book(isbn, title, author, publicationYear);
    }
}
