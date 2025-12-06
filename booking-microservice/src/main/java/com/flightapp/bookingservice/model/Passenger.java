package com.flightapp.bookingservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    private String name;
    private String gender;
    private int age;
}
