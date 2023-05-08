package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Services.GeoNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashSet;
import java.util.Set;

@RestController
public class GeoNodeController {

    @Autowired
    GeoNodeService geoNodeService;
    @GetMapping(value="/api/GeoPositions/{city}")
    public GeoPosition geoPositions(@PathVariable String city){
        return geoNodeService.getGeoPosition(city);
    }

    @GetMapping(value="/api/allGeoPositions")
    public Set<GeoPosition> allGeoPositions(/*@RequestParam GeoPosition _geoPosition*/){
        GeoPosition geoPosition = geoPositions("Paris");
        return geoNodeService.BfsSearchGeoNodes(geoPosition, new HashSet<>());
    }


    /*what airflow will dogd

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
