package com.springneobank.auth.messaging.UserRegistered;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.springneobank.auth.messaging.RabbitConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRegisteredPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                event
        );
    }
}
