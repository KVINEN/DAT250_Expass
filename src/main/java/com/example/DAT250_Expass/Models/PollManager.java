package com.example.DAT250_Expass.Models;

import io.valkey.JedisPooled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final JedisPooled jedis;
    private final int CASH_TTL_SECONDS = 300;

    public PollManager() {
        String valkeyHost = System.getProperty("valkey.host");
        if (valkeyHost == null || valkeyHost.isEmpty()) {
            valkeyHost = System.getenv("VALKEY_HOST");
        }
        if (valkeyHost == null || valkeyHost.isEmpty()) {
            valkeyHost = "localhost";
        }
        this.jedis = new JedisPooled(valkeyHost, 6379);
        System.out.println("Valkey connection established " + valkeyHost + ":6379" + this.jedis.ping());
    }

    public Map<Integer, Long> getPollVoteCounters(Integer pollId) {
        String cacheKey = "poll:cache:" + pollId;

        Map<String, String> cacheCounterStr = jedis.hgetAll(cacheKey);

        if (cacheCounterStr != null && !cacheCounterStr.isEmpty()) {
            System.out.println("Cache HIT for poll: " + pollId);
            jedis.expire(cacheKey, CASH_TTL_SECONDS);
            return cacheCounterStr.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry ->  Integer.parseInt(entry.getKey()),
                            entry -> Long.parseLong(entry.getValue())
                    ));
        } else {
            System.out.println("Cache MISS for poll: " + pollId);
            Poll poll = getPollById(pollId);
            if (poll == null) {
                return new HashMap<>();
            }
            Map<Integer, Long> actualCounts = getVotesForPoll(pollId).stream()
                    .collect(Collectors.groupingBy(vote -> vote.getVotesOn().getId(), Collectors.counting()));

            for (VoteOption option : poll.getOptions()) {
                actualCounts.putIfAbsent(option.getId(), 0L);
            }

            if (!actualCounts.isEmpty()) {
                Map<String, String> countsToCache = actualCounts.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> String.valueOf(entry.getValue())
                        ));
                jedis.hset(cacheKey, countsToCache);
                jedis.expire(cacheKey, CASH_TTL_SECONDS);
                System.out.println("Stored in cache for poll: " +  pollId);
            }
            return actualCounts;
        }
    }

    private void invalidatePollCache(Integer pollId) {
        String cacheKey = "poll:cache:" + pollId;
        jedis.del(cacheKey);
        System.out.println("Invalidated cache for poll: " + pollId);
    }

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

        invalidatePollCache(poll.getId());

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

        invalidatePollCache(pollId);
    }

    public void recordVoteFromQueue(Vote voteFromQueue) {
        Poll poll = getPollById(voteFromQueue.getVotesOn().getPoll().getId());
        User user = users.get(voteFromQueue.getUser().getId());
        VoteOption option = poll != null ? poll.getOptions().stream()
                .filter(opt -> opt.getId().equals(voteFromQueue.getVotesOn().getId()))
                .findFirst().orElse(null) : null;

        if (poll == null || user == null || option == null) {
            System.err.println("Cannot record vote from queue: Invalid poll, user, or option ID.");
            return;
        }

        Instant now = Instant.now();
        if(!(now.isAfter(poll.getPublishedAt()) && now.isBefore(poll.getValidUntil()))) {
            System.err.println("Vote from queue is outside the valid time window.");
            return;
        }

        if (poll.getIsPrivate() && poll.getLimitToOneVote()) {
            for (Vote existingVote : votes.values()) {
                if (existingVote.getUser().getId().equals(user.getId()) && existingVote.getVotesOn().getPoll().getId().equals(poll.getId())) {
                    System.err.println("User " + user.getId() + " has already voted on this private poll (from queue).");
                    return;
                }
            }
        }

        Vote internalVote = new Vote(
                voteIdCounter.incrementAndGet(),
                user,
                voteFromQueue.getPublishedAt(),
                option
        );
        votes.put(internalVote.getVoteId(), internalVote);
        System.out.println("Recorded vote from queue for poll " + poll.getId());

        invalidatePollCache(poll.getId());
    }
}