package com.airtribe.parking;

import com.airtribe.parking.model.ParkingSpot;
import com.airtribe.parking.model.SpotType;
import com.airtribe.parking.model.Vehicle;
import com.airtribe.parking.model.VehicleType;
import com.airtribe.parking.service.FeeCalculator;
import com.airtribe.parking.service.ParkingLotService;
import com.airtribe.parking.service.ParkingReceipt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public final class App {
    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

    public static void main(String[] args) throws InterruptedException {
        ParkingLotService parkingLot = new ParkingLotService(List.of(
                new ParkingSpot("F1-M1", 1, SpotType.MOTORCYCLE),
                new ParkingSpot("F1-C1", 1, SpotType.COMPACT),
                new ParkingSpot("F1-L1", 1, SpotType.LARGE),
                new ParkingSpot("F2-C1", 2, SpotType.COMPACT)
        ), new FeeCalculator(), Clock.systemUTC());

        parkAndExit(parkingLot, new Vehicle("KA-01-AB-1234", VehicleType.CAR));
        parkAndExit(parkingLot, new Vehicle("KA-05-MC-5678", VehicleType.MOTORCYCLE));
        parkAndExit(parkingLot, new Vehicle("KA-09-BU-9012", VehicleType.BUS));
        parkAndExit(parkingLot, new Vehicle("KA-02-CD-3456", VehicleType.CAR));
        LOGGER.info("Available spots by floor: {}", parkingLot.availableSpotsByFloor());

        runConcurrencySmokeTest();
    }

    private static void parkAndExit(ParkingLotService parkingLot, Vehicle vehicle) {
        var ticket = parkingLot.checkIn(vehicle);
        LOGGER.info("ENTRY vehicle={}, slot={}, entryTime={}", vehicle.licensePlate(),
            ticket.spot().spotId(), format(ticket.entryTime()));

        ParkingReceipt receipt = parkingLot.checkOut(vehicle.licensePlate());
        var closedTicket = receipt.ticket();
        LOGGER.info("EXIT vehicle={}, slot={}, entryTime={}, exitTime={}, fee={}",
            vehicle.licensePlate(), closedTicket.spot().spotId(),
            format(closedTicket.entryTime()), format(closedTicket.exitTime()), receipt.fee());
    }

    private static String format(java.time.Instant timestamp) {
        return TIMESTAMP_FORMAT.format(timestamp);
    }

    private static void runConcurrencySmokeTest() throws InterruptedException {
        ParkingLotService parkingLot = new ParkingLotService(
                List.of(new ParkingSpot("F1-C1", 1, SpotType.COMPACT)),
                new FeeCalculator(), Clock.systemUTC());
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successfulEntries = new AtomicInteger();
        Thread first = entryAttempt(parkingLot, "AA-01", start, successfulEntries);
        Thread second = entryAttempt(parkingLot, "AA-02", start, successfulEntries);
        first.start();
        second.start();
        start.countDown();
        first.join();
        second.join();
        if (successfulEntries.get() != 1) {
            LOGGER.error("Concurrency check failed: successfulEntries={}", successfulEntries.get());
            throw new IllegalStateException("Concurrency check failed");
        }
        LOGGER.info("Concurrency check passed: one vehicle received the only spot");
    }

    private static Thread entryAttempt(ParkingLotService parkingLot, String plate,
                                       CountDownLatch start, AtomicInteger successfulEntries) {
        return new Thread(() -> {
            try {
                start.await();
                parkingLot.checkIn(new Vehicle(plate, VehicleType.CAR));
                successfulEntries.incrementAndGet();
            } catch (Exception exception) {
                LOGGER.warn("Entry rejected for vehicle={}: {}", plate, exception.getMessage());
            }
        });
    }
}