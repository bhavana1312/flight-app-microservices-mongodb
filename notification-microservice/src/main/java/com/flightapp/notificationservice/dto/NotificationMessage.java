package com.flightapp.notificationservice.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {
	private String pnr;
	private String email;
	private String userName;
	private String flightId;
	private String status;
	private List<String> seats;
}
