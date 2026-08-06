package com.airtribe.library.service.reservation;

import com.airtribe.library.model.Book;

public interface ReservationObserver {
    void onBookAvailable(Book book, String patronId);
}
