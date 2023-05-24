package com.isep.dataengineservice.Services;

import javax.transaction.Transactional;

import com.isep.dataengineservice.Models.Place;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    SparkConf sparkConf = new SparkConf().set("spark.ui.port", "3000");
    SparkSession spark = SparkSession.builder().config(sparkConf).appName("clustering").master("local[*]").getOrCreate();

    public User getUserById(int userId) throws SQLException {
        User currentUser = new User();

        String getUserQuery = "SELECT * FROM users WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(getUserQuery);
        statement.setInt(1, userId);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            currentUser.setId(result.getInt("id"));
            currentUser.setUsername(result.getString("username"));
            currentUser.setPassword(result.getString("password"));
            currentUser.setPlaces(String.valueOf(result.getArray("places")));
        }
        return currentUser;
    }

    public List<User> getFriends(User user) throws SQLException {
        List<Integer> friendsIds = userRepository.getUserFriends(user);
        List<User> friends = new ArrayList<>();
        friendsIds.forEach(id -> {
            try {
                friends.add(getUserById(id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return friends;
    }


    public List<Integer> parsePlaces(String places) {
        List<Integer> placeList = new ArrayList<>();
        String[] placeArr = places.replace("{", "").replace("}", "").split(",");
        for (String place : placeArr) {
            placeList.add(Integer.parseInt(place.trim()));
        }
        return placeList;
    }


    public List<Place> predictPlace(User user) throws SQLException {

        /*

        INSERT INTO users (username, password, places) VALUES ('userA', 'pass', '{20,21,23}');
        INSERT INTO users (username, password, places) VALUES ('userB', 'pass', '{20,21,23,24}');
        if userA is friend's with userB and we try to recommend userA a destination, the algorithm predicts 24. not just because it is the missing value.
         */

        List<Integer> friendIds = userRepository.getUserFriends(user);

        Map<Integer, Integer> placeFrequencies = new HashMap<>();

        for (Integer friendId : friendIds) {
            User friend = getUserById(friendId);
            List<Integer> places = parsePlaces(friend.getPlaces());
            for (Integer placeId : places) {
                placeFrequencies.put(placeId, placeFrequencies.getOrDefault(placeId, 0) + 1);
            }
        }

        List<Row> data = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : placeFrequencies.entrySet()) {
            data.add(RowFactory.create(user.getId(), entry.getKey(), entry.getValue().doubleValue()));
        }

        StructType schema = new StructType(new StructField[]{
                new StructField("user", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("item", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("rating", DataTypes.DoubleType, false, Metadata.empty())
        });
        Dataset<Row> df = spark.createDataFrame(data, schema);

        ALS als = new ALS()
                .setMaxIter(10)
                .setRegParam(0.1)
                .setUserCol("user")
                .setItemCol("item")
                .setRatingCol("rating");
        ALSModel model = als.fit(df);

        List<Integer> visitedPlaces = parsePlaces(user.getPlaces());
        Set<Integer> allPlacesVisitedByFriends = new HashSet<>(placeFrequencies.keySet());
        allPlacesVisitedByFriends.removeAll(visitedPlaces);  // remove places that the user has already visited

        List<Row> predictData = new ArrayList<>();
        for (Integer placeId : allPlacesVisitedByFriends) {
            predictData.add(RowFactory.create(user.getId(), placeId));
        }

        Dataset<Row> predictions = model.transform(spark.createDataFrame(predictData, schema));

        int N = 1;
        Dataset<Row> topPlaces = predictions
                .select("item", "prediction")
                .orderBy(col("prediction").desc())
                .limit(N);

        return topPlaces.select("item").as(Encoders.INT()).collectAsList().stream()
                .map(placeId -> getPlaceById(placeId))
                .collect(Collectors.toList());

    }

    public Place getPlaceById(Integer placeId){
        return Place.builder().name(placeId.toString()).build();
    }
}
