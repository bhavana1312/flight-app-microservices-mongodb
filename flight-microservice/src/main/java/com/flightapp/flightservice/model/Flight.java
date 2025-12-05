package com.flightapp.flightservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {
	@Id
	private String id;

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
	private List<Seat> seats; // seat map

}
