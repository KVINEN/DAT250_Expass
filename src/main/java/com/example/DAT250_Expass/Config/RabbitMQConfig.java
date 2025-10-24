package com.example.DAT250_Expass.Config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    public static final String VOTE_QUEUE_NAME = "poll.votes.queue";
    public static final String POLL_EXCHANGE_NAME = "poll.*";
    public static final String VOTE_ROUTING_KEY = "vote.#";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue voteQueue() {
        return new Queue(VOTE_QUEUE_NAME, true);
    }

    @Bean
    public Declarables topicBinding(RabbitAdmin rabbitAdmin, Queue voteQueue) {
        Binding binding = BindingBuilder.bind(voteQueue)
                .to(new TopicExchange(POLL_EXCHANGE_NAME, true,false))
                .with(VOTE_ROUTING_KEY);
        return new Declarables(voteQueue, binding);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
