package com.flightapp.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.flightapp.notificationservice.dto.NotificationMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	public void sendBookingConfirmation(NotificationMessage message) {

		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(message.getEmail());
		email.setSubject("Flight Ticket Booking Confirmation - PNR " + message.getPnr());

		email.setText("Hello " + message.getUserName() + ",\n\n" + "Your flight ticket has been booked successfully.\n"
				+ "PNR Number: " + message.getPnr() + "\n" + "Status: " + message.getStatus() + "\n" + "Seats: "
				+ message.getSeats() + "\n\n" + "Thank you for choosing FlightApp ");

		mailSender.send(email);

		System.out.println("📧 Email sent to: " + message.getEmail());
	}
}
