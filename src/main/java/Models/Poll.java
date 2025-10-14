package Models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Poll {

    private Integer id;

    private String question;

    private Instant publishedAt;

    private Instant validUntil;

    private Boolean isPublic;

    private User user;

    private List<VoteOption> voteOption;

    public Poll() {
    }

    public Poll(Integer id, String question, Instant publishedAt, Instant validUntil, Boolean isPublic, User user, List<VoteOption> voteOption) {
        this.id = id;
        this.question = question;
        this.publishedAt = publishedAt;
        this.validUntil = validUntil;
        this.isPublic = isPublic;
        this.user = user;
        this.voteOption = voteOption;
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

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<VoteOption> getVoteOption() {
        return voteOption;
    }

    public void setVoteOption(List<VoteOption> voteOption) {
        this.voteOption = voteOption;
    }
}
