package com.isep.dataengineservice.Services.Trip;

import com.isep.dataengineservice.Models.Trip.Place;
import com.isep.dataengineservice.Models.User.User;
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

    /*
 WARNING: In case you get a stackOverFlow exception in Spark, reduce the value of the setMaxIter in the ALS parameters below.

    This is the ugliest, most inefficient piece of software even written since Internet Explorer, reader discretion is advised.

    I'd like to apologize to anyone reading this function, before explaining it. I had coded it with PlaceId in integer. In reality, the ID is in String.
    (I could've used the database ID, but it was too late). luckily the placeId only contained one character at the beginning and the rest is numerical
    that's what parsePlaces does, it removes the first character. but then the numbers were too large for Integer, so I used Long but the numbers were too large even for Long
    so I divided by 10,000 (the IDs are so far, that I'm almost certain they'll be no collisions). I ran the algorithm and then ran a function that
    gets the recommended places by ID. but now the IDs don't have their first character, and they are divided by 10,000. So to create a link between them, I linked them in a hashmap.
*/
    SparkConf sparkConf = new SparkConf().set("spark.ui.port", "3000");
    SparkSession spark = SparkSession.builder().config(sparkConf).appName("clustering").master("local[*]").getOrCreate();
    @Autowired
    UserService userService;
    @Autowired
    TripService tripService;

    private List<Long> parsePlaces(String places) {
        List<Long> placeList = new ArrayList<>();
        String[] placeArr = places.replace("{", "").replace("}", "").split(",");
        for (String place : placeArr) {
            placeList.add(Long.parseLong(place.trim().substring(1)));
        }
        return placeList;
    }
    public List<Place> RecommendPlaces(Integer userId) throws SQLException {


        List<Place> recommendedPlaces = new ArrayList<>();
        Map<String, Long> idMap = new HashMap<>();

        User user = userService.getUserById(userId);
        List<User> friends =  userService.getFriends(user);

        List<String> friendsVisitedPlacesIdsString;
        friendsVisitedPlacesIdsString = friends.stream()
                .flatMap(friend -> {
                    try {
                        return userService.getUserPlacesIds(friend).stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(place -> place.length() > 1)
                .collect(Collectors.toList());

        List<Long> friendsVisitedPlacesIdsLong;

        friendsVisitedPlacesIdsLong = friends.stream()
                .flatMap(friend -> {
                    try {
                        return userService.getUserPlacesIds(friend).stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(place -> place.length() > 1)
                .map(place -> {
                    long transformedId = Long.parseLong(place.substring(1))/10000;
                    idMap.put(place, transformedId);
                    return transformedId;
                })
                .collect(Collectors.toList());


        Map<Long, Integer> placeFrequencies = new HashMap<>();
        for (Long placeId : friendsVisitedPlacesIdsLong) {
            placeFrequencies.put(placeId, placeFrequencies.getOrDefault(placeId, 0) + 1);
        }

        List<Row> data = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : placeFrequencies.entrySet()) {
            data.add(RowFactory.create(user.getId(), entry.getKey(), entry.getValue().doubleValue()));
        }

        StructType schema = new StructType(new StructField[]{
                new StructField("user", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("item", DataTypes.LongType, false, Metadata.empty()),
                new StructField("rating", DataTypes.DoubleType, false, Metadata.empty())
        });
        Dataset<Row> df = spark.createDataFrame(data, schema);

        //In case you get a stackOverFlow exception in Spark, reduce the value of the setMaxIter
        ALS als = new ALS()
                .setMaxIter(5)
                .setRegParam(0.1)
                .setUserCol("user")
                .setItemCol("item")
                .setRatingCol("rating");
        ALSModel model = als.fit(df);

        List<Long> visitedPlaces = parsePlaces(user.getPlaces());
        Set<Long> allPlacesVisitedByFriends = new HashSet<>(placeFrequencies.keySet());
        allPlacesVisitedByFriends.removeAll(visitedPlaces);

        List<Row> predictData = new ArrayList<>();
        for (Long placeId : allPlacesVisitedByFriends) {
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
                String originalId = idMap.entrySet().stream()
                        .filter(entry -> Objects.equals(entry.getValue(), Long.parseLong(place)))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                if (originalId != null) {
                    recommendedPlaces.add(tripService.getPlaceById(originalId));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return recommendedPlaces;
    }

}
