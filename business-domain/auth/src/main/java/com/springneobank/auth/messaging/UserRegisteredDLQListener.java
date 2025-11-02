package com.springneobank.auth.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.springneobank.auth.service.KeycloakRestService;
import com.springneobank.auth.service.KCUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredDLQListener {

    @Autowired
    private KCUserService uService;

    @Autowired
    private KeycloakRestService kcService;

    @RabbitListener(queues = RabbitConfig.DLQ_QUEUE)
    public void handleUserRegisteredDLQ(UserRegisteredEvent event) {

        log.info("Event received UserRegisteredDLQ: {}", event);

        boolean kcSuccess = false;

        // Delete from Keycloak
        try{
            kcSuccess = kcService.deleteUser(event.getKeycloakId());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Keycloak user not found, ignoring DLQ");
        } catch (Exception e) {
            log.error("Error processing DLQ UserRegisteredDLQListener", e);
            throw e;
        }
        
        // Delete from database
        if(kcSuccess) {
            uService.removeUserByKeycloakID(event.getKeycloakId());

            log.info("DQL Success: Deleted from KC y DDBB");
        }
    }
}
