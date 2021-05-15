package com.covvaccine.controller;

import com.covvaccine.model.User;
import com.covvaccine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public String saveUser(@RequestBody User user) throws ExecutionException, InterruptedException {
        return userService.saveUser(user);
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUser() throws ExecutionException, InterruptedException {
        return userService.getAllUser();
    }

    @GetMapping("/users/{email}")
    public User getUser(@PathVariable String email) throws ExecutionException, InterruptedException {

        return userService.getUserDetailsByEmail(email);
    }

}
