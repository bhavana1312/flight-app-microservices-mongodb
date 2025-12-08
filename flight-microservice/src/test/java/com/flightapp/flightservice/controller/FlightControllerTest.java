package com.flightapp.flightservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.flightservice.dto.FlightInventoryRequest;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightSearchResponse;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.model.Flight;
import com.flightapp.flightservice.model.Seat;
import com.flightapp.flightservice.model.SeatType;
import com.flightapp.flightservice.service.FlightService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightControllerTest {

	@InjectMocks
	private FlightController flightController;

	@Mock
	private FlightService flightService;

	private ObjectMapper mapper = new ObjectMapper();

	FlightInventoryRequest invReq;
	FlightSearchRequest searchReq;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		invReq = FlightInventoryRequest.builder().airlineName("Indigo").airlineLogoUrl("logo").fromPlace("DEL")
				.toPlace("BLR").departureDateTime(LocalDateTime.now().plusDays(1))
				.arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2)).aircraftModel("A320").businessSeats(1)
				.economySeats(1).businessSeatPrice(5000).economySeatPrice(3000).build();

		searchReq = FlightSearchRequest.builder().fromPlace("DEL").toPlace("BLR").journeyDate(LocalDate.now()).build();
	}

	@Test
	void testAddInventory() {
		when(flightService.addInventory(any())).thenReturn("123");

		ResponseEntity<?> response = flightController.addInventory(invReq);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		assertEquals("123", ((Map<?, ?>) response.getBody()).get("id"));
	}

	@Test
	void testSearch() {
		FlightSearchResponse res = FlightSearchResponse.builder().flightId("F1").airlineName("Indigo")
				.airlineLogoUrl("logo").fromPlace("DEL").toPlace("BLR").departureDateTime(LocalDateTime.now())
				.arrivalDateTime(LocalDateTime.now().plusHours(2)).availableSeats(5).lowestPrice(2000).build();

		when(flightService.searchFlights(any())).thenReturn(List.of(res));

		ResponseEntity<?> response = flightController.search(searchReq);

		List<FlightSearchResponse> list = (List<FlightSearchResponse>) response.getBody();
		FlightSearchResponse obj = list.get(0);

		assertEquals("F1", obj.getFlightId());

		assertEquals(HttpStatus.OK, response.getStatusCode());

	}

	@Test
	void testGetFlightById() {
		Seat s = new Seat("E1", SeatType.ECONOMY, false, 2000);
		Flight f = Flight.builder().id("X1").airlineName("Indigo").airlineLogoUrl("logo").fromPlace("DEL")
				.toPlace("BLR").departureDateTime(LocalDateTime.now()).arrivalDateTime(LocalDateTime.now().plusHours(2))
				.seats(List.of(s)).build();

		when(flightService.getFlightById("X1")).thenReturn(Optional.of(f));

		ResponseEntity<?> response = flightController.getFlightById("X1");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertEquals("X1", ((Flight) response.getBody()).getId());
	}

	@Test
	void testGetFlightById_NotFound() {
		when(flightService.getFlightById("BAD")).thenThrow(new ResourceNotFoundException("Flight not found"));

		assertThrows(ResourceNotFoundException.class, () -> flightController.getFlightById("BAD"));
	}

	@Test
	void testUpdateSeats() {
		Flight f = new Flight();

		doNothing().when(flightService).updateSeats(eq("X1"), any());

		ResponseEntity<?> response = flightController.updateSeats("X1", f);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertEquals("Seats updated successfully", ((Map<?, ?>) response.getBody()).get("message"));
	}
}
