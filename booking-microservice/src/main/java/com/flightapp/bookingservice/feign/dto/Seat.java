package com.flightapp.bookingservice.feign.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
	private String seatNumber;
	private String seatType;
	private boolean reserved;
	private double price;
}
