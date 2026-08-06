package com.airtribe.library.service.reservation;

import com.airtribe.library.model.Book;
import com.airtribe.library.service.LibraryService;
import com.airtribe.library.service.notification.NotificationService;

import java.util.*;
import java.util.logging.Logger;

public class ReservationManager {
    private static final Logger LOGGER = Logger.getLogger(ReservationManager.class.getName());

    private final Map<String, Queue<Reservation>> reservationMap;
    private final Map<String, Queue<ReservationObserver>> observers;
    private final LibraryService libraryService;
    private final NotificationService notificationService;

    public ReservationManager(LibraryService libraryService, NotificationService notificationService) {
        this.libraryService = libraryService;
        this.reservationMap = new HashMap<>();
        this.observers = new HashMap<>();
        this.notificationService = notificationService;
    }

    public void reserveBook(String isbn, String patronId) {
        reservationMap.computeIfAbsent(isbn, key -> new LinkedList<>())
                .add(new Reservation(isbn, patronId));
        observers.computeIfAbsent(isbn, key -> new LinkedList<>())
                .add(new ReservationNotificationService(patronId, notificationService));
        LOGGER.info(String.format("Reservation added for book %s by patron %s", isbn, patronId));
    }

    public Reservation getNextReservation(String isbn) {
        Queue<Reservation> queue = reservationMap.get(isbn);
        return (queue == null || queue.isEmpty()) ? null : queue.peek();
    }

    public void fulfillNextReservation(String isbn) {
        Queue<Reservation> queue = reservationMap.get(isbn);
        if (queue != null) {
            Reservation reservation = queue.poll();
            if (reservation != null) {
                LOGGER.info(String.format("Reservation fulfilled for book %s: patron %s", isbn, reservation.getPatronId()));
            }
        }
    }

    public void notifyIfReserved(Book book) {
        Reservation reservation = getNextReservation(book.getIsbn());
        Queue<ReservationObserver> observerQueue = observers.get(book.getIsbn());
        if (reservation != null && observerQueue != null && !observerQueue.isEmpty()) {
            ReservationObserver observer = observerQueue.poll();
            LOGGER.info(String.format("Reserved book available: %s for patron %s", book.getIsbn(), reservation.getPatronId()));
            observer.onBookAvailable(book, reservation.getPatronId());
            fulfillNextReservation(book.getIsbn());
        }
    }
}
