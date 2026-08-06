package com.airtribe.library.service.recommendation;

import com.airtribe.library.model.Book;
import com.airtribe.library.model.Patron;

import java.util.*;
import java.util.stream.Collectors;

public class GenreBasedRecommendationStrategy implements RecommendationStrategy {
    @Override
    public List<Book> recommend(Patron patron, List<Book> availableBooks) {
        Set<String> interestedGenres = new HashSet<>(patron.getPreferredGenres());
        interestedGenres.addAll(patron.getBorrowedGenres());

        Map<Book, Long> score = new HashMap<>();
        for (Book book : availableBooks) {
            long preferredGenreMatches = book.getGenres().stream()
                    .filter(interestedGenres::contains)
                    .count();
            if (preferredGenreMatches > 0) {
                score.put(book, preferredGenreMatches);
            }
        }

        return score.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
