package com.airtribe.library.service.reservation;

public class Reservation {
    private final String isbn;
    private final String patronId;

    public Reservation(String isbn, String patronId) {
        this.isbn = isbn;
        this.patronId = patronId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPatronId() {
        return patronId;
    }
}
