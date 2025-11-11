package com.springneobank.auth.messaging.UserUnregistered;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.springneobank.auth.messaging.RabbitConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserUnregisteredPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(UserUnregisteredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.UNREGISTER_ROUTING_KEY,
                event
        );
    }
}
