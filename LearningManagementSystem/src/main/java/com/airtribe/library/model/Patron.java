package com.airtribe.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Patron {
    private final String patronId;
    private String name;
    private String email;
    private final List<String> borrowingHistory;
    private final Set<String> borrowedGenres;
    private final Set<String> preferredGenres;

    public Patron(String patronId, String name, String email) {
        this.patronId = patronId;
        this.name = name;
        this.email = email;
        this.borrowingHistory = new ArrayList<>();
        this.borrowedGenres = new HashSet<>();
        this.preferredGenres = new HashSet<>();
    }

    public String getPatronId() {
        return patronId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getBorrowingHistory() {
        return new ArrayList<>(borrowingHistory);
    }

    public void addBorrowingHistory(String isbn) {
        borrowingHistory.add(isbn);
    }

    public Set<String> getBorrowedGenres() {
        return Collections.unmodifiableSet(borrowedGenres);
    }

    public void addBorrowedGenre(String genre) {
        if (genre != null && !genre.isBlank()) {
            borrowedGenres.add(genre);
        }
    }

    public Set<String> getPreferredGenres() {
        return Collections.unmodifiableSet(preferredGenres);
    }

    public void addPreferredGenre(String genre) {
        if (genre != null && !genre.isBlank()) {
            preferredGenres.add(genre);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patron patron = (Patron) o;
        return Objects.equals(patronId, patron.patronId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patronId);
    }

    @Override
    public String toString() {
        return "Patron{" +
                "patronId='" + patronId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", borrowingHistory=" + borrowingHistory +
                '}';
    }
}
