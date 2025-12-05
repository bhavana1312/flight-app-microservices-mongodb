package com.flightapp.flightservice.model;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
	@NotBlank
	private String seatNumber;
	private SeatType seatType;
	private boolean reserved;
	private double price;
}
