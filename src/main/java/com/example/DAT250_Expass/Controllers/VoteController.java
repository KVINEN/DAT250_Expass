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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class VoteController {

    @Autowired
    private PollManager pollManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @PostMapping("/api/polls/{pollId}/votes")
    public ResponseEntity<?> castVote(@PathVariable Integer pollId, @RequestBody Vote voteRequest) {
        try {
            Poll parentPoll = pollManager.getPollById(pollId);
            if (parentPoll == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Poll not found"));
            }

            // Get the ID of the chosen option from the request
            Integer requestedOptionId = Optional.ofNullable(voteRequest.getVotesOn())
                    .map(VoteOption::getId)
                    .orElse(null);

            if (requestedOptionId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vote option ID is required"));
            }

            // Find the actual VoteOption entity from the parent poll
            VoteOption chosenOption = parentPoll.getOptions().stream()
                    .filter(opt -> opt.getId().equals(requestedOptionId))
                    .findFirst()
                    .orElse(null);

            if (chosenOption == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid vote option ID for this poll"));
            }

            // Create a new Vote object using the fetched entities
            Vote voteToSave = new Vote();
            voteToSave.setUser(voteRequest.getUser()); // Assuming user details are passed correctly
            voteToSave.setPublishedAt(voteRequest.getPublishedAt() != null ? voteRequest.getPublishedAt() : Instant.now());
            voteToSave.setVotesOn(chosenOption); // Use the validated VoteOption linked to the Poll

            // Add the vote using the manager
            Vote newVote = pollManager.addVote(voteToSave);

            // Publish event (no changes needed here if RabbitMQ is now configured correctly)
            String exchangeName = PollController.POLL_EXCHANGE_PREFIX + pollId;
            String routingKey = "vote.cast";
            try {
                String voteJson = objectMapper.writeValueAsString(newVote);
                rabbitTemplate.convertAndSend(exchangeName, routingKey, voteJson);
                System.out.println("Published vote event to exchange: " + exchangeName + " write key: " + routingKey);
            } catch (Exception e) {
                System.err.println("Failed to serialize or publish vote event: " + e.getMessage());
                // Decide if this should be a critical error - perhaps just log it
            }

            return new ResponseEntity<>(newVote, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Return specific errors from PollManager (like already voted)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Catch unexpected errors
            System.err.println("Error casting vote: " + e.getMessage());
            // Return a JSON error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An internal error occurred: " + e.getMessage()));
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