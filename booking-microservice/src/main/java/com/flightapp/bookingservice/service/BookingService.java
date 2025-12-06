package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.exception.BadRequestException;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.feign.dto.Flight;
import com.flightapp.bookingservice.feign.dto.Seat;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository bookingRepository;
	private final FlightClient flightClient;

	public String bookTicket(String flightId, BookingRequest request) {

		Flight flight = flightClient.getFlightById(flightId);
		if (flight == null)
			throw new BadRequestException("Flight not found");

		if (request.getNumberOfSeats() != request.getPassengers().size())
			throw new BadRequestException("Passengers count mismatch with numberOfSeats");

		if (request.getSelectedSeats().size() != request.getNumberOfSeats())
			throw new BadRequestException("Selected seats mismatch");

		for (String seatNum : request.getSelectedSeats()) {

			Seat seat = flight.getSeats().stream().filter(s -> s.getSeatNumber().equals(seatNum)).findFirst()
					.orElseThrow(() -> new BadRequestException("Seat not found: " + seatNum));

			if (seat.isReserved())
				throw new BadRequestException("Seat already reserved: " + seatNum);
		}

		for (Seat seat : flight.getSeats()) {
			if (request.getSelectedSeats().contains(seat.getSeatNumber())) {
				seat.setReserved(true);
			}
		}

		flightClient.updateSeats(flightId, flight);

		String pnr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

		Booking booking = Booking.builder().id(pnr).flightId(flightId).userName(request.getUserName())
				.userEmail(request.getUserEmail()).numberOfSeats(request.getNumberOfSeats())
				.passengers(request.getPassengers()).selectedSeats(request.getSelectedSeats())
				.mealPreference(request.getMealPreference()).bookingTime(LocalDateTime.now())
				.journeyDate(flight.getDepartureDateTime()).build();

		bookingRepository.save(booking);

		return pnr;
	}
}
