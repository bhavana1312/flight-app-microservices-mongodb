package com.flightapp.authservice.controller;

import com.flightapp.authservice.model.ERole;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.flightapp.authservice.model.Role;
import com.flightapp.authservice.model.User;
import com.flightapp.authservice.payload.request.ChangePasswordRequest;
import com.flightapp.authservice.payload.request.LoginRequest;
import com.flightapp.authservice.payload.request.SignupRequest;
import com.flightapp.authservice.payload.response.JwtResponse;
import com.flightapp.authservice.repository.RoleRepository;
import com.flightapp.authservice.repository.UserRepository;
import com.flightapp.authservice.security.JwtUtils;
import com.flightapp.authservice.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authManager;
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final PasswordEncoder encoder;
	private final JwtUtils jwtUtils;

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

}
