package com.springneobank.user.messaging.UserUnregistered;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.springneobank.user.messaging.RabbitConfig;
import com.springneobank.user.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUnregisteredListener {

    private final UserService uService;

    @RabbitListener(queues = RabbitConfig.UNREGISTER_QUEUE)
    public void handleUserUnregistered(UserUnregisteredEvent event, Channel channel, Message message) throws IOException {
        log.info("Event receibed UserUnregistered: {}", event);

        try {
            //throw new Exception("simulated");

            uService.deactivateUserByID(event.getUserId());

            log.info("User ID {} unregistered", event.getUserId());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Error processing event, sending DLQ: {}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
