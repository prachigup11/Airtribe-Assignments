package com.airtribe.library.service.recommendation;

import com.airtribe.library.model.Book;
import com.airtribe.library.model.Patron;

import java.util.List;

public interface RecommendationStrategy {
    List<Book> recommend(Patron patron, List<Book> availableBooks);
}
