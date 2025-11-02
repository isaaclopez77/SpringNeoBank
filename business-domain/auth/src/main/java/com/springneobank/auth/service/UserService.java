package com.springneobank.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springneobank.auth.entities.User;
import com.springneobank.auth.repositories.UsersRepository;

@Service
public class UserService {

    @Autowired
    private UsersRepository uRepository;

    /**
     * Remove user by Keycloack ID
     * 
     * @param keycloakID
     */
    public void removeUserByKeycloakID(UUID keycloakID) {
        Optional<User> optUser = uRepository.findByKeycloakID(keycloakID);

        if(optUser.isPresent()) {
            uRepository.delete(optUser.get());
        }
    }

}
