package com.airtribe.parking.service;

import com.airtribe.parking.model.ParkingSpot;
import com.airtribe.parking.model.ParkingTicket;
import com.airtribe.parking.model.Vehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ParkingLotService {
    private static final Logger LOGGER = LogManager.getLogger(ParkingLotService.class);
    private final List<ParkingSpot> spots;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();
    private final FeeCalculator feeCalculator;
    private final Clock clock;

    public ParkingLotService(List<ParkingSpot> spots, FeeCalculator feeCalculator, Clock clock) {
        if (spots == null || spots.isEmpty()) throw new IllegalArgumentException("At least one parking spot is required");
        this.spots = List.copyOf(spots);
        this.feeCalculator = Objects.requireNonNull(feeCalculator);
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized ParkingTicket checkIn(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle is required");
        LOGGER.info("Check-in requested for vehicle={}, type={}", vehicle.licensePlate(), vehicle.type());
        if (activeTickets.containsKey(vehicle.licensePlate())) {
            LOGGER.warn("Check-in rejected because vehicle is already parked: {}", vehicle.licensePlate());
            throw new IllegalStateException("Vehicle is already parked: " + vehicle.licensePlate());
        }
        ParkingSpot spot = spots.stream()
                .filter(candidate -> candidate.isAvailable() && candidate.type().fits(vehicle.type()))
            .sorted(Comparator.comparingInt(candidate -> candidate.type().ordinal()))
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.warn("Check-in rejected because no suitable spot is available for vehicle={}",
                        vehicle.licensePlate());
                    return new IllegalStateException("No suitable parking spot is available");
                });
        spot.park(vehicle);
        ParkingTicket ticket = new ParkingTicket(UUID.randomUUID().toString(), vehicle, spot, Instant.now(clock));
        activeTickets.put(vehicle.licensePlate(), ticket);
            LOGGER.info("Vehicle checked in: vehicle={}, ticketId={}, slot={}, entryTime={}",
                vehicle.licensePlate(), ticket.ticketId(), spot.spotId(), ticket.entryTime());
        availableSpotsByFloor();
        return ticket;
    }

    public synchronized ParkingReceipt checkOut(String licensePlate) {
            LOGGER.info("Check-out requested for vehicle={}", licensePlate);
        ParkingTicket ticket = activeTickets.remove(normalizePlate(licensePlate));
            if (ticket == null) {
                LOGGER.warn("Check-out rejected because no active ticket exists for vehicle={}", licensePlate);
                throw new IllegalArgumentException("No active ticket for vehicle: " + licensePlate);
            }
        ticket.close(Instant.now(clock));
        ticket.spot().release();
            ParkingReceipt receipt = new ParkingReceipt(ticket, feeCalculator.calculate(ticket));
            LOGGER.info("Vehicle checked out: vehicle={}, ticketId={}, slot={}, entryTime={}, exitTime={}, fee={}",
                ticket.vehicle().licensePlate(), ticket.ticketId(), ticket.spot().spotId(),
                ticket.entryTime(), ticket.exitTime(), receipt.fee());
            availableSpotsByFloor();
            return receipt;
    }

    public synchronized ParkingTicket getActiveTicket(String licensePlate) {
        return activeTickets.get(normalizePlate(licensePlate));
    }

    public synchronized Map<Integer, Long> availableSpotsByFloor() {
        Map<Integer, Long> availability = new HashMap<>();
        spots.stream().filter(ParkingSpot::isAvailable)
                .forEach(spot -> availability.merge(spot.floorNumber(), 1L, Long::sum));
        Map<Integer, Long> result = Map.copyOf(availability);
        LOGGER.info("Available spots by floor: {}", result);
        return result;
    }

    public synchronized List<ParkingSpot> spots() {
        return List.copyOf(new ArrayList<>(spots));
    }

    private String normalizePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) throw new IllegalArgumentException("License plate is required");
        return licensePlate.trim().toUpperCase();
    }
}