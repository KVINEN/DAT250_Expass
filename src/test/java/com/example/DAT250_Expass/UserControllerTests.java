package com.example.DAT250_Expass;

import com.example.DAT250_Expass.Models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testCreateAndListUsers() throws URISyntaxException {
        final String url = "http://localhost:" + randomServerPort + "/api/users";
        URI uri = new URI(url);

        User user = new User(1, "Bob", "bob@gmail.com", "SuperSecret123");
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<User> result = this.testRestTemplate.postForEntity(uri, request, User.class);

        assertThat(result.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<User[]> listResponse = this.testRestTemplate.getForEntity(uri, User[].class);;

        assertThat(listResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().length).isGreaterThan(0);
        assertThat(listResponse.getBody()[0].getUsername()).isEqualTo("Bob");
    }

}