package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Services.User.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController

public class UserController {
    @Autowired
    UserService userService;
    @PostMapping(value="/api/registerUser")
    public ResponseEntity<Object> registerUser(@RequestParam @NotNull String username, @RequestParam String password) throws SQLException {
        Boolean userAlreadyExists = userService.usernameTaken(username);
        return userAlreadyExists ? ResponseEntity.status(HttpStatus.CONFLICT).build() :  ResponseEntity.ok(userService.registerUser(username, password));
    }
    @PostMapping(value="/api/loginUser")
    public ResponseEntity<User> loginUser(@RequestBody @NotNull User user) throws SQLException {
        if(userService.loginUser(user)){
            return ResponseEntity.ok(user);
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @GetMapping(value="/api/getFriends")
    public List<User> getFriends(@RequestParam @NotNull Integer id) throws SQLException {
        User user = userService.getUserById(id);
        return userService.getFriends(user);
    }
}
