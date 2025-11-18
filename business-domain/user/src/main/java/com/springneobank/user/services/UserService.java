package com.springneobank.user.services;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springneobank.user.common.OperationResult;
import com.springneobank.user.entities.User;
import com.springneobank.user.feign.auth.AuthClient;
import com.springneobank.user.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository uRepository;

    @Autowired
    private AuthClient authClient;

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
     * Update user data
     * Some data is stored in Keycloak, this method checks if keycloak info is being updated in order to do the request
     * 
     * @param id
     * @param email
     * @param name
     * @param lastName
     * @param password
     * @param phone
     */
    public OperationResult<String> updateProfile(String email, String name, String lastName, String password, String phone) {
        
        try{
            // Get KC + Auth data
            Map<String, Object> getKCDataResponse = authClient.getKCData();

            // Get profile data
            Long user_id = Long.parseLong(getKCDataResponse.get("user_id").toString());
            User user = uRepository.findById(user_id).orElseThrow(() -> new RuntimeException("User not found in database"));

            log.info(user.toString());

            // @TODO Update


            return OperationResult.ok("User updated");
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
        
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
