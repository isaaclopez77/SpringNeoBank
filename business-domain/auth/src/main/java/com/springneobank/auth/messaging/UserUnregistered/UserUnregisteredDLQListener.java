package com.springneobank.auth.messaging.UserUnregistered;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.springneobank.auth.service.KeycloakService;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.RabbitConfig;
import com.springneobank.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUnregisteredDLQListener {

    @Autowired
    private AuthService KCUService;

    @Autowired
    private KeycloakService kcService;

    @RabbitListener(queues = RabbitConfig.UNREGISTER_DLQ_QUEUE)
    public void handleUserRegisteredDLQ(UserUnregisteredEvent event) {

        log.info("Event received UserUnregisteredDLQ: {}", event);

        boolean kcSuccess = false;

        // Get KC user
        KCUser user = KCUService.getUserByID(event.getUserId());

        // Delete from KC
        try{
            kcService.activateUser(user.getKeycloakID());
            kcSuccess = true;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Keycloak user not found, ignoring DLQ");
        } catch (Exception e) {
            log.error("Error processing DLQ UserUnregisteredDLQListener", e);
            throw e;
        }
        
        // Delete from database
        if(kcSuccess) {
            KCUService.activateUser(user);

            log.info("DQL Success: Activated in KC y DDBB");
        }
    }
}
