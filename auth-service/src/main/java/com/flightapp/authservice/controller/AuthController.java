package com.flightapp.authservice.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.authservice.model.ERole;
import com.flightapp.authservice.model.PasswordResetToken;
import com.flightapp.authservice.model.Role;
import com.flightapp.authservice.model.User;
import com.flightapp.authservice.payload.request.ChangePasswordRequest;
import com.flightapp.authservice.payload.request.ForgotPasswordRequest;
import com.flightapp.authservice.payload.request.LoginRequest;
import com.flightapp.authservice.payload.request.PasswordResetMessage;
import com.flightapp.authservice.payload.request.ResetPasswordRequest;
import com.flightapp.authservice.payload.request.SignupRequest;
import com.flightapp.authservice.payload.response.JwtResponse;
import com.flightapp.authservice.repository.PasswordResetTokenRepository;
import com.flightapp.authservice.repository.RoleRepository;
import com.flightapp.authservice.repository.UserRepository;
import com.flightapp.authservice.security.JwtUtils;
import com.flightapp.authservice.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authManager;
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final PasswordEncoder encoder;
	private final JwtUtils jwtUtils;
	private final RabbitTemplate rabbitTemplate;
	private final PasswordResetTokenRepository tokenRepo;

	@PostMapping("/signup")
	public ResponseEntity<?> register(@RequestBody SignupRequest req) {

		if (userRepo.existsByUsername(req.getUsername())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Username already exists"));
		}

		if (userRepo.existsByEmail(req.getEmail())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email already exists"));
		}

		User user = new User();
		user.setUsername(req.getUsername());
		user.setEmail(req.getEmail());
		user.setPassword(encoder.encode(req.getPassword()));

		Set<Role> roles = new HashSet<>();

		if (req.getRole() == null) {
			Role userRole = roleRepo.findByName(ERole.ROLE_USER).orElseThrow();
			roles.add(userRole);
		} else {
			req.getRole().forEach(r -> {
				if (r.equalsIgnoreCase("admin")) {
					roles.add(roleRepo.findByName(ERole.ROLE_ADMIN).orElseThrow());
				} else {
					roles.add(roleRepo.findByName(ERole.ROLE_USER).orElseThrow());
				}
			});
		}

		user.setRoles(roles);
		userRepo.save(user);

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
	}

	@PostMapping("/signin")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {

		try {
			var auth = authManager
					.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

			UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();

			String token = jwtUtils.generateJwtToken(user);

			List<String> roles = user.getAuthorities().stream().map(a -> a.getAuthority()).toList();

			return ResponseEntity
					.ok(new JwtResponse(token, "Bearer", user.getId(), user.getUsername(), user.getEmail(), roles));

		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Invalid username or password"));
		}
	}

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req,
			@AuthenticationPrincipal UserDetailsImpl userDetails) {
		User user = userRepo.findById(userDetails.getId()).orElseThrow();

		if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Old password is incorrect"));
		}

		user.setPassword(encoder.encode(req.getNewPassword()));
		userRepo.save(user);

		return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {

		User user = userRepo.findByEmail(req.getEmail()).orElse(null);

		if (user == null) {
			return ResponseEntity.ok(Map.of("message", "If email exists, reset link sent"));
		}

		String token = UUID.randomUUID().toString();

		PasswordResetToken resetToken = PasswordResetToken.builder().token(token).userId(user.getId())
				.expiryTime(LocalDateTime.now().plusMinutes(15)).build();

		tokenRepo.save(resetToken);

		String link = "http://localhost:4200/reset-password?token=" + token;

		PasswordResetMessage msg = new PasswordResetMessage(user.getEmail(), link);

		rabbitTemplate.convertAndSend("password.reset.queue", msg);

		return ResponseEntity.ok(Map.of("message", "Password reset link sent"));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {

		PasswordResetToken token = tokenRepo.findByToken(req.getToken()).orElse(null);

		if (token == null || token.getExpiryTime().isBefore(LocalDateTime.now())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or expired token"));
		}

		User user = userRepo.findById(token.getUserId()).orElseThrow();

		user.setPassword(encoder.encode(req.getNewPassword()));
		userRepo.save(user);

		tokenRepo.delete(token);

		return ResponseEntity.ok(Map.of("message", "Password reset successful"));
	}

}
