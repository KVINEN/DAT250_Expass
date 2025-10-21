package com.example.DAT250_Expass.Models;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class PollManager {

    private final AtomicInteger userIdCounter = new AtomicInteger();
    private final AtomicInteger pollIdCounter = new AtomicInteger();
    private final AtomicInteger voteIdCounter = new AtomicInteger();

    private final HashMap<Integer, User> users = new HashMap<>();
    private final HashMap<Integer, Poll> polls = new HashMap<>();
    private final HashMap<Integer, Vote> votes = new HashMap<>();

    public User addUser(User user) {
        user.setId(userIdCounter.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Poll addPoll(Poll poll) {
        poll.setId(pollIdCounter.incrementAndGet());

        List<VoteOption> options = poll.getOptions();

        if (options == null) {
            options = new ArrayList<>();
            poll.setOptions(options);
        }

        int optionCounter = 1;
        for (VoteOption option : options) {
            option.setPoll(poll);
            option.setId(optionCounter++);
            option.setPresentationOrder(optionCounter - 1);
        }

        polls.put(poll.getId(), poll);
        return poll;
    }

    public Poll getPollById(Integer pollId) {
        return polls.get(pollId);
    }

    public List<Poll> getAllPublicPolls() {
        return polls.values().stream()
                .filter(poll -> !poll.getIsPrivate())
                .collect(Collectors.toList());
    }

    public Vote addVote(Vote vote) {
        Poll poll = vote.getVotesOn().getPoll();
        User user = vote.getUser();
        Instant now = Instant.now();

        if(!(now.isAfter(poll.getPublishedAt()) && now.isBefore(poll.getValidUntil()))) {
            throw new IllegalArgumentException("Vote is outside the valid time window");
        }

        if (poll.getIsPrivate() && poll.getLimitToOneVote()) {
            for (Vote existingVote : votes.values()) {
                if (existingVote.getUser().equals(user) && existingVote.getVotesOn().getPoll().getId().equals(poll.getId())) {
                    throw new IllegalArgumentException("User has already voted on this private poll");
                }
            }
        }

        vote.setVoteId(voteIdCounter.incrementAndGet());
        votes.put(vote.getVoteId(), vote);
        return vote;
    }

    public Vote updateVote(Integer voteId, Vote updatedVote) {
        if (votes.containsKey(voteId)) {
            votes.put(voteId, updatedVote);
            return updatedVote;
        }
        return null;
    }

    public List<Vote> getVotesForPoll(Integer pollId) {
        return votes.values().stream()
                .filter(vote -> vote.getVotesOn().getPoll().getId().equals(pollId))
                .collect(Collectors.toList());
    }

    public HashMap<Integer, Vote> getVotes() {
        return votes;
    }

    public void deletePoll(Integer pollId) {
        List<Vote> votesToRemove = getVotesForPoll(pollId);
        for (Vote vote : votesToRemove) {
            votes.remove(vote.getVoteId());
        }

        polls.remove(pollId);
    }
}