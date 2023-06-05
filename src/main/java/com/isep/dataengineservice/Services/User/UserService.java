package com.isep.dataengineservice.Services.User;

import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Repository.User.FriendRepository;
import com.isep.dataengineservice.Repository.User.UserRepository;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    FriendRepository friendRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    Connection connection;

    public void modifyAccount(Integer userId, String newUsername, String newPassword) throws SQLException {
        userRepository.modifyAccount(userId, newUsername, passwordEncoder.encode(newPassword));
    }
    public User getUserById(int userId) throws SQLException {
       return userRepository.getUserById(userId);
    }

    public List<Integer> getUserFriendIds(User user) throws SQLException{
        return userRepository.getUserFriendIds(user);
    }

    public List<User> getFriends(User user) throws SQLException{
        List<Integer> friendsIds = getUserFriendIds(user);
        List<User> friends = new ArrayList<>();
        friendsIds.forEach( id -> {
            try {
                friends.add(getUserById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return friends;
    }
    public List<String> getUserPlacesIds(User user) throws SQLException {
            Array sqlArray = userRepository.getVisitedPlaces(user);
            if(sqlArray == null){
                return new ArrayList<>();
            }
            String[] visitedPlacesArray = (String[]) sqlArray.getArray();
            List<String> visitedPlaces = new ArrayList<>(Arrays.asList(visitedPlacesArray));
            return visitedPlaces;
    }

    public User loginUser(User user) throws SQLException {
        var existingUser = userRepository.getUserByUsername(user.getUsername());
        if( existingUser != null && (passwordEncoder.matches(user.getPassword(), existingUser.getPassword()))){
            return existingUser;
        }
        return null;
    }
    public boolean usernameTaken(String username) throws SQLException {
        return userRepository.usernameAlreadyTaken(username) ;
    }
    public User registerUser(String username, String password) throws SQLException {
        userRepository.registerUser(username, passwordEncoder.encode(password));
        if(userRepository.getUserByUsername(username) != null){
            return userRepository.getUserByUsername(username);
        };
       return null;
    }

    public void addFriend(int userId, int friendId) throws SQLException {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user == null || friend == null) {
            throw new IllegalArgumentException("invalid user ids");
        }
        List<Integer> friendIds = userRepository.getUserFriendIds(user);
        if (friendIds.contains(friendId)) {
            throw new IllegalArgumentException("users are already friends");
        }
        friendRepository.addFriend(userId, friendId);
        friendRepository.addFriend(friendId, userId);
    }



}


