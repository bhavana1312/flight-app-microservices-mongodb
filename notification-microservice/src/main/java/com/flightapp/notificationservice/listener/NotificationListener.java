package com.flightapp.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.flightapp.notificationservice.dto.NotificationMessage;

@Service
public class NotificationListener {

	@RabbitListener(queues = "booking.queue")
	public void handleNotification(NotificationMessage message) {
		System.out.println("📩 Received Notification:");
		System.out.println("PNR: " + message.getPnr());
		System.out.println("Email: " + message.getEmail());
		System.out.println("Message: " + message.getStatus());
	}
}
