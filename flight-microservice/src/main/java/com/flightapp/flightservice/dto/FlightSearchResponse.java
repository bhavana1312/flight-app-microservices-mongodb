package com.flightapp.flightservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchResponse {
	private String flightId;
	private String airlineName;
	private String airlineLogoUrl;
	private LocalDateTime departureDateTime;
	private LocalDateTime arrivalDateTime;
	private String fromPlace;
	private String toPlace;
	private int availableSeats;
	private double lowestPrice;
}
