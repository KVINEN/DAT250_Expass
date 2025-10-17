package com.example.DAT250_Expass;

import com.example.DAT250_Expass.Models.Poll;
import com.example.DAT250_Expass.Models.User;
import com.example.DAT250_Expass.Models.Vote;
import com.example.DAT250_Expass.Models.VoteOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VoteControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testUserVotesAndChangesVote() {
        String usersUrl = "http://localhost:" + randomServerPort + "/api/users";
        User user1 = new User();
        user1.setUsername("Bob");
        user1.setEmail("bob@gmail.com");
        user1.setPassword("pass123");
        ResponseEntity<User> user1Response = restTemplate.postForEntity(usersUrl, user1, User.class);
        User createdUser1 = user1Response.getBody();

        User user2 = new User();
        user2.setUsername("Tom");
        user2.setEmail("tom@gmail.com");
        user2.setPassword("pass456");
        ResponseEntity<User> user2Response = restTemplate.postForEntity(usersUrl, user2, User.class);
        User createdUser2 = user2Response.getBody();

        String pollsUrl = "http://localhost:" + randomServerPort + "/api/polls";
        VoteOption optionYes = new VoteOption();
        optionYes.setCaption("Yes");
        optionYes.setPresentationOrder(1);

        VoteOption optionNo = new VoteOption();
        optionNo.setCaption("No");
        optionNo.setPresentationOrder(2);

        Poll poll = new Poll();
        poll.setQuestion("Does pineapple belong on pizza?");
        poll.setPublishedAt(Instant.now());
        poll.setValidUntil(Instant.now().plusSeconds(3600));
        poll.setPublic(false);
        poll.setUser(createdUser1);
        poll.setVoteOption(List.of(optionYes, optionNo));
        ResponseEntity<Poll> pollResponse = restTemplate.postForEntity(pollsUrl, poll, Poll.class);
        Poll createdPoll = pollResponse.getBody();
        assertThat(createdPoll).isNotNull();

        String votesUrl = pollsUrl + "/" + createdPoll.getId() + "/votes";

        Vote firstVote = new Vote();
        firstVote.setUser(createdUser2);
        firstVote.setPublishedAt(Instant.now());
        firstVote.setVotingOption(createdPoll.getVoteOption().get(0));

        ResponseEntity<Vote> firstVoteResponse = restTemplate.postForEntity(votesUrl, new HttpEntity<>(firstVote), Vote.class);
        assertThat(firstVoteResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(firstVoteResponse.getBody().getVotingOption().getCaption()).isEqualTo("Yes");

        Integer createdVoteId = firstVoteResponse.getBody().getVoteId();

        String updateVoteUrl = "http://localhost:" + randomServerPort + "/api/votes/" + createdVoteId;
        Vote updatedVote = new Vote();
        updatedVote.setUser(createdUser2);
        updatedVote.setPublishedAt(Instant.now());
        updatedVote.setVotingOption(createdPoll.getVoteOption().get(1));

        ResponseEntity<Vote> updatedVoteResponse = restTemplate.exchange(updateVoteUrl, HttpMethod.PUT, new HttpEntity<>(updatedVote), Vote.class);
        assertThat(updatedVoteResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(updatedVoteResponse.getBody().getVotingOption().getCaption()).isEqualTo("No");

        ResponseEntity<Vote[]> listResponse = restTemplate.getForEntity(votesUrl, Vote[].class);
        assertThat(listResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody()).hasSize(1);
        assertThat(listResponse.getBody()[0].getUser().getUsername()).isEqualTo("Tom");
        assertThat(listResponse.getBody()[0].getVotingOption().getCaption()).isEqualTo("No");
    }
}