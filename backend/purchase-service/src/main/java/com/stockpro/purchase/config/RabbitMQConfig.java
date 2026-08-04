package com.stockpro.purchase.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE        = "stockpro.alerts";
    public static final String QUEUE_OVERDUE   = "alert.po.overdue";
    public static final String RK_OVERDUE      = "alert.po.overdue";

    @Bean
    public TopicExchange alertExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue poOverdueQueue() {
        return QueueBuilder.durable(QUEUE_OVERDUE).build();
    }

    @Bean
    public Binding poOverdueBinding(Queue poOverdueQueue, TopicExchange alertExchange) {
        return BindingBuilder.bind(poOverdueQueue)
                .to(alertExchange)
                .with(RK_OVERDUE);
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