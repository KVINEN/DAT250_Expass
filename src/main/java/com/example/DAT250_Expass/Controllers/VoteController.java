package com.example.DAT250_Expass.Controllers;

import com.example.DAT250_Expass.Models.Poll;
import com.example.DAT250_Expass.Models.PollManager;
import com.example.DAT250_Expass.Models.Vote;
import com.example.DAT250_Expass.Models.VoteOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
public class VoteController {

    @Autowired
    private PollManager pollManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @PostMapping("/api/polls/{pollId}/votes")
    public ResponseEntity<?> castVote(@PathVariable Integer pollId, @RequestBody Vote vote) {
        try {
            Poll parentPoll = pollManager.getPollById(pollId);
            if (parentPoll == null) {
                return ResponseEntity.notFound().build();
            }
            if (vote.getVotesOn() == null || !vote.getVotesOn().getPoll().getId().equals(pollId)) {
                VoteOption requestedOption = parentPoll.getOptions().stream()
                        .filter(opt -> opt.getId().equals(vote.getVotesOn().getId()))
                        .findFirst()
                        .orElse(null);
                if (requestedOption == null) {
                    return ResponseEntity.badRequest().body("Invalid vote option ID for this poll");
                }
                vote.setVotesOn(requestedOption);
            } else {
                vote.getVotesOn().setPoll(parentPoll);
            }

            Vote newVote = pollManager.addVote(vote);

            String exchangeName = PollController.POLL_EXCHANGE_PREFIX + pollId;
            String routingKey = "vote.cast";

            try {
                String voteJson = objectMapper.writeValueAsString(newVote);
                rabbitTemplate.convertAndSend(exchangeName, routingKey, voteJson);
                System.out.println("Published vote event to exchange: " + exchangeName + " write key: " + routingKey);
            } catch (Exception e) {
                System.err.println("Failed to serialize or publish vote event: " + e.getMessage());
            }
            return new ResponseEntity<>(newVote, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error casting vote: " +  e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred");
        }
    }

    @PutMapping("/api/votes/{voteId}")
    public ResponseEntity<Vote> changeVote(@PathVariable Integer voteId, @RequestBody Vote vote) {
        Vote existingVote = pollManager.getVotes().get(voteId);
        if (existingVote != null) {
            Poll parentPoll = existingVote.getVotesOn().getPoll();
            vote.getVotesOn().setPoll(parentPoll);
        }

        Vote updatedVote = pollManager.updateVote(voteId, vote);
        if (updatedVote != null) {
            return ResponseEntity.ok(updatedVote);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/polls/{pollId}/votes")
    public ResponseEntity<List<Vote>> getVotesForPoll(@PathVariable Integer pollId) {
        List<Vote> pollVotes = pollManager.getVotesForPoll(pollId);
        return ResponseEntity.ok(pollVotes);
    }
}