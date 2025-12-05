package com.flightapp.flightservice.dto;

import com.flightapp.flightservice.model.Seat;
import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInventoryRequest {

	@NotBlank(message = "Airline name is required")
	private String airlineName;

	@NotBlank(message = "Airline logo URL is required")
	private String airlineLogoUrl;

	@NotBlank(message = "From place is required")
	private String fromPlace;

	@NotBlank(message = "To place is required")
	private String toPlace;

	@NotNull(message = "Departure time is required")
	private LocalDateTime departureDateTime;

	@NotNull(message = "Arrival time is required")
	private LocalDateTime arrivalDateTime;

	@NotBlank(message = "Aircraft model is required")
	private String aircraftModel;

	@Min(value = 0, message = "Business seat count must be 0 or more")
	private int businessSeats;

	@Min(value = 0, message = "Economy seat count must be 0 or more")
	private int economySeats;

	@PositiveOrZero(message = "Business seat price must be >= 0")
	private double businessSeatPrice;

	@PositiveOrZero(message = "Economy seat price must be >= 0")
	private double economySeatPrice;
}