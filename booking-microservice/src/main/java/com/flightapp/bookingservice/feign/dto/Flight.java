package com.flightapp.bookingservice.feign.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

	private String id;
	private String airlineName;
	private String airlineLogoUrl;
	private String fromPlace;
	private String toPlace;
	private LocalDateTime departureDateTime;
	private LocalDateTime arrivalDateTime;
	private List<Seat> seats;
}
