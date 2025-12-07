package com.flightapp.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String BOOKING_QUEUE = "booking.queue";

	@Bean
	public Queue queue() {
		return new Queue(BOOKING_QUEUE, true);
	}
}
