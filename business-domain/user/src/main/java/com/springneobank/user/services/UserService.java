package com.springneobank.user.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springneobank.user.entities.User;
import com.springneobank.user.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository uRepository;

    /**
     * Create user in database
     * 
     * @param id
     * @param phone
     * @param status
     * @return
     */
    public User createUser(Long id, String phone, boolean status) {
        return uRepository.save(new User(id, phone, status));
    }

    /**
     * Deactivate: set status 0 in database
     * 
     * @param id
     * @return
     */
    public User deactivateUserByID(Long id) {
        User user = uRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario "+ id +" no encontrado"));

        user.setStatus(false);
        uRepository.save(user);

        return user;
    }
}
