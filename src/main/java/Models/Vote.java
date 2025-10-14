package Models;

import java.time.Instant;

public class Vote {

    private  Integer voteId;
    private User user;
    private Instant publishedAt;
    private VoteOption votingOption;

    public Vote() {
    }

    public Vote(Integer voteId, User user, Instant publishedAt, VoteOption votingOption) {
        this.voteId = voteId;
        this.user = user;
        this.publishedAt = publishedAt;
        this.votingOption = votingOption;
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

    public VoteOption getVotingOption() {
        return votingOption;
    }

    public void setVotingOption(VoteOption votingOption) {
        this.votingOption = votingOption;
    }
}
