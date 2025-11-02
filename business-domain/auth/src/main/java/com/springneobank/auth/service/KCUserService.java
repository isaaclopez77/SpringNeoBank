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
     * Remove user by Keycloack ID
     * 
     * @param keycloakID
     */
    public void removeUserByKeycloakID(UUID keycloakID) {
        Optional<KCUser> optUser = uRepository.findByKeycloakID(keycloakID);

        if(optUser.isPresent()) {
            uRepository.delete(optUser.get());
        }
    }

}
