package com.springneobank.user.messaging;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    //private final CustomerRepository customerRepository;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event, Channel channel, Message message) throws IOException {
        //log.info("Recibido evento UserRegistered: {}", event);

        try{
            /*Customer customer = new Customer(
                event.getUserId(),
                event.getFirstName(),
                event.getLastName(),
                event.getEmail(),
                event.getPhone(),
                LocalDateTime.now()
            );

            customerRepository.save(customer);*/

            //throw new Exception("simulating exception");

            //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            //log.info("Procesado evento handleUserRegistered");
        } catch(Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            //log.error("Error procesando evento, enviado a DLQ: {}", e.getMessage());
        }

        
        //log.info("Customer creado: {}", customer.getId());
    }
}
