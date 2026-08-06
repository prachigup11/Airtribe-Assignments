package com.airtribe.library.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LibraryBranch {
    private final String branchId;
    private String name;
    private final Map<String, Book> inventory;

    public LibraryBranch(String branchId, String name) {
        this.branchId = branchId;
        this.name = name;
        this.inventory = new HashMap<>();
    }

    public String getBranchId() {
        return branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Book> getInventory() {
        return inventory;
    }

    public void addBook(Book book) {
        inventory.put(book.getIsbn(), book);
    }

    public Book removeBook(String isbn) {
        return inventory.remove(isbn);
    }

    public Book findBook(String isbn) {
        return inventory.get(isbn);
    }

    public boolean hasBook(String isbn) {
        return inventory.containsKey(isbn);
    }

    public List<Book> getAvailableBooks() {
        return inventory.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Book> getBorrowedBooks() {
        return inventory.values().stream()
                .filter(book -> !book.isAvailable())
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryBranch that = (LibraryBranch) o;
        return Objects.equals(branchId, that.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }
}
