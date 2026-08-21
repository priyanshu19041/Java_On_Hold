package com.omnicharge.recharge.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String RECHARGE_QUEUE = "recharge.queue";
    public static final String RECHARGE_EXCHANGE = "recharge.exchange";
    public static final String RECHARGE_ROUTING_KEY = "recharge.process";

    public static final String RECHARGE_DLQ = "recharge.dlq";
    public static final String RECHARGE_DLX = "recharge.dlx";
    public static final String RECHARGE_DLQ_ROUTING_KEY = "recharge.dead";

    @Bean
    public DirectExchange rechargeExchange() {
        return new DirectExchange(RECHARGE_EXCHANGE);
    }

    @Bean
    public Queue rechargeQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RECHARGE_DLX);
        args.put("x-dead-letter-routing-key", RECHARGE_DLQ_ROUTING_KEY);
        return QueueBuilder.durable(RECHARGE_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding rechargeBinding(Queue rechargeQueue, DirectExchange rechargeExchange) {
        return BindingBuilder.bind(rechargeQueue).to(rechargeExchange).with(RECHARGE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange rechargeDeadLetterExchange() {
        return new DirectExchange(RECHARGE_DLX);
    }

    @Bean
    public Queue rechargeDeadLetterQueue() {
        return QueueBuilder.durable(RECHARGE_DLQ).build();
    }

    @Bean
    public Binding rechargeDLQBinding(Queue rechargeDeadLetterQueue, DirectExchange rechargeDeadLetterExchange) {
        return BindingBuilder.bind(rechargeDeadLetterQueue).to(rechargeDeadLetterExchange).with(RECHARGE_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
