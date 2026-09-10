package com.airtribe.parking.model;

public enum SpotType {
    MOTORCYCLE(1),
    COMPACT(2),
    LARGE(3);

    private final int capacity;

    SpotType(int capacity) {
        this.capacity = capacity;
    }

    public boolean fits(VehicleType vehicleType) {
        return capacity >= vehicleType.minimumSpotType().capacity;
    }
}