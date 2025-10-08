package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен GET запрос на :PORT/users");
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody final User user) {
        log.info("Получен POST запрос на :PORT/users");
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody final User user) {
        log.info("Получен PUT запрос на :PORT/users");
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getUserByID(@PathVariable int id) {
        log.info("Получен GET запрос на :PORT/users/{id}");
        return userService.getUserByID(id);
    }

    @PutMapping("/{userID}/friends/{friendID}")
    public String addFriendship(@PathVariable int userID, @PathVariable int friendID) {
        log.info("Получен PUT запрос на :PORT/{id}/friends/{another_id}");
        return userService.addFriendship(userID, friendID);
    }

    @DeleteMapping("/{userID}/friends/{friendID}")
    public String removeFriendship(@PathVariable int userID, @PathVariable int friendID) {
        log.info("Получен DELETE запрос на :PORT/{id}/friends/{another_id}");
        return userService.removeFriendship(userID, friendID);
    }

    @GetMapping("/{userID}/friends")
    public List<User> getFriendsOfUser(@PathVariable int userID) {
        log.info("Получен GET запрос на :PORT/{id}/friends}");
        return userService.getFriendsOfUser(userID);
    }

    @GetMapping("/{userID}/friends/common/{anotherID}")
    public List<User> getFriendsCrossing(@PathVariable int userID, @PathVariable int anotherID) {
        log.info("Получен GET запрос на :PORT/{id}/friends/common/{another_id}");
        return userService.getFriendsCrossing(userID, anotherID);
    }
}