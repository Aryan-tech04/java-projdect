package com.studentledger.service;

import com.studentledger.dto.LoginRequest;
import com.studentledger.dto.LoginResponse;
import com.studentledger.exception.InvalidRequestException;
import com.studentledger.model.User;
import com.studentledger.repository.UserRepository;
import com.studentledger.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidRequestException("Email and password are required.");
        }

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new InvalidRequestException("Invalid email or password."));

        if (!PasswordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidRequestException("Invalid email or password.");
        }

        return new LoginResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
