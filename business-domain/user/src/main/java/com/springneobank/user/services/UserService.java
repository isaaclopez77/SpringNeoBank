package com.springneobank.user.services;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @param phone
     */
    public OperationResult<String> updateProfile(String email, String name, String lastName, String phone) {
        
        try{
            // Get KC + Auth data
            Map<String, Object> getKCDataResponse = authClient.getKCData();

            // Get KC  data
            Long user_id = Long.parseLong(getKCDataResponse.get("user_id").toString());
            User user = uRepository.findById(user_id).orElseThrow(() -> new RuntimeException("User not found in database"));

            // Check if user keycloak data update is required
            boolean kcDataUpdate = !getKCDataResponse.get("given_name").equals(name) || // @TODO don't use kc fieldNames, only in Auth Microservice
                !getKCDataResponse.get("family_name").equals(lastName) ||
                !getKCDataResponse.get("email").equals(email);
            
            if(kcDataUpdate) {
                // Update KC Data
                authClient.updateKCData(email, name, lastName);
            }
            
            // Update profile data if needed
            if(!user.getPhone().equals(phone)) {
                user.setPhone(phone);
                uRepository.save(user);
            } else if(!kcDataUpdate) {
                return OperationResult.ok("Nothing to update");
            }

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
