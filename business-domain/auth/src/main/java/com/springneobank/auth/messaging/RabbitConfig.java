package com.springneobank.auth.messaging;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // --- Common Exchanges ---
    public static final String EXCHANGE = "user.exchange";
    public static final String DLX_EXCHANGE = "user.exchange.dlx";

    // --- Producer
    public static final String ROUTING_KEY = "user.registered";
    public static final String UNREGISTER_ROUTING_KEY = "user.unregistered";

    // -- Listener
    public static final String DLQ_QUEUE = "user.registered.queue.dlq";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange userDeadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
