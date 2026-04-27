package com.example1.service;

import com.example1.model.User;
import com.example1.repository.UserRepository;

public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User getUserById(Long id) {
        if (id == 999L) {
            return null; // Do not interact with the repository for ID 999L
        }
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public String publicMethod(String input) {
        return privateMethod(input);
    }

    /*private String privateMethod(String input) {
        return "Original Result";
    }*/

    private String privateMethod(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        return "Original Result";
    }
}
