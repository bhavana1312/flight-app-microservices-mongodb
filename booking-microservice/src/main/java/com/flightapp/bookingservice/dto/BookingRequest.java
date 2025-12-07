package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.model.Passenger;
import lombok.*;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

	@NotBlank
	private String userName;

	@Email
	@NotBlank
	private String userEmail;

	@Positive
	private int numberOfSeats;

	@NotEmpty
	private List<Passenger> passengers;

	@NotEmpty
	private List<String> selectedSeats;

	@NotBlank
	private String mealPreference;
}
