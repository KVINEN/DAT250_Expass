package com.example.DAT250_Expass.Messaging;

import com.example.DAT250_Expass.Config.RabbitMQConfig;
import com.example.DAT250_Expass.Models.PollManager;
import com.example.DAT250_Expass.Models.Vote;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteEventListener {

    @Autowired
    private PollManager pollManager;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = RabbitMQConfig.VOTE_QUEUE_NAME)
    public void handleVoteMessage(String voteJson) {
        System.out.println("Received vote message: " + voteJson);
        try {
            Vote vote = objectMapper.readValue(voteJson, Vote.class);

            if (vote != null && vote.getVotesOn() != null && vote.getVotesOn().getId() != null && vote.getUser() != null && vote.getUser().getId().equals(vote.getVotesOn().getId())) {
                System.out.println("Processing vote from queue for user: " + vote.getUser().getUsername() + " on option: " + vote.getVotesOn().getId());

                pollManager.recordVoteFromQueue(vote);
            } else {
                System.err.println("Received invalid vote message format.");
            }
        } catch ( Exception e ) {
            System.err.println("Failed to process vote message: " + e.getMessage());
        }
    }
}
