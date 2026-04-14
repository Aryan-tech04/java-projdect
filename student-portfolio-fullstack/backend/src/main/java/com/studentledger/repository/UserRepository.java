package com.studentledger.repository;

import com.studentledger.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
}
