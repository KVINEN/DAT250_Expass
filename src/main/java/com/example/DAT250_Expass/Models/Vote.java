package com.example.DAT250_Expass.Models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer voteId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Instant publishedAt;

    @ManyToOne
    @JoinColumn(name = "voting_option_id")
    private VoteOption votesOn;

    public Vote() {
    }

    public Vote(Integer voteId, User user, Instant publishedAt, VoteOption votesOn) {
        this.voteId = voteId;
        this.user = user;
        this.publishedAt = publishedAt;
        this.votesOn = votesOn;
    }

    public Integer getVoteId() {
        return voteId;
    }

    public void setVoteId(Integer voteId) {
        this.voteId = voteId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public VoteOption getVotesOn() {
        return votesOn;
    }

    public void setVotesOn(VoteOption votingOption) {
        this.votesOn = votesOn;
    }

    @Transient
    @Deprecated
    public VoteOption getVotingOption() {
        return votesOn;
    }

    @Transient
    @Deprecated
    public void setVotingOption(VoteOption votingOption) {
        this.votesOn = votingOption;
    }
}
