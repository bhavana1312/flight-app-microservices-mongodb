package com.flightapp.authservice.controller;

import com.flightapp.authservice.model.ERole;
import com.flightapp.authservice.model.Role;
import com.flightapp.authservice.model.User;
import com.flightapp.authservice.payload.request.LoginRequest;
import com.flightapp.authservice.payload.request.SignupRequest;
import com.flightapp.authservice.payload.response.JwtResponse;
import com.flightapp.authservice.repository.RoleRepository;
import com.flightapp.authservice.repository.UserRepository;
import com.flightapp.authservice.security.JwtUtils;
import com.flightapp.authservice.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public String register(@RequestBody SignupRequest req) {

        if (userRepo.existsByUsername(req.getUsername()))
            return "Username already exists";

        if (userRepo.existsByEmail(req.getEmail()))
            return "Email already exists";

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));

        Set<Role> roles = new HashSet<>();

        if (req.getRole() == null) {
            Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                    .orElseThrow();
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

        return "User registered successfully!";
    }

    @PostMapping("/signin")
    public JwtResponse login(@RequestBody LoginRequest req) {

        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();

        String token = jwtUtils.generateJwtToken(user);

        List<String> roles = user.getAuthorities()
                .stream().map(a -> a.getAuthority()).toList();

        return new JwtResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}
