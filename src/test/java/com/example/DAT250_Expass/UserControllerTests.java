package com.example.DAT250_Expass;

import com.example.DAT250_Expass.Models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testCreateTwoUsersAndVerifyBothInList() throws URISyntaxException {
        final String baseUrl = "http://localhost:" + randomServerPort + "/api/users";
        URI uri = new URI(baseUrl);

        User user1 = new User();
        user1.setUsername("Bob");
        user1.setEmail("bob@gmail.com");
        user1.setPassword("pass123");
        restTemplate.postForEntity(uri, new HttpEntity<>(user1), User.class);

        User user2 = new User();
        user2.setUsername("Tom");
        user2.setEmail("tom@gmail.com");
        user2.setPassword("pass456");
        restTemplate.postForEntity(uri, new HttpEntity<>(user2), User.class);

        ResponseEntity<User[]> listResponse = restTemplate.getForEntity(uri, User[].class);
        assertThat(listResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(listResponse.getBody()).isNotNull();

        List<String> usernames = Arrays.stream(listResponse.getBody())
                .map(User::getUsername)
                .collect(Collectors.toList());
        assertThat(usernames).hasSize(2).containsExactlyInAnyOrder("Bob", "Tom");
    }
}