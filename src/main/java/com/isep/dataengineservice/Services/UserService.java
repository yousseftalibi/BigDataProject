package com.isep.dataengineservice.Services;

import com.isep.dataengineservice.Models.User;
import com.isep.dataengineservice.Repository.UserRepository;
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

    public List<String> getUserPlaces(User user) throws SQLException {

            Array sqlArray = userRepository.getVisitedPlaces(user);
            if(sqlArray == null){
                return new ArrayList<>();
            }
            String[] visitedPlacesArray = (String[]) sqlArray.getArray();
            List<String> visitedPlaces = new ArrayList<>(Arrays.asList(visitedPlacesArray));
            return visitedPlaces;
    }


}
