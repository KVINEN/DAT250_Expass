package com.example.DAT250_Expass.Models;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String question;

    private Instant publishedAt;

    private Instant validUntil;

    private Boolean isPrivate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VoteOption> options = new ArrayList<>();

    private Boolean limitToOneVote;

    public Poll() {
        this.options = new ArrayList<>();
    }

    public Poll(Integer id, String question, Instant publishedAt, Instant validUntil, Boolean isPrivate, User user, List<VoteOption> voteOptions, Boolean limitToOneVote) {
        this();
        this.id = id;
        this.question = question;
        this.publishedAt = publishedAt;
        this.validUntil = validUntil;
        this.isPrivate = isPrivate;
        this.limitToOneVote = limitToOneVote;
        this.createdBy = user;
        if (voteOptions != null) {
            for (VoteOption option : voteOptions) {
                this.addVoteOption(option.getCaption());
            }
        }
    }

    public VoteOption addVoteOption(String caption) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        VoteOption newOption = new VoteOption();
        newOption.setCaption(caption);
        newOption.setPresentationOrder(this.options.size());
        newOption.setPoll(this);
        this.options.add(newOption);
        return newOption;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<VoteOption> getOptions() {
        return options;
    }

    public void setOptions(List<VoteOption> options) {
        this.options = options;
    }

    public Boolean getLimitToOneVote() {
        return limitToOneVote;
    }

    public void setLimitToOneVote(Boolean limitToOneVote) {
        this.limitToOneVote = limitToOneVote;
    }
}
