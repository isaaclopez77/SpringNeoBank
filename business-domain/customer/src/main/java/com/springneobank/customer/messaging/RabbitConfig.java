package com.springneobank.customer.messaging;

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

    public static final String EXCHANGE = "user.exchange";
    public static final String QUEUE = "user.registered.queue";
    public static final String ROUTING_KEY = "user.registered";

    public static final String DLX_EXCHANGE = "user.exchange.dlx";
    public static final String DLQ_QUEUE = "user.registered.queue.dlq";
    public static final String DLQ_ROUTING_KEY = "user.registered.dlq";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }


    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange userDeadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue userRegisteredDlq() {
        // Añadir TTL si quieres limpiar automáticamente los mensajes antiguos
        return QueueBuilder.durable(DLQ_QUEUE)
                //.withArgument("x-message-ttl", 3600000) // opcional: 1 hora
                .build();
    }

    @Bean
    public Binding userRegisteredBinding(@Qualifier("userRegisteredQueue") Queue userRegisteredQueue, @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(userExchange())
                .with(ROUTING_KEY);
    }

     @Bean
    public Binding userRegisteredDlqBinding(@Qualifier("userRegisteredDlq") Queue userRegisteredDlq, @Qualifier("userDeadLetterExchange") TopicExchange userDeadLetterExchange) {
        return BindingBuilder.bind(userRegisteredDlq)
                .to(userDeadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}