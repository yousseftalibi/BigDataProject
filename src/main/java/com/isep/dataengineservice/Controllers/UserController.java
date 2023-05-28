package com.isep.dataengineservice.Controllers;
import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Repository.User.UserRepository;
import com.isep.dataengineservice.Services.User.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @PostMapping(value="/api/registerUser")
    public ResponseEntity<User> registerUser(@RequestBody @NotNull User user) throws SQLException {
        Boolean userAlreadyExists = userService.usernameTaken(user.getUsername());
        return userAlreadyExists ? ResponseEntity.status(HttpStatus.CONFLICT).build() : ResponseEntity.ok(userService.registerUser(user.getUsername(), user.getPassword()));
    }

    @PostMapping(value="/api/loginUser")
    public ResponseEntity<User> loginUser(@RequestBody @NotNull User user) throws SQLException {
        if(userService.loginUser(user) != null){
            return ResponseEntity.ok(userService.loginUser(user));
        } else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping(value="/api/getFriends")
    public List<User> getFriends(@RequestParam @NotNull Integer id) throws SQLException {
        User user = userService.getUserById(id);
        return userService.getFriends(user);
    }

    @GetMapping(value="/api/searchUsers")
    public List<User> searchUsers(@RequestParam @NotNull String query) throws SQLException {
        List<User> allUsers = userRepository.getAllUsers();
        List<User> matchingUsers = allUsers.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        return matchingUsers;
    }

    @PostMapping(value="/api/addFriend")
    public ResponseEntity<Object> addFriend(@RequestParam @NotNull Integer userId, @RequestParam @NotNull Integer friendId) throws SQLException {
        userService.addFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }
}