package com.flightapp.flightservice.dto;

import lombok.*;

import java.time.LocalDateTime;

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
