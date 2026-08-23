package com.example.ticket.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.queue.ticket}")
    private String ticketQueue;
    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;
    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchangeName;

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(paymentExchangeName);
    }

    @Bean
    public Queue ticketQueue() {
        return new Queue(ticketQueue, true);
    }

    @Bean
    public Binding ticketBinding() {
        return BindingBuilder.bind(ticketQueue())
                .to(eventExchange())
                .with(createdRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}