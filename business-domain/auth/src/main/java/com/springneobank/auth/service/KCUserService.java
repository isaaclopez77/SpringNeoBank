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
    public KCUser createKCUser(UUID keycloakID) {
        return uRepository.save(new KCUser(keycloakID, true));
    }

    /**
     * Deactivate: Set user status 0 in Database
     * 
     * @param kcuser
     * @return
     */
    public KCUser deactivateUser(KCUser kcuser) {
        kcuser.setStatus(false);
        return uRepository.save(kcuser);
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

    /**
     * Get Keycloak ID by user ID
     * 
     * @param id
     * @return
     */
    public UUID getKeycloakIDByUserID(Long id) {
        UUID kcID = null;

        Optional<KCUser> optUser = uRepository.findById(id);
        if(optUser.isPresent()) {
            kcID = optUser.get().getKeycloakID();
        }

        return kcID;
    }

}
