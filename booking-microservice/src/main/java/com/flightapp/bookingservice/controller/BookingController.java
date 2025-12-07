package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.service.BookingService;
import com.flightapp.bookingservice.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	@PostMapping("/{flightId}")
	public ResponseEntity<Map<String, String>> bookTicket(@PathVariable String flightId,
			@Valid @RequestBody BookingRequest request) {

		String pnr = bookingService.bookTicket(flightId, request);

		return ResponseEntity.ok(Map.of("pnr", pnr));
	}

	@GetMapping("/ticket/{pnr}")
	public ResponseEntity<Booking> getTicket(@PathVariable String pnr) {
		return ResponseEntity.ok(bookingService.getBookingByPnr(pnr));
	}

	@GetMapping("/history/{email}")
	public ResponseEntity<List<Booking>> getHistory(@PathVariable String email) {
		return ResponseEntity.ok(bookingService.getHistoryByEmail(email));
	}

	@DeleteMapping("/cancel/{pnr}")
	public ResponseEntity<Map<String, String>> cancel(@PathVariable String pnr) {
		String msg = bookingService.cancelBooking(pnr);
		return ResponseEntity.ok(Map.of("message", msg));
	}

}
