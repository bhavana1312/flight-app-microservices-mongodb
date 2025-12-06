package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.exception.BadRequestException;
import com.flightapp.bookingservice.exception.ResourceNotFoundException;
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

	public Booking getBookingByPnr(String pnr) {
		return bookingRepository.findById(pnr)
				.orElseThrow(() -> new ResourceNotFoundException("No ticket found for PNR: " + pnr));
	}

	public List<Booking> getHistoryByEmail(String email) {
		List<Booking> bookings = bookingRepository.findByUserEmailIgnoreCase(email);
		if (bookings.isEmpty())
			throw new ResourceNotFoundException("No bookings found for email: " + email);

		return bookings;
	}

	public String cancelBooking(String pnr) {

		Booking booking = bookingRepository.findById(pnr)
				.orElseThrow(() -> new ResourceNotFoundException("No ticket found for PNR: " + pnr));

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime journeyDateTime = booking.getJourneyDate();

		if (journeyDateTime.minusHours(24).isBefore(now)) {
			throw new BadRequestException("Ticket cannot be cancelled within 24 hours of journey.");
		}

		Flight flight = flightClient.getFlightById(booking.getFlightId());
		if (flight == null)
			throw new BadRequestException("Flight not found for cancellation");

		for (String seatNumber : booking.getSelectedSeats()) {
			flight.getSeats().forEach(seat -> {
				if (seat.getSeatNumber().equals(seatNumber)) {
					seat.setReserved(false);
				}
			});
		}

		flightClient.updateSeats(flight.getId(), flight);

		bookingRepository.deleteById(pnr);

		return "Ticket cancelled successfully";
	}

}
