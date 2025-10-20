package com.example.DAT250_Expass.Models;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String email;

    private String password;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Poll> createdPolls = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes = new LinkedHashSet<>();

    public User() {
        this.createdPolls = new HashSet<>();
        this.votes = new HashSet<>();
    }

    public User(Integer id, String username, String email, String password) {
        this();
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
    }

    public Poll createPoll(String question) {
        Poll newPoll = new Poll();
        newPoll.setQuestion(question);
        newPoll.setCreatedBy(this);
        newPoll.setPublishedAt(Instant.now());
        newPoll.setIsPrivate(false);
        newPoll.setLimitToOneVote(false);
        this.createdPolls.add(newPoll);
        return newPoll;
    }

    public Vote voteFor(VoteOption option) {
        Vote newVote = new Vote();
        newVote.setUser(this);
        newVote.setVotesOn(option);
        newVote.setPublishedAt(Instant.now());
        this.votes.add(newVote);
        return newVote;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Poll> getCreatedPolls() {
        return createdPolls;
    }

    public void setCreatedPolls(Set<Poll> createdPolls) {
        this.createdPolls = createdPolls;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }
}
