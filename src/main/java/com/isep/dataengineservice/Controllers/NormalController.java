package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.GeoNodeService;
import com.isep.dataengineservice.Services.Place.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
public class NormalController {

    @Autowired
    GeoNodeController geoNodeController;
    @Autowired
    PlaceService placeService;
    @Autowired
    GeoNodeService geoNodeService;

   @GetMapping(value="/api/normalController/{destination}")
    public List<Place> normal(@PathVariable("destination") String destination) {

       Set<GeoPosition> geoPositions = geoNodeService.getAllGeoPositionsFromBfsAlgo(destination);
       List<Place> interestingPlaces = new ArrayList<>();
        geoPositions.forEach( position -> {
            List<Place> rawPlaces = placeService.getRawPlacesFromGeoPosition(position);
            List<Place> filteredPlaces = placeService.getInterestingPlacesFromRawPlaces(rawPlaces);
            interestingPlaces.addAll(filteredPlaces);
        });

        return interestingPlaces;
    }


}
