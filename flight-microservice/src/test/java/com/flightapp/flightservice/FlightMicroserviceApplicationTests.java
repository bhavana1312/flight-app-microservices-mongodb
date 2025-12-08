package com.flightapp.flightservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;

class FlightServiceApplicationTest {

	@Test
	void testMainMethod() {
		try (MockedStatic<SpringApplication> app = Mockito.mockStatic(SpringApplication.class)) {
			FlightServiceApplication.main(new String[] {});
			app.verify(() -> SpringApplication.run(FlightServiceApplication.class, new String[] {}));
		}
		assertTrue(true);
	}
}
