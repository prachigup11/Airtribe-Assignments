package com.airtribe.parking.model;

public enum VehicleType {
    MOTORCYCLE(SpotType.MOTORCYCLE),
    CAR(SpotType.COMPACT),
    BUS(SpotType.LARGE);

    private final SpotType minimumSpotType;

    VehicleType(SpotType minimumSpotType) {
        this.minimumSpotType = minimumSpotType;
    }

    public SpotType minimumSpotType() {
        return minimumSpotType;
    }
}