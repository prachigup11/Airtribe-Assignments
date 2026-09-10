package com.airtribe.parking.model;

import java.util.Objects;

public final class ParkingSpot {
    private final String spotId;
    private final int floorNumber;
    private final SpotType type;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, int floorNumber, SpotType type) {
        this.spotId = Objects.requireNonNull(spotId);
        this.floorNumber = floorNumber;
        this.type = Objects.requireNonNull(type);
    }

    public String spotId() { return spotId; }
    public int floorNumber() { return floorNumber; }
    public SpotType type() { return type; }
    public boolean isAvailable() { return parkedVehicle == null; }
    public Vehicle parkedVehicle() { return parkedVehicle; }

    public void park(Vehicle vehicle) {
        if (!isAvailable()) throw new IllegalStateException("Spot is already occupied: " + spotId);
        if (!type.fits(vehicle.type())) throw new IllegalArgumentException("Vehicle does not fit in spot: " + spotId);
        parkedVehicle = vehicle;
    }

    public Vehicle release() {
        if (isAvailable()) throw new IllegalStateException("Spot is already free: " + spotId);
        Vehicle vehicle = parkedVehicle;
        parkedVehicle = null;
        return vehicle;
    }
}