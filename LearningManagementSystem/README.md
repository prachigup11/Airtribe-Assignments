# Airtribe Library Management System

A Java-based Library Management System that supports book and patron management, lending workflows, inventory tracking, multi-branch support, reservations, and recommendations.

## Features

- Book management: add, remove, update, and search books by title, author, or ISBN.
- Patron management: register patrons, update details, and track borrowing history.
- Lending process: checkout and return books.
- Inventory tracking: list available and borrowed books per branch.
- Multi-branch support: manage books across multiple library branches and transfer books between branches.
- Reservation system: reserve checked-out books and notify patrons when they become available.
- Recommendation engine: suggest books based on patron borrowing history and genre preferences.
- Logging: uses built-in Java logging for audit and error events.

## Project Structure

- `src/main/java/com/airtribe/library/App.java` - application entry point.
- `src/main/java/com/airtribe/library/model` - domain models: `Book`, `Patron`, `LibraryBranch`.
- `src/main/java/com/airtribe/library/service` - service layer and factory.
- `src/main/java/com/airtribe/library/service/notification` - notification handlers.
- `src/main/java/com/airtribe/library/service/recommendation` - recommendation engine.
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

## Example Workflow

After starting the app, the demo creates branches, adds books and patrons, performs checkouts, returns, reservations, branch transfers, and prints recommendations for patrons.

A typical run includes:

- creating library branches
- adding books with genre metadata
- registering patrons and checking out books
- returning a book so a reserved patron is notified
- transferring books between branches
- printing recommended books based on genre preferences and borrowing history

## Notes

- This repository is designed for learning and demonstration.
- Extend it further with a recommendation engine or persistent storage.
