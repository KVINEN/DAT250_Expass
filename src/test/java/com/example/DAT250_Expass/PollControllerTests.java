package com.example.DAT250_Expass;

import com.example.DAT250_Expass.Models.Poll;
import com.example.DAT250_Expass.Models.User;
import com.example.DAT250_Expass.Models.Vote;
import com.example.DAT250_Expass.Models.VoteOption;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PollControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    private User pollCreator;

    @BeforeEach
    public void setup() {
        final String url = "http://localhost:" + randomServerPort + "/api/users";
        pollCreator = new User();

        pollCreator.setUsername("Bob");
        pollCreator.setEmail("bob@gmail.com");
        pollCreator.setPassword("pass123");

        restTemplate.postForEntity(url, pollCreator, User.class);
    }

    @DynamicPropertySource
    static void valkeyProperties(DynamicPropertyRegistry registry) {
        String valkeyHost = System.getenv("VALKEY_HOST_TEST"); // Use a specific env var if needed later
        if (valkeyHost == null || valkeyHost.isEmpty()) {
            valkeyHost = System.getProperty("valkey.host"); // Check system property from Gradle
        }
        if (valkeyHost == null || valkeyHost.isEmpty()) {
            valkeyHost = "localhost"; // Default for local runs if nothing else is set
        }
        System.out.println("Setting valkey.host for test context: " + valkeyHost);
        // Set the system property that PollManager reads
        System.setProperty("valkey.host", valkeyHost);
        // If you were using Spring properties (e.g., spring.data.valkey.host), you'd set them like this:
        // registry.add("spring.data.valkey.host", () -> valkeyHost);
    }

    @Test
    public void createPoll() throws URISyntaxException {
        final String url = "http://localhost:" + randomServerPort + "/api/polls";
        URI uri = new URI(url);

        List<VoteOption> options = List.of(
                new VoteOption(1, "Yes", 1),
                new VoteOption(2, "No", 2)
        );
        Poll newPoll = new Poll(1, "Does pineapple belong on pizza?", Instant.now(), Instant.now().plusSeconds(3600), false, pollCreator, options, false);
        HttpEntity<Poll> request = new HttpEntity<>(newPoll);
        ResponseEntity<Poll> response = this.restTemplate.postForEntity(uri, request, Poll.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().getQuestion()).isEqualTo("Does pineapple belong on pizza?");

        ResponseEntity<Poll[]> response2 = restTemplate.getForEntity(uri, Poll[].class);

        assertThat(response2.getStatusCode().value()).isEqualTo(200);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody()).hasSize(1);
        assertThat(response2.getBody()[0].getQuestion()).isEqualTo("Does pineapple belong on pizza?");
    }

    @Test
    public void testDeletePollAndVerifyVotesAreGone() throws URISyntaxException {
        final String usersUrl = "http://localhost:" + randomServerPort + "/api/users";
        User pollCreator = new User(1, "Creator", "creator@gmail.com", "pass123");
        restTemplate.postForEntity(usersUrl, pollCreator, User.class);

        final String pollsUrl = "http://localhost:" + randomServerPort + "/api/polls";
        VoteOption option = new VoteOption(1, "OK", 1);
        Poll pollToCreate = new Poll(1, "Delete Me Poll", Instant.now(), Instant.now().plusSeconds(100), true, pollCreator, List.of(option), false);

        ResponseEntity<Poll> createdPollResponse = restTemplate.postForEntity(pollsUrl, pollToCreate, Poll.class);
        Poll createdPoll = createdPollResponse.getBody();
        assertThat(createdPoll).isNotNull();

        final String votesUrl = pollsUrl + "/" + createdPoll.getId() + "/votes";
        Vote vote = new Vote(1, pollCreator, Instant.now(), createdPoll.getOptions().get(0));
        restTemplate.postForEntity(votesUrl, vote, Vote.class);

        final String deletePollUrl = pollsUrl + "/" + createdPoll.getId();
        restTemplate.delete(deletePollUrl);

        ResponseEntity<Vote[]> listResponse = restTemplate.getForEntity(votesUrl, Vote[].class);

        assertThat(listResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody()).isEmpty();
    }

}
