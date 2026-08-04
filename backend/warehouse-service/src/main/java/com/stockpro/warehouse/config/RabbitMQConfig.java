package com.stockpro.warehouse.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE      = "stockpro.alerts";
    public static final String QUEUE_LOWSTOCK = "alert.lowstock";
    public static final String RK_LOWSTOCK    = "alert.lowstock";

    @Bean
    public TopicExchange alertExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue lowStockQueue() {
        return QueueBuilder.durable(QUEUE_LOWSTOCK).build();
    }

    @Bean
    public Binding lowStockBinding(Queue lowStockQueue, TopicExchange alertExchange) {
        return BindingBuilder.bind(lowStockQueue)
                .to(alertExchange)
                .with(RK_LOWSTOCK);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}