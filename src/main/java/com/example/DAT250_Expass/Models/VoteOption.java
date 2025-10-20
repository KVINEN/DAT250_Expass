package com.example.DAT250_Expass.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "voting_options")
public class VoteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String caption;
    private Integer presentationOrder;

    @ManyToOne
    @JoinColumn(name = "poll_id")
    @JsonIgnore
    private Poll poll;

    public VoteOption() {
    }

    public VoteOption(Integer id, String caption, Integer presentationOrder) {
        this.id = id;
        this.caption = caption;
        this.presentationOrder = presentationOrder;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Integer getPresentationOrder() {
        return presentationOrder;
    }

    public void setPresentationOrder(Integer presentationOrder) {
        this.presentationOrder = presentationOrder;
    }
}
