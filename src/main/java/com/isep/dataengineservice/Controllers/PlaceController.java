package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class PlaceController {

    @Autowired
    PlaceService placeService;

    @GetMapping(value="/api/rawPlaces")
    public List<Place> rawPlaces(@RequestParam Double lon, Double lat ){
        return placeService.getRawPlaces(lon, lat);
    }
    @PostMapping(value="/api/formatPlaces")
    public List<Place> formattedPlaces(@RequestBody List<Place> places){
        return placeService.getFormattedPlaces(places);
    }

    /*what airflow will do
        GeoPosition geoPosition = geoPositions("Paris");
        List<Place> rawPlaces = rawPlaces( geoPosition.getLon(), geoPosition.getLat());
        List<Place> formattedPlaces = formattedPlaces(rawPlaces);
*/
    //use Kafka to run this over&over again in perimeters different than 500

    //controller to store the data in the data lake
    //controller to get tweets
    //controller to clean tweets
    //controller to store tweets
    //controller to combine places & tweets
    //controller to return sentiment analysis
    //controller to store sentiment analysis in data lake
    //controller to index in elastic

}
