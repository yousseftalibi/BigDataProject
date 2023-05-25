package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.GeoNodeService;
import com.isep.dataengineservice.Services.PlaceClusteringService;
import com.isep.dataengineservice.Services.PlaceService;
import com.isep.dataengineservice.Services.RecommendationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class NormalController {

    @Autowired
    GeoNodeController geoNodeController;
    @Autowired
    PlaceController placeController;

   @GetMapping(value="/api/normalController/{destination}")
    public List<Place> normal(@PathVariable("destination") String destination) {

        Set<GeoPosition> geoPositions = geoNodeController.getAllGeoPositionsFromBfsAlgo(destination);
        List<Place> interestingPlaces = new ArrayList<>();

        geoPositions.forEach( geoPosition -> {
            List<Place> rawPlaces = geoNodeController.getRawPlacesFromGeoPosition(geoPosition);
            List<Place> filteredPlaces = placeController.getInterestingPlacesFromRawPlaces(rawPlaces);
            interestingPlaces.addAll(filteredPlaces);
        });

        return interestingPlaces;
    }


}
