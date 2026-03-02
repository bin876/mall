package com.hbin.mall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SALE_COUNT_EXCHANGE = "sale.count.exchange";
    public static final String SALE_COUNT_QUEUE = "sale.count.queue";
    public static final String SALE_COUNT_ROUTING_KEY = "sale.count.update";

    @Bean
    public DirectExchange saleCountExchange() {
        return new DirectExchange(SALE_COUNT_EXCHANGE);
    }

    @Bean
    public Queue saleCountQueue() {
        return QueueBuilder.durable(SALE_COUNT_QUEUE)
            .build();
    }

    @Bean
    public Binding saleCountBinding() {
        return BindingBuilder.bind(saleCountQueue())
            .to(saleCountExchange())
            .with(SALE_COUNT_ROUTING_KEY);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter messageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }
}