package com.flightapp.bookingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    private String id; 

    private String flightId;

    private String userName;
    private String userEmail;

    private int numberOfSeats;

    private List<Passenger> passengers;

    private List<String> selectedSeats;

    private String mealPreference; 

    private LocalDateTime bookingTime;

    private LocalDateTime journeyDate; 
}
