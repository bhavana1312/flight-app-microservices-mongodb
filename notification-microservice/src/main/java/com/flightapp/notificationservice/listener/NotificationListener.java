//package com.flightapp.notificationservice.listener;
//
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//import com.flightapp.notificationservice.dto.NotificationMessage;
//
//@Service
//public class NotificationListener {
//
//	@RabbitListener(queues = "booking.queue")
//	public void handleNotification(NotificationMessage message) {
//		System.out.println("\n");
//		System.out.println("Cancellation Notification Received:");
//		System.out.println("PNR: " + message.getPnr());
//		System.out.println("Email: " + message.getEmail());
//		System.out.println("Flight: " + message.getFlightId());
//		System.out.println("Status: " + message.getStatus());
//		System.out.println("Seats: " + message.getSeats());
//	}
//}

package com.flightapp.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.flightapp.notificationservice.dto.NotificationMessage;
import com.flightapp.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "booking.queue")
    public void handleNotification(NotificationMessage message) {

        System.out.println("📩 Received Notification for Email Sending:");
        System.out.println(message);

        emailService.sendBookingConfirmation(message);
    }
}

