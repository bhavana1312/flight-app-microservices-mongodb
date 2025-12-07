package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.NotificationMessage;
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
	private final NotificationProducer notificationProducer;

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
				.journeyDate(flight.getDepartureDateTime()).bookingStatus("ACTIVE").build();

		bookingRepository.save(booking);

		notificationProducer.sendNotification(
				NotificationMessage.builder().pnr(pnr).email(request.getUserEmail()).userName(request.getUserName())
						.flightId(flightId).status("BOOKED").seats(request.getSelectedSeats()).build());

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

		if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
			throw new BadRequestException("Ticket is already cancelled.");
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime journeyTime = booking.getJourneyDate();

		if (journeyTime.minusHours(24).isBefore(now)) {
			throw new BadRequestException("Cannot cancel within 24 hours of journey.");
		}

		Flight flight = flightClient.getFlightById(booking.getFlightId());
		if (flight == null) {
			throw new BadRequestException("Flight not found for cancellation.");
		}

		for (String seatNum : booking.getSelectedSeats()) {
			flight.getSeats().forEach(seat -> {
				if (seat.getSeatNumber().equals(seatNum)) {
					seat.setReserved(false);
				}
			});
		}

		flightClient.updateSeats(flight.getId(), flight);

		booking.setBookingStatus("CANCELLED");
		bookingRepository.save(booking);

		notificationProducer.sendNotification(
				NotificationMessage.builder().pnr(pnr).email(booking.getUserEmail()).userName(booking.getUserName())
						.flightId(booking.getFlightId()).seats(booking.getSelectedSeats()).status("CANCELLED").build());

		return "Ticket cancelled successfully";
	}

}
