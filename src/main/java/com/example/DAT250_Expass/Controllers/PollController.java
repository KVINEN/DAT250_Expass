package com.example.DAT250_Expass.Controllers;

import com.example.DAT250_Expass.Models.Poll;
import com.example.DAT250_Expass.Models.PollManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PollController {

    @Autowired
    private PollManager pollManager;

    @PostMapping("/api/polls")
    public ResponseEntity<Poll>  createPoll (@RequestBody Poll poll){
        Poll createPoll = pollManager.addPoll(poll);
        return new ResponseEntity<>(createPoll, HttpStatus.CREATED);
    }

    @GetMapping("/api/polls")
    public ResponseEntity<List<Poll>> getAllPolls() {
        List<Poll> polls = pollManager.getAllPolls();
        return new ResponseEntity<>(polls, HttpStatus.OK);
    }
}
