package com.airtribe.library.service.reservation;

import com.airtribe.library.model.Book;
import com.airtribe.library.service.notification.NotificationService;

public class ReservationNotificationService implements ReservationObserver {
    private final String patronId;
    private final NotificationService notificationService;

    public ReservationNotificationService(String patronId, NotificationService notificationService) {
        this.patronId = patronId;
        this.notificationService = notificationService;
    }

    @Override
    public void onBookAvailable(Book book, String patronId) {
        notificationService.notifyPatron(patronId, book, "Your reserved book is now available.");
    }
}
