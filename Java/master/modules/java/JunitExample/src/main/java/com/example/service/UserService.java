package com.example.service;

import com.example.model.User;

public interface UserService {
    User getUserById(Long id);

    User createUser(User user);
}
