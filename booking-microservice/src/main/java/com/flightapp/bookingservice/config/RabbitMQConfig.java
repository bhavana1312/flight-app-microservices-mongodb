package com.flightapp.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String BOOKING_EXCHANGE = "booking.exchange";
	public static final String BOOKING_QUEUE = "booking.queue";
	public static final String BOOKING_ROUTING_KEY = "booking.key";

	@Bean
	public TopicExchange bookingExchange() {
		return new TopicExchange(BOOKING_EXCHANGE);
	}

	@Bean
	public Queue bookingQueue() {
		return new Queue(BOOKING_QUEUE, true);
	}

	@Bean
	public Binding binding() {
		return BindingBuilder.bind(bookingQueue()).to(bookingExchange()).with(BOOKING_ROUTING_KEY);
	}
}
