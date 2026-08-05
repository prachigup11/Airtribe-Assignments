package com.airtribe.library.service;

import com.airtribe.library.model.Book;
import com.airtribe.library.model.LibraryBranch;
import com.airtribe.library.model.Patron;
import com.airtribe.library.service.reservation.ReservationManager;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LibraryService {
    private static final Logger LOGGER = Logger.getLogger(LibraryService.class.getName());

    private final Map<String, LibraryBranch> branches;
    private final Map<String, Patron> patrons;
    private final ReservationManager reservationManager;

    public LibraryService() {
        this.branches = new HashMap<>();
        this.patrons = new HashMap<>();
        this.reservationManager = new ReservationManager(this);
    }

    public void addBranch(LibraryBranch branch) {
        branches.put(branch.getBranchId(), branch);
        LOGGER.info(String.format("Branch added: %s (%s)", branch.getName(), branch.getBranchId()));
    }

    public LibraryBranch getBranch(String branchId) {
        return branches.get(branchId);
    }

    public void addBook(String branchId, Book book) {
        LibraryBranch branch = branches.get(branchId);
        if (branch == null) {
            LOGGER.severe(String.format("Branch not found: %s", branchId));
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
        branch.addBook(book);
        LOGGER.info(String.format("Book added to branch %s: %s", branch.getName(), book));
    }

    public void removeBook(String branchId, String isbn) {
        LibraryBranch branch = branches.get(branchId);
        if (branch == null) {
            LOGGER.severe(String.format("Branch not found: %s", branchId));
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
        Book removed = branch.removeBook(isbn);
        if (removed == null) {
            LOGGER.warning(String.format("Book not found for removal: %s", isbn));
            return;
        }
        LOGGER.info(String.format("Book removed from branch %s: %s", branch.getName(), isbn));
    }

    public void updateBook(String branchId, String isbn, String title, String author, int publicationYear) {
        Book book = getBook(branchId, isbn);
        if (book == null) {
            LOGGER.severe(String.format("Book not found: %s", isbn));
            throw new IllegalArgumentException("Book not found: " + isbn);
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublicationYear(publicationYear);
        LOGGER.info(String.format("Book updated: %s", isbn));
    }

    public Book getBook(String branchId, String isbn) {
        LibraryBranch branch = branches.get(branchId);
        return branch == null ? null : branch.findBook(isbn);
    }

    public List<Book> searchBooks(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        return branches.values().stream()
                .flatMap(branch -> branch.getInventory().values().stream())
                .filter(book -> book.getTitle().toLowerCase(Locale.ROOT).contains(normalized)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(normalized)
                        || book.getIsbn().toLowerCase(Locale.ROOT).contains(normalized))
                .collect(Collectors.toList());
    }

    public void addPatron(Patron patron) {
        patrons.put(patron.getPatronId(), patron);
        LOGGER.info(String.format("Patron added: %s (%s, %s)", patron.getName(), patron.getPatronId(), patron.getEmail()));
    }

    public void updatePatron(String patronId, String name, String email) {
        Patron patron = patrons.get(patronId);
        if (patron == null) {
            LOGGER.severe(String.format("Patron not found: %s", patronId));
            throw new IllegalArgumentException("Patron not found: " + patronId);
        }
        patron.setName(name);
        patron.setEmail(email);
        LOGGER.info(String.format("Patron updated: %s", patronId));
    }

    public Patron getPatron(String patronId) {
        return patrons.get(patronId);
    }

    public void checkoutBook(String branchId, String isbn, String patronId) {
        Book book = getBook(branchId, isbn);
        Patron patron = patrons.get(patronId);
        LibraryBranch branch = getBranch(branchId);

        if (book == null) {
            LOGGER.severe(String.format("Book not found for checkout: %s in branch %s", isbn, branchId));
            throw new IllegalArgumentException("Book not found: " + isbn);
        }
        if (patron == null) {
            LOGGER.severe(String.format("Patron not found for checkout: %s", patronId));
            throw new IllegalArgumentException("Patron not found: " + patronId);
        }
        if (!book.isAvailable()) {
            LOGGER.warning(String.format("Book already borrowed: %s in branch %s", isbn, branchId));
            throw new IllegalStateException("Book is already borrowed");
        }

        book.setAvailable(false);
        patron.addBorrowingHistory(isbn);
        LOGGER.info(String.format("Book checked out: %s by %s from branch %s", book, patron.getName(), branch == null ? branchId : branch.getName()));
    }

    public void returnBook(String branchId, String isbn, String patronId) {
        Book book = getBook(branchId, isbn);
        Patron patron = patrons.get(patronId);
        LibraryBranch branch = getBranch(branchId);

        if (book == null) {
            LOGGER.severe(String.format("Book not found for return: %s in branch %s", isbn, branchId));
            throw new IllegalArgumentException("Book not found: " + isbn);
        }
        if (patron == null) {
            LOGGER.severe(String.format("Patron not found for return: %s", patronId));
            throw new IllegalArgumentException("Patron not found: " + patronId);
        }

        book.setAvailable(true);
        LOGGER.info(String.format("Book returned: %s by %s to branch %s", book, patron.getName(), branch == null ? branchId : branch.getName()));
        reservationManager.notifyIfReserved(book);
    }

    public List<Book> listAvailableBooks(String branchId) {
        LibraryBranch branch = branches.get(branchId);
        if (branch == null) {
            LOGGER.warning(String.format("Requested available books for unknown branch: %s", branchId));
            return Collections.emptyList();
        }
        List<Book> availableBooks = branch.getInventory().values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
        LOGGER.info(String.format("Available books in branch %s: %s", branch.getName(), availableBooks));
        return availableBooks;
    }

    public List<Book> listBorrowedBooks(String branchId) {
        LibraryBranch branch = branches.get(branchId);
        if (branch == null) {
            LOGGER.warning(String.format("Requested borrowed books for unknown branch: %s", branchId));
            return Collections.emptyList();
        }
        List<Book> borrowedBooks = branch.getInventory().values().stream()
                .filter(book -> !book.isAvailable())
                .collect(Collectors.toList());
        LOGGER.info(String.format("Borrowed books in branch %s: %s", branch.getName(), borrowedBooks));
        return borrowedBooks;
    }

    public Set<String> getAllPatronIds() {
        return Collections.unmodifiableSet(patrons.keySet());
    }

    public ReservationManager getReservationManager() {
        return reservationManager;
    }
}
