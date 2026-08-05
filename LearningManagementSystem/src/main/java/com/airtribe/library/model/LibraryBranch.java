package com.airtribe.library.model;

import java.util.HashMap;
import java.util.Map;

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
}
