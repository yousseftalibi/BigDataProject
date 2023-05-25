package com.isep.dataengineservice.Services;

import javax.transaction.Transactional;

import com.isep.dataengineservice.Models.User;
import com.isep.dataengineservice.Repository.UserRepository;
import org.apache.spark.SparkConf;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.col;

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
