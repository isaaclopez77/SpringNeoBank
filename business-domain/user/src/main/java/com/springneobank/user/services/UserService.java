package com.springneobank.user.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springneobank.user.entities.User;
import com.springneobank.user.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository uRepository;

    public User createUser(Long id, String phone, boolean status) {
        return uRepository.save(new User(id, phone, status));
    }
}
