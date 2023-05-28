package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.Trip.Place;
import com.isep.dataengineservice.Services.Trip.GeoNodeService;
import com.isep.dataengineservice.Services.Trip.PlaceClusteringService;
import com.isep.dataengineservice.Services.Trip.RecommendationService;
import com.isep.dataengineservice.Services.Trip.TripService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController

public class TripController {
    @Autowired
    TripService tripService;
    @Autowired
    RecommendationService recommendationService;

    @Autowired
    PlaceClusteringService placeClusteringService;
    @Autowired
    GeoNodeService geoNodeService;

    @GetMapping(value = "/api/getUserPlaces/{id}")
    public List<Place> getUserPlaces(@PathVariable int id) throws SQLException {
       return tripService.getUserPlaces(id);
    }
    @PostMapping(value="/api/visitPlace")
    public void visitPlace(@RequestParam @NotNull Integer userId, @RequestBody @NotNull Place place) throws SQLException {
        tripService.visitPlace(userId, place);
    }
    @GetMapping(value="/api/recommendDestination/{userId}")
    public List<Place> recommendDestination(@PathVariable("userId") int userId) throws SQLException  {
        List<Place> recommendedPlaces = recommendationService.RecommendPlaces(userId);
        return recommendedPlaces;
    }




}
