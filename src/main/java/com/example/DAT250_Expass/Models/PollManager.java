package com.example.DAT250_Expass.Models;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PollManager {

    private final HashMap<Integer, User> users = new HashMap<>();
    private final HashMap<Integer, Poll> polls = new HashMap<>();
    private final HashMap<Integer, Vote> votes = new HashMap<>();

    public User addUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Poll addPoll(Poll poll) {
        polls.put(poll.getId(), poll);
        return poll;
    }

    public Poll getPollById(Integer pollId) {
        return polls.get(pollId);
    }

    public List<Poll> getAllPolls() {
        return new ArrayList<>(polls.values());
    }

    public Vote addVote(Vote vote) {
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
                .filter(vote -> vote.getVotingOption().getPoll().getId().equals(pollId))
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