package com.flightapp.notificationservice.dto;

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
}
