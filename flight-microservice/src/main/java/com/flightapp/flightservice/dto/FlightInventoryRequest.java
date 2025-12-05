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
	@NotBlank
	private String airlineName;
	private String airlineLogoUrl;

	@NotBlank
	private String fromPlace;

	@NotBlank
	private String toPlace;

	@NotNull
	private LocalDateTime departureDateTime;

	@NotNull
	private LocalDateTime arrivalDateTime;

	@NotEmpty
	private List<Seat> seats;

}
