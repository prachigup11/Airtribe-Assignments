package com.airtribe.library.service.notification;

import com.airtribe.library.model.Book;
import java.util.logging.Logger;

public class EmailNotificationService implements NotificationService {
    private static final Logger LOGGER = Logger.getLogger(EmailNotificationService.class.getName());

    @Override
    public void notifyPatron(String patronId, Book book, String message) {
        LOGGER.info(String.format("Notify patron %s about book %s: %s", patronId, book.getIsbn(), message));
    }
}
