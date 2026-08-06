package com.airtribe.library.service.recommendation;

import com.airtribe.library.model.Book;
import com.airtribe.library.model.Patron;

import java.util.List;
import java.util.logging.Logger;

public class RecommendationService {
    private static final Logger LOGGER = Logger.getLogger(RecommendationService.class.getName());

    private RecommendationStrategy strategy;

    public RecommendationService(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Book> recommend(Patron patron, List<Book> availableBooks) {
        LOGGER.info(String.format("Generating recommendations for patron %s", patron.getPatronId()));
        return strategy.recommend(patron, availableBooks);
    }

    public void setStrategy(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }
}
