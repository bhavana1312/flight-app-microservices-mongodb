package com.flightapp.flightservice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.flightapp.flightservice.security.JwtAuthFilter;

import jakarta.servlet.Filter;

class SecurityConfigTest {

	private JwtAuthFilter jwtAuthFilter;
	private SecurityConfig securityConfig;

	@BeforeEach
	void setup() {
		jwtAuthFilter = mock(JwtAuthFilter.class);
		securityConfig = new SecurityConfig(jwtAuthFilter);
	}

	@Test
	void testSecurityFilterChain() throws Exception {

		HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

		when(http.csrf(any())).thenReturn(http);
		when(http.sessionManagement(any())).thenReturn(http);
		when(http.authorizeHttpRequests(any())).thenReturn(http);
		when(http.addFilterBefore(any(), any())).thenReturn(http);

		SecurityFilterChain chain = securityConfig.securityFilterChain(http);

		assertNotNull(chain);

		ArgumentCaptor<JwtAuthFilter> filterCaptor = ArgumentCaptor.forClass(JwtAuthFilter.class);
		ArgumentCaptor<Class<?>> classCaptor = ArgumentCaptor.forClass(Class.class);

		verify(http).addFilterBefore(filterCaptor.capture(), (Class<? extends Filter>) classCaptor.capture());

		assertEquals(jwtAuthFilter, filterCaptor.getValue());
		assertEquals(UsernamePasswordAuthenticationFilter.class, classCaptor.getValue());

		verify(http).sessionManagement(any());

		verify(http).authorizeHttpRequests(any());
	}
}
