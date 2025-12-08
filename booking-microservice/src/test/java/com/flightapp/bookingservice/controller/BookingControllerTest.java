package com.flightapp.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.bookingservice.TestConfig;
import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.model.Passenger;
import com.flightapp.bookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
class BookingControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	BookingService bookingService;

	@Autowired
	ObjectMapper mapper;

	BookingRequest bookingRequest;
	Booking booking;

	@BeforeEach
	void setup() {
		bookingRequest = BookingRequest.builder().userName("pandhiiii").userEmail("p@x.com").numberOfSeats(1)
				.passengers(List.of(Passenger.builder().name("A").age(20).gender("M").build()))
				.selectedSeats(List.of("E1")).mealPreference("Veg").build();

		booking = Booking.builder().id("PNR12345").flightId("F1").userName("priya").userEmail("p@x.com")
				.numberOfSeats(1).passengers(bookingRequest.getPassengers())
				.selectedSeats(bookingRequest.getSelectedSeats()).mealPreference("Veg").bookingTime(LocalDateTime.now())
				.journeyDate(LocalDateTime.now().plusDays(2)).bookingStatus("ACTIVE").build();
	}

	@Test
	void testBookTicket() throws Exception {
		when(bookingService.bookTicket(eq("F1"), any())).thenReturn("PNR12345");

		mockMvc.perform(post("/booking/F1").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(bookingRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.pnr").value("PNR12345"));
	}

	@Test
	void testGetTicket() throws Exception {
		when(bookingService.getBookingByPnr("PNR12345")).thenReturn(booking);

		mockMvc.perform(get("/booking/ticket/PNR12345")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("PNR12345"));
	}

	@Test
	void testGetHistory() throws Exception {
		when(bookingService.getHistoryByEmail("p@x.com")).thenReturn(List.of(booking));

		mockMvc.perform(get("/booking/history/p@x.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("PNR12345"));
	}

	@Test
	void testCancel() throws Exception {
		when(bookingService.cancelBooking("PNR12345")).thenReturn("Ticket cancelled successfully");

		mockMvc.perform(delete("/booking/cancel/PNR12345")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ticket cancelled successfully"));
	}
}
