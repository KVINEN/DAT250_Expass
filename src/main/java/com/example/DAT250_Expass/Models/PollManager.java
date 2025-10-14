package com.example.DAT250_Expass.Models;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PollManager {

    private HashMap<Integer, User> users = new HashMap<>();
    private HashMap<Integer, Poll> polls = new HashMap<>();

    public PollManager() {
    }

    public User addUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public User getUser(int id) {
        return users.get(id);
    }

    public Poll addPoll(Poll poll) {
        polls.put(poll.getId(), poll);
        return poll;
    }

    public Poll getPoll(int id) {
        return polls.get(id);
    }

}
