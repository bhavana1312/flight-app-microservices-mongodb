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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddInventory_Success() {
        FlightInventoryRequest req = FlightInventoryRequest.builder()
                .airlineName("Indigo")
                .airlineLogoUrl("logo")
                .fromPlace("DEL")
                .toPlace("BLR")
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .arrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .aircraftModel("A320")
                .businessSeats(1)
                .economySeats(2)
                .businessSeatPrice(5000)
                .economySeatPrice(3000)
                .build();

        when(flightRepository.findByAirlineNameRegexAndFromPlaceRegexAndToPlaceRegexAndDepartureDateTime(
                anyString(), anyString(), anyString(), any())).thenReturn(Optional.empty());

        Flight saved = Flight.builder().id("123").seats(new ArrayList<>()).build();
        when(flightRepository.save(any(Flight.class))).thenReturn(saved);

        String id = flightService.addInventory(req);

        assertEquals("123", id);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void testAddInventory_AlreadyExists() {
        FlightInventoryRequest req = FlightInventoryRequest.builder()
                .airlineName("Indigo")
                .airlineLogoUrl("logo")
                .fromPlace("DEL")
                .toPlace("BLR")
                .departureDateTime(LocalDateTime.now())
                .arrivalDateTime(LocalDateTime.now().plusHours(2))
                .aircraftModel("A320")
                .build();

        when(flightRepository.findByAirlineNameRegexAndFromPlaceRegexAndToPlaceRegexAndDepartureDateTime(
                anyString(), anyString(), anyString(), any())).thenReturn(Optional.of(new Flight()));

        assertThrows(BadRequestException.class, () -> flightService.addInventory(req));
    }

    @Test
    void testAddInventory_InvalidTimes() {
        LocalDateTime now = LocalDateTime.now();

        FlightInventoryRequest req = FlightInventoryRequest.builder()
                .airlineName("Indigo")
                .airlineLogoUrl("logo")
                .fromPlace("DEL")
                .toPlace("BLR")
                .departureDateTime(now.plusHours(5))
                .arrivalDateTime(now.plusHours(1))
                .aircraftModel("A320")
                .build();

        when(flightRepository.findByAirlineNameRegexAndFromPlaceRegexAndToPlaceRegexAndDepartureDateTime(
                anyString(), anyString(), anyString(), any())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> flightService.addInventory(req));
    }

    @Test
    void testSearchFlights_Success() {
        LocalDate journey = LocalDate.now();
        FlightSearchRequest req = FlightSearchRequest.builder()
                .fromPlace("DEL")
                .toPlace("BLR")
                .journeyDate(journey)
                .build();

        Seat s1 = new Seat("E1", SeatType.ECONOMY, false, 2500);
        Seat s2 = new Seat("B1", SeatType.BUSINESS, true, 6000);

        Flight f = Flight.builder()
                .id("F001")
                .airlineName("Indigo")
                .airlineLogoUrl("logo")
                .fromPlace("DEL")
                .toPlace("BLR")
                .departureDateTime(journey.atStartOfDay().plusHours(9))
                .arrivalDateTime(journey.atStartOfDay().plusHours(12))
                .seats(List.of(s1, s2))
                .build();

        when(flightRepository.findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureDateTimeBetween(
                anyString(), anyString(), any(), any())).thenReturn(List.of(f));

        List<FlightSearchResponse> list = flightService.searchFlights(req);

        assertEquals(1, list.size());
        assertEquals("F001", list.get(0).getFlightId());
        assertEquals(1, list.get(0).getAvailableSeats());
        assertEquals(2500, list.get(0).getLowestPrice());
    }

    @Test
    void testSearchFlights_NoJourneyDate() {
        FlightSearchRequest req = FlightSearchRequest.builder()
                .fromPlace("DEL")
                .toPlace("BLR")
                .journeyDate(null)
                .build();

        assertThrows(BadRequestException.class, () -> flightService.searchFlights(req));
    }

    @Test
    void testSearchFlights_NoFlightsFound() {
        FlightSearchRequest req = FlightSearchRequest.builder()
                .fromPlace("DEL")
                .toPlace("BLR")
                .journeyDate(LocalDate.now())
                .build();

        when(flightRepository.findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureDateTimeBetween(
                anyString(), anyString(), any(), any())).thenReturn(Collections.emptyList());

        List<FlightSearchResponse> list = flightService.searchFlights(req);

        assertTrue(list.isEmpty());
    }

    @Test
    void testUpdateSeats_Success() {
        Flight existing = Flight.builder().id("F001").seats(new ArrayList<>()).build();
        Flight updated = Flight.builder()
                .seats(List.of(new Seat("E1", SeatType.ECONOMY, false, 2000)))
                .build();

        when(flightRepository.findById("F001")).thenReturn(Optional.of(existing));

        flightService.updateSeats("F001", updated);

        assertEquals(1, existing.getSeats().size());
        verify(flightRepository).save(existing);
    }

    @Test
    void testUpdateSeats_NotFound() {
        when(flightRepository.findById("F002")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> flightService.updateSeats("F002", new Flight()));
    }

    @Test
    void testGetFlightById() {
        Flight f = new Flight();
        when(flightRepository.findById("X1")).thenReturn(Optional.of(f));

        Optional<Flight> result = flightService.getFlightById("X1");

        assertTrue(result.isPresent());
    }
}
