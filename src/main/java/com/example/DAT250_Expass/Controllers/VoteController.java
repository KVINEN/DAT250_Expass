package com.example.DAT250_Expass.Controllers;

import com.example.DAT250_Expass.Models.Poll;
import com.example.DAT250_Expass.Models.PollManager;
import com.example.DAT250_Expass.Models.Vote;
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

    @PostMapping("/api/polls/{pollId}/votes")
    public ResponseEntity<Vote> castVote(@PathVariable Integer pollId, @RequestBody Vote vote) {
        try {
            Poll parentPoll = pollManager.getPollById(pollId);
            if (parentPoll == null) {
                return ResponseEntity.notFound().build();
            }
            vote.getVotingOption().setPoll(parentPoll);
            Vote newVote = pollManager.addVote(vote);
            return new ResponseEntity<>(newVote, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/api/votes/{voteId}")
    public ResponseEntity<Vote> changeVote(@PathVariable Integer voteId, @RequestBody Vote vote) {
        Vote existingVote = pollManager.getVotes().get(voteId);
        if (existingVote != null) {
            Poll parentPoll = existingVote.getVotingOption().getPoll();
            vote.getVotingOption().setPoll(parentPoll);
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