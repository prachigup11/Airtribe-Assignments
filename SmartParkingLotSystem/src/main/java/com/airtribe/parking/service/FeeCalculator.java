package com.airtribe.parking.service;

import com.airtribe.parking.model.ParkingTicket;
import com.airtribe.parking.model.VehicleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

public final class FeeCalculator {
    private static final Logger LOGGER = LogManager.getLogger(FeeCalculator.class);
    private static final BigDecimal MOTORCYCLE_RATE = new BigDecimal("10.00");
    private static final BigDecimal CAR_RATE = new BigDecimal("20.00");
    private static final BigDecimal BUS_RATE = new BigDecimal("40.00");

    public BigDecimal calculate(ParkingTicket ticket) {
        if (!ticket.isClosed()) throw new IllegalArgumentException("Cannot calculate fee for an active ticket");
        long minutes = Duration.between(ticket.entryTime(), ticket.exitTime()).toMinutes();
        long billableHours = Math.max(1, (minutes + 59) / 60);
        BigDecimal fee = rateFor(ticket.vehicle().type())
            .multiply(BigDecimal.valueOf(billableHours)).setScale(2, RoundingMode.HALF_UP);
        LOGGER.info("Fee calculated: ticketId={}, vehicle={}, billableHours={}, fee={}",
            ticket.ticketId(), ticket.vehicle().licensePlate(), billableHours, fee);
        return fee;
    }

    private BigDecimal rateFor(VehicleType type) {
        return switch (type) {
            case MOTORCYCLE -> MOTORCYCLE_RATE;
            case CAR -> CAR_RATE;
            case BUS -> BUS_RATE;
        };
    }
}