package com.flightapp.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration}")
	private long jwtExpirationMs;

	public String generateJwtToken(UserDetailsImpl user) {

		return Jwts.builder().setSubject(user.getUsername())
				.claim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).compact();
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token).getBody()
				.getSubject();
	}
}
