package com.flightapp.flightservice.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {
	@NotBlank
	private String fromPlace;

	@NotBlank
	private String toPlace;

	@NotNull
	private LocalDateTime startDateTime;

	@NotNull
	private LocalDateTime endDateTime;

	private Boolean roundTrip;
	private LocalDateTime returnStartDateTime;
	private LocalDateTime returnEndDateTime;

	private Integer seatsRequired;
	private String seatType;
}
