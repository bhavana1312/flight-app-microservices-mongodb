package com.flightapp.flightservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

	private JwtAuthFilter filter;

	private final String secret = "12345678901234567890123456789012";

	@BeforeEach
	void setup() throws Exception {
		MockitoAnnotations.openMocks(this);
		filter = new JwtAuthFilter();

		var f = JwtAuthFilter.class.getDeclaredField("jwtSecret");
		f.setAccessible(true);
		f.set(filter, secret);

		SecurityContextHolder.clearContext(); // 🔥 FIX
	}

	String generateToken() {
		var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

		return Jwts.builder().setSubject("pandhiiii").claim("roles", List.of("ROLE_ADMIN"))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	@Test
	void testValidToken() throws Exception {
		SecurityContextHolder.clearContext();

		String token = generateToken();

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("Authorization", "Bearer " + token);

		MockHttpServletResponse res = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(req, res, chain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void testInvalidToken() throws Exception {
		SecurityContextHolder.clearContext();

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("Authorization", "Bearer invalid.token.here");

		MockHttpServletResponse res = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(req, res, chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void testNoHeader() throws Exception {
		SecurityContextHolder.clearContext();

		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(req, res, chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
