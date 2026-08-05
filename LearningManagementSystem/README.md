# Airtribe Library Management System

A Java-based Library Management System that supports book and patron management, lending workflows, inventory tracking, multi-branch support, and reservations.

## Features

- Book management: add, remove, update, and search books by title, author, or ISBN.
- Patron management: register patrons, update details, and track borrowing history.
- Lending process: checkout and return books.
- Inventory tracking: list available and borrowed books per branch.
- Logging: uses built-in Java logging for audit and error events.

## Project Structure

- `src/main/java/com/airtribe/library/App.java` - application entry point.
- `src/main/java/com/airtribe/library/model` - domain models: `Book`, `Patron`, `LibraryBranch`.
- `src/main/java/com/airtribe/library/service` - service layer and factory.
- `src/main/java/com/airtribe/library/service/reservation` - reservation feature.
- `src/main/java/com/airtribe/library/util` - utility helpers.

## Class Diagram

```text
App
  |
  +-- LibraryService
         |-- LibraryBranch
         |-- Patron
         |-- ReservationManager
                |-- Reservation
         |-- Book
```

## Design Patterns

- Factory: `BookFactory` centralizes `Book` creation.
- Observer-style notification: `ReservationManager` notifies when reserved books become available.

## Build & Run

### Using Java compiler

```bash
javac -d out src/main/java/com/airtribe/library/**/*.java
java -cp out com.airtribe.library.App
```

### Using Maven

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.airtribe.library.App"
```

Logs are written to `logs/app.log` in addition to console output.

## Notes

- This repository is designed for learning and demonstration.
- Extend it further with a recommendation engine or persistent storage.
