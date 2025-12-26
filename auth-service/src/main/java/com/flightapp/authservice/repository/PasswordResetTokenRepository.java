package com.flightapp.authservice.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.flightapp.authservice.model.PasswordResetToken;

public interface PasswordResetTokenRepository
        extends MongoRepository<PasswordResetToken,String> {

    Optional<PasswordResetToken> findByToken(String token);
}
