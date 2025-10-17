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
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PollControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testCreateAndListPoll() throws URISyntaxException {
        String usersUrl = "http://localhost:" + randomServerPort + "/api/users";
        User creator = new User();
        creator.setUsername("Bob");
        creator.setEmail("bob@gmail.com");
        creator.setPassword("pass123");
        ResponseEntity<User> userResponse = restTemplate.postForEntity(usersUrl, creator, User.class);
        User createdCreator = userResponse.getBody();

        String pollsUrl = "http://localhost:" + randomServerPort + "/api/polls";
        URI uri = new URI(pollsUrl);

        VoteOption option1 = new VoteOption();
        option1.setCaption("Yes");
        option1.setPresentationOrder(1);

        VoteOption option2 = new VoteOption();
        option2.setCaption("No");
        option2.setPresentationOrder(2);

        Poll newPoll = new Poll();
        newPoll.setQuestion("Does pineapple belong on pizza?");
        newPoll.setPublishedAt(Instant.now());
        newPoll.setValidUntil(Instant.now().plusSeconds(3600));
        newPoll.setPublic(false);
        newPoll.setUser(createdCreator);
        newPoll.setVoteOption(List.of(option1, option2));

        ResponseEntity<Poll> createPollResponse = restTemplate.postForEntity(uri, new HttpEntity<>(newPoll), Poll.class);
        assertThat(createPollResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(createPollResponse.getBody().getQuestion()).isEqualTo("Does pineapple belong on pizza?");

        ResponseEntity<Poll[]> listPollsResponse = restTemplate.getForEntity(uri, Poll[].class);
        assertThat(listPollsResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(listPollsResponse.getBody()).isNotNull();
        assertThat(listPollsResponse.getBody().length).isGreaterThan(0);
        assertThat(listPollsResponse.getBody()[0].getQuestion()).isEqualTo("Does pineapple belong on pizza?");
    }

    @Test
    public void testDeletePollAndVerifyVotesAreGone() throws URISyntaxException {
        String usersUrl = "http://localhost:" + randomServerPort + "/api/users";
        User creator = new User();
        creator.setUsername("Creator");
        creator.setEmail("creator@gmail.com");
        creator.setPassword("pass123");
        ResponseEntity<User> userResponse = restTemplate.postForEntity(usersUrl, creator, User.class);
        User createdCreator = userResponse.getBody();

        String pollsUrl = "http://localhost:" + randomServerPort + "/api/polls";
        VoteOption option = new VoteOption();
        option.setCaption("OK");
        option.setPresentationOrder(1);

        Poll pollToCreate = new Poll();
        pollToCreate.setQuestion("Delete Me Poll");
        pollToCreate.setPublishedAt(Instant.now());
        pollToCreate.setValidUntil(Instant.now().plusSeconds(100));
        pollToCreate.setPublic(true);
        pollToCreate.setUser(createdCreator);
        pollToCreate.setVoteOption(List.of(option));

        ResponseEntity<Poll> createdPollResponse = restTemplate.postForEntity(pollsUrl, pollToCreate, Poll.class);
        Poll createdPoll = createdPollResponse.getBody();
        assertThat(createdPoll).isNotNull();

        String votesUrl = pollsUrl + "/" + createdPoll.getId() + "/votes";
        Vote vote = new Vote();
        vote.setUser(createdCreator);
        vote.setPublishedAt(Instant.now());
        vote.setVotingOption(createdPoll.getVoteOption().get(0));
        restTemplate.postForEntity(votesUrl, vote, Vote.class);

        String deletePollUrl = pollsUrl + "/" + createdPoll.getId();
        restTemplate.delete(deletePollUrl);

        ResponseEntity<Vote[]> listResponse = restTemplate.getForEntity(votesUrl, Vote[].class);
        assertThat(listResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody()).isEmpty();
    }
}