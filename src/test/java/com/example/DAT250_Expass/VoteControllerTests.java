package com.example.DAT250_Expass;

import com.example.DAT250_Expass.Models.Poll;
import com.example.DAT250_Expass.Models.User;
import com.example.DAT250_Expass.Models.Vote;
import com.example.DAT250_Expass.Models.VoteOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class VoteControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    private User user1;
    private User user2;
    private Poll poll;
    private VoteOption  optionYes;
    private VoteOption optionNo;

    @BeforeEach
    public void setup() {
        final String userUrl = "http://localhost:" + randomServerPort + "/api/users";
        user1 = new User(1, "Bob", "bob@gmail.com", "pass123");
        user2 = new User(2, "Tom", "tom@gmail.com", "pass456");
        restTemplate.postForLocation(userUrl, user1);
        restTemplate.postForLocation(userUrl, user2);

        final String pollUrl = "http://localhost:" + randomServerPort + "/api/polls";
        optionYes = new VoteOption(1, "Yes", 1);
        optionNo = new VoteOption(2, "No", 2);
        List<VoteOption> options = List.of(optionYes, optionNo);
        poll = new Poll(1, "Does pineapple belong on pizza?", Instant.now(), Instant.now().plusSeconds(3600), false, user1, options, false);

        ResponseEntity<Poll> pollResponse = restTemplate.postForEntity(pollUrl, poll, Poll.class);
        poll = pollResponse.getBody();
    }

    @Test
    public void testUserVotesAndChangesVote() throws URISyntaxException {
        final String votesUrl = "http://localhost:" + randomServerPort + "/api/polls/" + poll.getId() + "/votes";
        Vote firstVote = new Vote(1, user2, Instant.now(), optionYes);
        HttpEntity<Vote> firstVoteRequest = new HttpEntity<>(firstVote);
        ResponseEntity<Vote> firstVoteResponse = restTemplate.postForEntity(votesUrl, firstVoteRequest, Vote.class);

        assertThat(firstVoteResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(firstVoteResponse.getBody().getVotingOption().getCaption()).isEqualTo("Yes");

        Integer createdVoteId = firstVoteResponse.getBody().getVoteId();

        final String updateVoteUrl = "http://localhost:" + randomServerPort + "/api/votes/" + createdVoteId;
        Vote updatedVote = new Vote(createdVoteId, user2, Instant.now(), optionNo);
        HttpEntity<Vote> updatedVoteRequest = new HttpEntity<>(updatedVote);

        ResponseEntity<Vote> updatedVoteResponse = restTemplate.exchange(updateVoteUrl, HttpMethod.PUT, updatedVoteRequest, Vote.class);

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
