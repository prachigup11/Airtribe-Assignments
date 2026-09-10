package com.airtribe.parking.model;

import java.time.Instant;
import java.util.Objects;

public final class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final Instant entryTime;
    private Instant exitTime;
    private boolean closed;

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot spot, Instant entryTime) {
        this.ticketId = Objects.requireNonNull(ticketId);
        this.vehicle = Objects.requireNonNull(vehicle);
        this.spot = Objects.requireNonNull(spot);
        this.entryTime = Objects.requireNonNull(entryTime);
    }

    public String ticketId() { return ticketId; }
    public Vehicle vehicle() { return vehicle; }
    public ParkingSpot spot() { return spot; }
    public Instant entryTime() { return entryTime; }
    public Instant exitTime() { return exitTime; }
    public boolean isClosed() { return closed; }

    public void close(Instant exitTime) {
        if (closed) throw new IllegalStateException("Ticket is already closed: " + ticketId);
        if (exitTime.isBefore(entryTime)) throw new IllegalArgumentException("Exit time cannot precede entry time");
        this.exitTime = exitTime;
        this.closed = true;
    }
}