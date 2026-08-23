package com.example.payment.config;
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
    @Value("${rabbitmq.queue.payment}")
    private String paymentQueue;
    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;

    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchangeName;
    @Value("${rabbitmq.queue.purchase}")
    private String purchaseQueue;
    @Value("${rabbitmq.routing.purchased}")
    private String purchasedRoutingKey;

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(paymentExchangeName);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(paymentQueue, true);
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(eventExchange())
                .with(createdRoutingKey);
    }

    @Bean
    public Queue purchaseQueue() {
        return new Queue(purchaseQueue, true);
    }

    @Bean
    public Binding purchaseBinding() {
        return BindingBuilder.bind(purchaseQueue())
                .to(paymentExchange())
                .with(purchasedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}