package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.PlaceClusteringService;
import com.isep.dataengineservice.Services.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PlaceController {
    @Autowired
    PlaceService placeService;
    @Autowired
    PlaceClusteringService placeClusteringService;

    @GetMapping(value="/api/rawPlaces")
    public List<Place> rawPlaces(@RequestParam Double lon, Double lat ){
        return placeService.getRawPlaces(lon, lat);
    }
    @PostMapping(value="/api/formatPlaces")
    public List<Place> formattedPlaces(@RequestBody List<Place> places){
        return placeService.getFormattedPlaces(places);
    }
    @GetMapping(value="/api/getInterestingPlaces")
    public List<Place> clusterPlaces(@RequestParam Double lon, Double lat ){
        //2.2945&lat=48.8584
        List<Place> places = rawPlaces(2.2945, 48.8584);
        List<Place> clusteredPlaces  = placeClusteringService.DbscanCluster(places).get().stream().filter(place -> place.getRate()>=5).collect(Collectors.toList());
        clusteredPlaces.forEach(place -> System.out.println(place.getName()));
        return clusteredPlaces;
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
