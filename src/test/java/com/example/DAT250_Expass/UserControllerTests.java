package com.example.DAT250_Expass;

import com.example.DAT250_Expass.Models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testCreateTwoUsersAndVerifyBothInList() throws URISyntaxException {

        final String baseUrl = "http://localhost:" + randomServerPort + "/api/users";
        URI uri = new URI(baseUrl);

        User user1 = new User(1, "Bob", "bob@gmail.com", "pass123");
        HttpEntity<User> request1 = new HttpEntity<>(user1);
        ResponseEntity<User> response1 = restTemplate.postForEntity(uri, request1, User.class);
        assertThat(response1.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<User[]> listResponse1 = restTemplate.getForEntity(uri, User[].class);
        assertThat(listResponse1.getBody()).isNotNull();
        assertThat(listResponse1.getBody()[0].getUsername()).isEqualTo("Bob");

        User user2 = new User(2, "Tom", "tom@gmail.com", "pass456");
        HttpEntity<User> request2 = new HttpEntity<>(user2);
        ResponseEntity<User> response2 = restTemplate.postForEntity(uri, request2, User.class);
        assertThat(response2.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<User[]> listResponse = restTemplate.getForEntity(uri, User[].class);

        ResponseEntity<User[]> listResponse2 = restTemplate.getForEntity(uri, User[].class);
        assertThat(listResponse2.getBody()).hasSize(2);

        List<String> usernames = Arrays.stream(listResponse2.getBody())
                .map(User::getUsername)
                .collect(Collectors.toList());
        assertThat(usernames).containsExactlyInAnyOrder("Bob", "Tom");
    }
}