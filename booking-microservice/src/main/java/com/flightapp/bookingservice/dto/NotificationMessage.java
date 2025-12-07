package com.flightapp.bookingservice.dto;

import java.io.Serializable;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {
	private String pnr;
	private String email;
	private String userName;
	private String flightId;
	private String status;
}
