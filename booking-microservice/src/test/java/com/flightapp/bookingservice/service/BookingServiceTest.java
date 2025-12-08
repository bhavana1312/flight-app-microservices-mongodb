package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.NotificationMessage;
import com.flightapp.bookingservice.exception.BadRequestException;
import com.flightapp.bookingservice.exception.ResourceNotFoundException;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.feign.dto.Flight;
import com.flightapp.bookingservice.feign.dto.Seat;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.model.Passenger;
import com.flightapp.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    FlightClient flightClient;

    @Mock
    NotificationProducer notificationProducer;

    @Mock
    CircuitBreakerFactory circuitBreakerFactory;

    @Mock
    CircuitBreaker circuitBreaker;

    @InjectMocks
    BookingService bookingService;

    BookingRequest req;
    Flight flight;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(i -> {
            var supplier = (java.util.function.Supplier<?>) i.getArguments()[0];
            return supplier.get();
        });

        req = BookingRequest.builder()
                .userName("bhavana")
                .userEmail("b@x.com")
                .numberOfSeats(1)
                .passengers(List.of(Passenger.builder().name("A").age(20).gender("F").build()))
                .selectedSeats(List.of("E1"))
                .mealPreference("Veg")
                .build();

        Seat s1 = Seat.builder()
                .seatNumber("E1")
                .seatType("ECONOMY")
                .reserved(false)
                .price(2000)
                .build();

        flight = Flight.builder()
                .id("F1")
                .airlineName("Indigo")
                .airlineLogoUrl("logo")
                .fromPlace("DEL")
                .toPlace("BLR")
                .departureDateTime(LocalDateTime.now().plusDays(2))
                .arrivalDateTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .seats(List.of(s1))
                .build();
    }

    @Test
    void testBookTicketSuccess() {
        when(flightClient.getFlightById("F1")).thenReturn(flight);
        when(bookingRepository.save(any())).thenReturn(null);

        String pnr = bookingService.bookTicket("F1", req);

        assertNotNull(pnr);
        verify(flightClient).updateSeats(eq("F1"), any());
        verify(notificationProducer).sendNotification(any(NotificationMessage.class));
    }

    @Test
    void testBookTicket_FlightNull() {
        when(flightClient.getFlightById("F1")).thenReturn(null);
        assertThrows(BadRequestException.class, () -> bookingService.bookTicket("F1", req));
    }

    @Test
    void testBookTicket_PassengerCountMismatch() {
        req.setNumberOfSeats(2);
        when(flightClient.getFlightById("F1")).thenReturn(flight);
        assertThrows(BadRequestException.class, () -> bookingService.bookTicket("F1", req));
    }

    @Test
    void testBookTicket_SelectedSeatsMismatch() {
        req.setSelectedSeats(List.of("E1", "E2"));
        when(flightClient.getFlightById("F1")).thenReturn(flight);
        assertThrows(BadRequestException.class, () -> bookingService.bookTicket("F1", req));
    }

    @Test
    void testBookTicket_SeatNotFound() {
        req.setSelectedSeats(List.of("X1"));
        when(flightClient.getFlightById("F1")).thenReturn(flight);
        assertThrows(BadRequestException.class, () -> bookingService.bookTicket("F1", req));
    }

    @Test
    void testBookTicket_SeatReserved() {
        flight.getSeats().get(0).setReserved(true);
        when(flightClient.getFlightById("F1")).thenReturn(flight);
        assertThrows(BadRequestException.class, () -> bookingService.bookTicket("F1", req));
    }

    @Test
    void testGetBookingByPnr_Success() {
        Booking b = Booking.builder().id("P1").build();
        when(bookingRepository.findById("P1")).thenReturn(Optional.of(b));
        assertEquals("P1", bookingService.getBookingByPnr("P1").getId());
    }

    @Test
    void testGetBookingByPnr_NotFound() {
        when(bookingRepository.findById("X")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingByPnr("X"));
    }

    @Test
    void testGetHistoryByEmail_Success() {
        Booking b = Booking.builder().id("P1").build();
        when(bookingRepository.findByUserEmailIgnoreCase("b@x.com")).thenReturn(List.of(b));
        assertEquals(1, bookingService.getHistoryByEmail("b@x.com").size());
    }

    @Test
    void testGetHistoryByEmail_Empty() {
        when(bookingRepository.findByUserEmailIgnoreCase("b@x.com")).thenReturn(Collections.emptyList());
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getHistoryByEmail("b@x.com"));
    }

    @Test
    void testCancelBooking_Success() {
        Booking b = Booking.builder()
                .id("P1")
                .flightId("F1")
                .userEmail("b@x.com")
                .userName("bhavana")
                .selectedSeats(List.of("E1"))
                .journeyDate(LocalDateTime.now().plusDays(3))
                .bookingStatus("ACTIVE")
                .build();

        when(bookingRepository.findById("P1")).thenReturn(Optional.of(b));
        when(flightClient.getFlightById("F1")).thenReturn(flight);

        String msg = bookingService.cancelBooking("P1");

        assertEquals("Ticket cancelled successfully", msg);
        verify(flightClient).updateSeats(eq("F1"), any());
        verify(notificationProducer).sendNotification(any(NotificationMessage.class));
    }

    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findById("X")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking("X"));
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        Booking b = Booking.builder().id("P1").bookingStatus("CANCELLED").build();
        when(bookingRepository.findById("P1")).thenReturn(Optional.of(b));
        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking("P1"));
    }

    @Test
    void testCancelBooking_TooLate() {
        Booking b = Booking.builder()
                .id("P1")
                .journeyDate(LocalDateTime.now().plusHours(10))
                .bookingStatus("ACTIVE")
                .build();

        when(bookingRepository.findById("P1")).thenReturn(Optional.of(b));
        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking("P1"));
    }

    @Test
    void testCancelBooking_FlightNull() {
        Booking b = Booking.builder()
                .id("P1")
                .flightId("F1")
                .userEmail("b@x.com")
                .userName("bhavana")
                .selectedSeats(List.of("E1"))
                .journeyDate(LocalDateTime.now().plusDays(3))
                .bookingStatus("ACTIVE")
                .build();

        when(bookingRepository.findById("P1")).thenReturn(Optional.of(b));
        when(flightClient.getFlightById("F1")).thenReturn(null);

        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking("P1"));
    }
}
