package com.airtribe.parking.model;

import java.util.Objects;

public record Vehicle(String licensePlate, VehicleType type) {
    public Vehicle {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("License plate is required");
        }
        Objects.requireNonNull(type, "Vehicle type is required");
        licensePlate = licensePlate.trim().toUpperCase();
    }
}