package com.flightapp.authservice;

import com.flightapp.authservice.model.ERole;
import com.flightapp.authservice.model.Role;
import com.flightapp.authservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {

			if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
				roleRepository.save(new Role("1", ERole.ROLE_ADMIN));
				System.out.println("Inserted ROLE_ADMIN");
			}

			if (!roleRepository.existsByName(ERole.ROLE_USER)) {
				roleRepository.save(new Role("2", ERole.ROLE_USER));
				System.out.println("Inserted ROLE_USER");
			}

			System.out.println("Roles initialized successfully.");
		};
	}
}
