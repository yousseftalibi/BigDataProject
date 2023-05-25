package com.isep.dataengineservice.Services.User;

import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Repository.UserRepository;
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
    PasswordEncoder passwordEncoder;
    @Autowired
    Connection connection;

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

    public Boolean loginUser(User user) throws SQLException {
        var existingUser = userRepository.getUserById(user.getId());
        return existingUser != null && (passwordEncoder.matches(user.getPassword(), existingUser.getPassword()));
    }
    public boolean usernameTaken(String username) throws SQLException {
        return userRepository.usernameAlreadyTaken(username) ;
    }
    public Boolean registerUser(String username, String password) throws SQLException {
        userRepository.registerUser(username, password);
        return userRepository.getUserByUsername(username) != null;
    }

}


