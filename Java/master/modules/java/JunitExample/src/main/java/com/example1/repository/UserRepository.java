package com.example1.repository;

import com.example1.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);

    User save(User user);

    void deleteById(Long id);
}
