package com.airtribe.library;

import com.airtribe.library.model.Book;
import com.airtribe.library.model.LibraryBranch;
import com.airtribe.library.model.Patron;
import com.airtribe.library.service.BookFactory;
import com.airtribe.library.service.LibraryService;
import com.airtribe.library.util.ConsoleLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        configureLogging();
        LOGGER.info("Starting Library Management System");

        LibraryService libraryService = new LibraryService();

        LibraryBranch mainBranch = new LibraryBranch("branch-1", "Main Branch");
        LibraryBranch eastBranch = new LibraryBranch("branch-2", "East Branch");
        libraryService.addBranch(mainBranch);
        libraryService.addBranch(eastBranch);

        Book book1 = BookFactory.createBook("9780134685991", "Effective Java", "Joshua Bloch", 2018, Set.of("Programming", "Java"));
        Book book2 = BookFactory.createBook("9780596009205", "Head First Java", "Kathy Sierra", 2005, Set.of("Java", "Learning"));
        Book book3 = BookFactory.createBook("9781491950357", "Designing Data-Intensive Applications", "Martin Kleppmann", 2017, Set.of("Data", "Architecture"));

        libraryService.addBook(mainBranch.getBranchId(), book1);
        libraryService.addBook(mainBranch.getBranchId(), book2);
        libraryService.addBook(eastBranch.getBranchId(), book3);

        Patron alice = new Patron("patron-1", "Alice Walker", "alice@example.com");
        alice.addPreferredGenre("Java");
        alice.addPreferredGenre("Programming");
        libraryService.addPatron(alice);

        Patron bob = new Patron("patron-2", "Bob Martin", "bob@example.com");
        bob.addPreferredGenre("Data");
        libraryService.addPatron(bob);

        libraryService.checkoutBook(mainBranch.getBranchId(), book1.getIsbn(), alice.getPatronId());
        alice.addBorrowedGenre("Java");
        ConsoleLogger.log("Alice borrowing history: " + alice.getBorrowingHistory());

        libraryService.reserveBook(mainBranch.getBranchId(), book1.getIsbn(), bob.getPatronId());
        ConsoleLogger.log("Bob reserved book: " + book1.getTitle());

        libraryService.checkoutBook(mainBranch.getBranchId(), book2.getIsbn(), alice.getPatronId());
        alice.addBorrowedGenre("Java");

        libraryService.returnBook(mainBranch.getBranchId(), book1.getIsbn(), alice.getPatronId());

        libraryService.transferBook(eastBranch.getBranchId(), mainBranch.getBranchId(), book3.getIsbn());
        ConsoleLogger.log("Transferred book to Main Branch: " + book3.getTitle());

        List<Book> recommendationsForBob = libraryService.recommendBooks(mainBranch.getBranchId(), bob.getPatronId());
        ConsoleLogger.log("Recommendations for Bob: " + recommendationsForBob);

        ConsoleLogger.log("Available books in Main Branch: " + libraryService.listAvailableBooks(mainBranch.getBranchId()));
        ConsoleLogger.log("Borrowed books in Main Branch: " + libraryService.listBorrowedBooks(mainBranch.getBranchId()));
        ConsoleLogger.log("Available books in East Branch: " + libraryService.listAvailableBooks(eastBranch.getBranchId()));

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
