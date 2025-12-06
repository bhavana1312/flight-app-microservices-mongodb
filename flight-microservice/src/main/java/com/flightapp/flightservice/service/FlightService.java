package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.FlightInventoryRequest;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightSearchResponse;
import com.flightapp.flightservice.exception.BadRequestException;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.model.Flight;
import com.flightapp.flightservice.model.Seat;
import com.flightapp.flightservice.model.SeatType;
import com.flightapp.flightservice.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

	private final FlightRepository flightRepository;

	public String addInventory(FlightInventoryRequest request) {

		Optional<Flight> existing = flightRepository
				.findByAirlineNameRegexAndFromPlaceRegexAndToPlaceRegexAndDepartureDateTime(
						"^" + request.getAirlineName() + "$", "^" + request.getFromPlace() + "$",
						"^" + request.getToPlace() + "$", request.getDepartureDateTime());

		if (existing.isPresent()) {
			throw new BadRequestException("Flight already exists for this airline, route, and departure time");
		}

		if (request.getDepartureDateTime().isAfter(request.getArrivalDateTime())) {
			throw new BadRequestException("Arrival must be after departure");
		}

		List<Seat> seats = new ArrayList<>();
		for (int i = 1; i <= request.getBusinessSeats(); i++) {
			seats.add(new Seat("B" + i, SeatType.BUSINESS, false, request.getBusinessSeatPrice()));
		}
		for (int i = 1; i <= request.getEconomySeats(); i++) {
			seats.add(new Seat("E" + i, SeatType.ECONOMY, false, request.getEconomySeatPrice()));
		}
		Flight flight = Flight.builder().airlineName(request.getAirlineName())
				.airlineLogoUrl(request.getAirlineLogoUrl()).fromPlace(request.getFromPlace())
				.toPlace(request.getToPlace()).departureDateTime(request.getDepartureDateTime())
				.arrivalDateTime(request.getArrivalDateTime()).seats(seats).build();

		Flight saved = flightRepository.save(flight);
		return saved.getId();
	}

	public List<FlightSearchResponse> searchFlights(FlightSearchRequest req) {

		LocalDate date = req.getJourneyDate();
		if (date == null) {
			throw new BadRequestException("Journey date is required");
		}

		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(23, 59, 59);

		List<Flight> flights = flightRepository
				.findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureDateTimeBetween(req.getFromPlace(),
						req.getToPlace(), start, end);

		return flights
				.stream().map(
						f -> FlightSearchResponse.builder().flightId(f.getId()).airlineName(f.getAirlineName())
								.airlineLogoUrl(f.getAirlineLogoUrl()).departureDateTime(f.getDepartureDateTime())
								.arrivalDateTime(f.getArrivalDateTime()).fromPlace(f.getFromPlace())
								.toPlace(f.getToPlace())
								.availableSeats((int) f.getSeats().stream().filter(s -> !s.isReserved()).count())
								.lowestPrice(f.getSeats().stream().filter(s -> !s.isReserved())
										.mapToDouble(Seat::getPrice).min().orElse(0))
								.build())
				.collect(Collectors.toList());
	}

	public void updateSeats(String id, Flight updatedFlight) {

		Flight existing = flightRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));

		existing.setSeats(updatedFlight.getSeats());

		flightRepository.save(existing);
	}

	public Optional<Flight> getFlightById(String id) {
		return flightRepository.findById(id);
	}
}
