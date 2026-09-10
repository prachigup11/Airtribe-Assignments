package com.airtribe.parking.service;

import com.airtribe.parking.model.ParkingTicket;

import java.math.BigDecimal;

public record ParkingReceipt(ParkingTicket ticket, BigDecimal fee) { }