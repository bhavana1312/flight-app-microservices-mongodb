package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/flight/booking")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	@PostMapping("/{flightId}")
	public ResponseEntity<Map<String, String>> bookTicket(@PathVariable String flightId,
			@Valid @RequestBody BookingRequest request) {

		String pnr = bookingService.bookTicket(flightId, request);

		return ResponseEntity.ok(Map.of("pnr", pnr));
	}
}
