// RabbitMqConfig.java
package com.hbin.mall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SECKILL_ORDER_QUEUE = "seckill-order-queue";
    public static final String SECKILL_ORDER_EXCHANGE = "seckill-order-exchange";
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order";

    /**
     * 配置 JSON 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder
            .durable(SECKILL_ORDER_QUEUE)
            .build();
    }

    @Bean
    public DirectExchange seckillOrderExchange() {
        return ExchangeBuilder
            .directExchange(SECKILL_ORDER_EXCHANGE)
            .durable(true)
            .build();
    }

    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder
            .bind(seckillOrderQueue())
            .to(seckillOrderExchange())
            .with(SECKILL_ORDER_ROUTING_KEY);
    }
}