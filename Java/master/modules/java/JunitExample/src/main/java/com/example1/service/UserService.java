package com.example1.service;

import com.example1.model.User;

public interface UserService {
    User getUserById(Long id);

    User createUser(User user);

    void deleteUserById(Long id);

    String publicMethod(String input);
}
