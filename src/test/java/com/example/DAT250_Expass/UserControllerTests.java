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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testAddUser() throws URISyntaxException {
        final String url = "http://localhost:" + randomServerPort + "/api/users";
        URI uri = new URI(url);
        User user = new User(1, "Bob", "bob@gmail.com", "SuperSecret123");
        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<User> result = this.testRestTemplate.postForEntity(uri, request, User.class);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
    }
}