package com.springneobank.user.messaging;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.springneobank.user.entities.User;
import com.springneobank.user.repositories.UserRepository;
import com.springneobank.user.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    private final UserService uService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event, Channel channel, Message message) throws IOException {
        log.info("Recibido evento UserRegistered: {}", event);

        try{
            //throw new Exception("simulating exception");

            uService.createUser(event.getUserId(), event.getPhone(), true);

            log.info("Procesado evento handleUserRegistered");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch(Exception e) {
            log.error("Error procesando evento, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
