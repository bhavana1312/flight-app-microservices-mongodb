package com.flightapp.flightservice.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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
	private LocalDate journeyDate;
}
