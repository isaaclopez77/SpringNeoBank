package com.springneobank.auth.messaging.UserRegistered;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.springneobank.auth.service.KeycloakService;
import com.springneobank.auth.entities.KCUser;
import com.springneobank.auth.messaging.RabbitConfig;
import com.springneobank.auth.service.KCUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredDLQListener {

    @Autowired
    private KCUserService KCUService;

    @Autowired
    private KeycloakService kcService;

    @RabbitListener(queues = RabbitConfig.DLQ_QUEUE)
    public void handleUserRegisteredDLQ(UserRegisteredEvent event) {

        log.info("Event received UserRegisteredDLQ: {}", event);

        boolean kcSuccess = false;

        // Get KC user
        KCUser user = KCUService.getUserByID(event.getUserId());

        // Delete from KC
        try{
            kcSuccess = kcService.deleteUser(user.getKeycloakID());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Keycloak user not found, ignoring DLQ");
        } catch (Exception e) {
            log.error("Error processing DLQ UserRegisteredDLQListener", e);
            throw e;
        }
        
        // Delete from database
        if(kcSuccess) {
            KCUService.removeUser(user);

            log.info("DQL Success: Deleted from KC y DDBB");
        }
    }
}
