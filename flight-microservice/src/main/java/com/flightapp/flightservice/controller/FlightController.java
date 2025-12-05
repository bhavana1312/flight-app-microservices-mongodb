package com.flightapp.flightservice.controller;

import com.flightapp.flightservice.dto.FlightInventoryRequest;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightSearchResponse;
import com.flightapp.flightservice.service.FlightService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
@Validated
public class FlightController {

	private final FlightService flightService;

	@PostMapping("/airline/inventory/add")
	public ResponseEntity<Map<String, String>> addInventory(@Valid @RequestBody FlightInventoryRequest request) {
		String id = flightService.addInventory(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
	}

	@PostMapping("/search")
	public ResponseEntity<List<FlightSearchResponse>> search(@Valid @RequestBody FlightSearchRequest request) {
		List<FlightSearchResponse> results = flightService.searchFlights(request);
		return ResponseEntity.ok(results);
	}
}
