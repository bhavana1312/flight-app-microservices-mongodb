package com.flightapp.flightservice.repository;

import com.flightapp.flightservice.model.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends MongoRepository<Flight, String> {
	List<Flight> findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureDateTimeBetween(String fromPlace,
			String toPlace, LocalDateTime start, LocalDateTime end);

	Optional<Flight> findByAirlineNameRegexAndFromPlaceRegexAndToPlaceRegexAndDepartureDateTime(String airlineNameRegex,
			String fromPlaceRegex, String toPlaceRegex, LocalDateTime departureDateTime);

}
