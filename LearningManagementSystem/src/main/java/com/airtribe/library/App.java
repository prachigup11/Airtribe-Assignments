package com.airtribe.library;

import com.airtribe.library.model.Book;
import com.airtribe.library.model.LibraryBranch;
import com.airtribe.library.model.Patron;
import com.airtribe.library.service.BookFactory;
import com.airtribe.library.service.LibraryService;
import com.airtribe.library.service.reservation.ReservationManager;
import com.airtribe.library.util.ConsoleLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        configureLogging();
        LOGGER.info("Starting Library Management System");

        LibraryService libraryService = new LibraryService();

        LibraryBranch mainBranch = new LibraryBranch("branch-1", "Main Branch");
        libraryService.addBranch(mainBranch);

        Book book1 = BookFactory.createBook("9780134685991", "Effective Java", "Joshua Bloch", 2018);
        Book book2 = BookFactory.createBook("9780596009205", "Head First Java", "Kathy Sierra", 2005);
        libraryService.addBook(mainBranch.getBranchId(), book1);
        libraryService.addBook(mainBranch.getBranchId(), book2);

        Patron patron = new Patron("patron-1", "Alice Walker", "alice@example.com");
        libraryService.addPatron(patron);

        libraryService.checkoutBook(mainBranch.getBranchId(), book1.getIsbn(), patron.getPatronId());
        ConsoleLogger.log("Borrowing history: " + patron.getBorrowingHistory());

        libraryService.returnBook(mainBranch.getBranchId(), book1.getIsbn(), patron.getPatronId());

        List<Book> searchResults = libraryService.searchBooks("Java");
        ConsoleLogger.log("Search results: " + searchResults);

        ReservationManager reservationManager = libraryService.getReservationManager();
        reservationManager.reserveBook(book1.getIsbn(), patron.getPatronId());
        libraryService.checkoutBook(mainBranch.getBranchId(), book2.getIsbn(), patron.getPatronId());

        ConsoleLogger.log("Available books: " + libraryService.listAvailableBooks(mainBranch.getBranchId()));
        ConsoleLogger.log("Borrowed books: " + libraryService.listBorrowedBooks(mainBranch.getBranchId()));

        LOGGER.info("Library Management System finished successfully");
    }

    private static void configureLogging() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            try (InputStream configStream = App.class.getResourceAsStream("/logging.properties")) {
                if (configStream != null) {
                    LogManager.getLogManager().readConfiguration(configStream);
                } else {
                    System.err.println("Warning: logging.properties resource not found.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logging configuration", e);
        }
    }
}
