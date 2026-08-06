package com.airtribe.library.service.notification;

import com.airtribe.library.model.Book;

public interface NotificationService {
    void notifyPatron(String patronId, Book book, String message);
}
