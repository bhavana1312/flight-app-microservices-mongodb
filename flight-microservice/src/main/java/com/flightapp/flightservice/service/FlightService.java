package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.FlightInventoryRequest;
import com.flightapp.flightservice.dto.FlightSearchRequest;
import com.flightapp.flightservice.dto.FlightSearchResponse;
import com.flightapp.flightservice.exception.BadRequestException;
import com.flightapp.flightservice.model.Flight;
import com.flightapp.flightservice.model.Seat;
import com.flightapp.flightservice.model.SeatType;
import com.flightapp.flightservice.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
		if (request.getSeats() == null || request.getSeats().isEmpty()) {
			throw new BadRequestException("Seats must be provided");
		}
		Set<String> seatNums = new HashSet<>();
		for (Seat s : request.getSeats()) {
			if (!seatNums.add(s.getSeatNumber())) {
				throw new BadRequestException("Duplicate seat number: " + s.getSeatNumber());
			}
			if (s.getPrice() < 0)
				throw new BadRequestException("Seat price cannot be negative");
			if (s.getSeatType() == null)
				s.setSeatType(SeatType.ECONOMY);
		}
		Flight flight = Flight.builder().airlineName(request.getAirlineName())
				.airlineLogoUrl(request.getAirlineLogoUrl()).fromPlace(request.getFromPlace())
				.toPlace(request.getToPlace()).departureDateTime(request.getDepartureDateTime())
				.arrivalDateTime(request.getArrivalDateTime()).seats(request.getSeats()).build();

		Flight saved = flightRepository.save(flight);
		return saved.getId();
	}

	public List<FlightSearchResponse> searchFlights(FlightSearchRequest req) {
		List<Flight> flights = flightRepository
				.findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureDateTimeBetween(req.getFromPlace(),
						req.getToPlace(), req.getStartDateTime(), req.getEndDateTime());

		return flights.stream().map(f -> {
			int availableSeats = (int) f.getSeats().stream().filter(s -> !s.isReserved()).count();
			double lowestPrice = f.getSeats().stream().filter(s -> !s.isReserved()).filter(s -> {
				if (req.getSeatType() == null)
					return true;
				try {
					return s.getSeatType() == SeatType.valueOf(req.getSeatType());
				} catch (Exception e) {
					return true;
				}
			}).mapToDouble(Seat::getPrice).min().orElse(Double.NaN);

			return FlightSearchResponse.builder().flightId(f.getId()).airlineName(f.getAirlineName())
					.airlineLogoUrl(f.getAirlineLogoUrl()).departureDateTime(f.getDepartureDateTime())
					.arrivalDateTime(f.getArrivalDateTime()).fromPlace(f.getFromPlace()).toPlace(f.getToPlace())
					.availableSeats(availableSeats).lowestPrice(Double.isNaN(lowestPrice) ? 0.0 : lowestPrice).build();
		}).collect(Collectors.toList());
	}

	public Optional<Flight> getFlightById(String id) {
		return flightRepository.findById(id);
	}
}
