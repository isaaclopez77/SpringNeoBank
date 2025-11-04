package com.springneobank.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.repositories.KCUsersRepository;

@Service
public class KCUserService {

    @Autowired
    private KCUsersRepository uRepository;


    /**
     * Create KC User in Database
     * 
     * @param keycloakID
     * @param username
     * @param password
     * @param email
     * @param name
     * @param lastName
     */
    public KCUser createKCUser(UUID keycloakID, String username, String password, String email, String name, String lastName) {
        return uRepository.save(new KCUser(keycloakID, username, password, email, name, lastName));
    }

    /**
     * Remove user by Keycloack ID
     * 
     * @param keycloakID
     */
    public void removeUser(KCUser user) {
        uRepository.delete(user);
    }

    /**
     * Get KC UUID by ID
     * 
     * @param id
     * @return
     */
    public KCUser getUserByID(Long id) {
        return uRepository.findById(id).orElse(null);
    }

}
