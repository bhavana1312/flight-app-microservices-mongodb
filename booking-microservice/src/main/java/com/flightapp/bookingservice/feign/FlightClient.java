package com.flightapp.bookingservice.feign;

import com.flightapp.bookingservice.feign.dto.Flight;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service", url = "http://localhost:8081")
public interface FlightClient {

	@GetMapping("/api/flight/{id}")
	Flight getFlightById(@PathVariable String id);

	@PutMapping("/api/flight/update-seats/{id}")
	void updateSeats(@PathVariable String id, @RequestBody Flight flight);
}
