package com.springneobank.user.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // --- Common Exchanges ---
    public static final String EXCHANGE = "user.exchange";
    public static final String REGISTER_DLX_EXCHANGE = "user.exchange.dlx";

    // --- User Register ---
    public static final String REGISTER_QUEUE = "user.registered.queue";
    public static final String REGISTER_ROUTING_KEY = "user.registered";

    // --- User Register DLQ ---
    public static final String REGISTER_DLQ_QUEUE = "user.registered.queue.dlq";
    public static final String REGISTER_DLQ_ROUTING_KEY = "user.registered.dlq";

    // --- User Unregister ---
    public static final String UNREGISTER_QUEUE = "user.unregistered.queue";
    public static final String UNREGISTER_ROUTING_KEY = "user.unregistered";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }


    // --- Exchanges ---
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange userDeadLetterExchange() {
        return new TopicExchange(REGISTER_DLX_EXCHANGE);
    }


    // --- Queues ---
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(REGISTER_QUEUE)
                .withArgument("x-dead-letter-exchange", REGISTER_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", REGISTER_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue userRegisteredDlq() {
        return QueueBuilder.durable(REGISTER_DLQ_QUEUE)
                .withArgument("x-message-ttl", 3600000) // clean old messages: 1 hour
                .build();
    }

    @Bean
    public Queue userUnregisteredQueue() {
        return QueueBuilder.durable(UNREGISTER_QUEUE)
                .build();
    }

    // --- Bindings ---
    @Bean
    public Binding userRegisteredBinding(@Qualifier("userRegisteredQueue") Queue userRegisteredQueue, @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(userExchange())
                .with(REGISTER_ROUTING_KEY);
    }

     @Bean
    public Binding userRegisteredDlqBinding(@Qualifier("userRegisteredDlq") Queue userRegisteredDlq, @Qualifier("userDeadLetterExchange") TopicExchange userDeadLetterExchange) {
        return BindingBuilder.bind(userRegisteredDlq)
                .to(userDeadLetterExchange())
                .with(REGISTER_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding userUnregisteredBinding(@Qualifier("userUnregisteredQueue") Queue queue, @Qualifier("userExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(UNREGISTER_ROUTING_KEY);
    }

    // --- JSON Converter ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}