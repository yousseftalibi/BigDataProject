package com.isep.dataengineservice.Services.Trip;

import com.isep.dataengineservice.Models.Trip.Place;
import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Repository.TripRepository;
import com.isep.dataengineservice.Services.User.UserService;
import org.apache.spark.SparkConf;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import static org.apache.spark.sql.functions.col;

@Service
public class RecommendationService {

    SparkConf sparkConf = new SparkConf().set("spark.ui.port", "3000");
    SparkSession spark = SparkSession.builder().config(sparkConf).appName("clustering").master("local[*]").getOrCreate();
    @Autowired
    UserService userService;
    @Autowired
    TripService tripService;

    private List<Integer> parsePlaces(String places) {
        List<Integer> placeList = new ArrayList<>();
        String[] placeArr = places.replace("{", "").replace("}", "").split(",");
        for (String place : placeArr) {
            placeList.add(Integer.parseInt(place.trim()));
        }
        return placeList;
    }

    public List<Place> predictPlace(Integer userId) throws SQLException {

        /*

        INSERT INTO users (username, password, places) VALUES ('userA', 'pass', '{20,21,23}');
        INSERT INTO users (username, password, places) VALUES ('userB', 'pass', '{20,21,23,24}');
        if userA is friend's with userB and we try to recommend userA a destination, the algorithm predicts 24. not just because it is the missing value.
         */
        List<Place> recommendedPlaces = new ArrayList<>();

        User user = userService.getUserById(userId);
        List<User> friends =  userService.getFriends(user);

        List<String> friendsVisitedPlacesIdsString = new ArrayList<>();
        friends.forEach(friend -> {
            try {
                friendsVisitedPlacesIdsString.addAll(userService.getUserPlacesIds(friend));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        List<Integer> friendsVisitedPlacesIdsInteger = new ArrayList<>();
        friendsVisitedPlacesIdsString.forEach(visitedPlace -> friendsVisitedPlacesIdsInteger.add(Integer.parseInt(visitedPlace)));


        Map<Integer, Integer> placeFrequencies = new HashMap<>();
        for (Integer placeId : friendsVisitedPlacesIdsInteger) {
            placeFrequencies.put(placeId, placeFrequencies.getOrDefault(placeId, 0) + 1);
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
        allPlacesVisitedByFriends.removeAll(visitedPlaces);

        List<Row> predictData = new ArrayList<>();
        for (Integer placeId : allPlacesVisitedByFriends) {
            predictData.add(RowFactory.create(user.getId(), placeId));
        }

        Dataset<Row> predictions = model.transform(spark.createDataFrame(predictData, schema));

        int N = 3;
        Dataset<Row> topPlaces = predictions
                .select("item", "prediction")
                .orderBy(col("prediction").desc())
                .limit(N);


        List<String> PlacesIds = topPlaces.select("item").as(Encoders.STRING()).collectAsList().stream().collect(Collectors.toList());

        PlacesIds.forEach(place -> {
            try {
                recommendedPlaces.add(tripService.getPlaceById(place));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return recommendedPlaces;
    }


}
