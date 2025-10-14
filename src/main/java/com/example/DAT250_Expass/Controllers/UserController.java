package com.example.DAT250_Expass.Controllers;

import com.example.DAT250_Expass.Models.PollManager;
import com.example.DAT250_Expass.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private PollManager pollManager;

    @PostMapping("/api/users")
    public ResponseEntity<?> addUser(@RequestBody User user){
        User createUser = pollManager.addUser(user);
        return new ResponseEntity<>(createUser, HttpStatus.CREATED);
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getAllUsers (){
        List<User> users = pollManager.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
