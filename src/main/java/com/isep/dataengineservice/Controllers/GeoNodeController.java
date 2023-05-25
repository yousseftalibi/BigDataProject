package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Services.GeoNodeService;
import com.isep.dataengineservice.Services.Place.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController

public class GeoNodeController {

    @Autowired
    GeoNodeService geoNodeService;

    //we give the name of a city and use RapidAPI to get its geoPosition (lon, lat). It's used mainly as a private function but we added endpoint just in case.
    @GetMapping(value="/api/GeoPositions/{destination}")
    public GeoPosition getGeoPositionsFromRapidApi(@PathVariable String destination){
        return geoNodeService.getGeoPosition(destination);
    }

    //starting point, we give the name of a place and we get geoPosition from method getGeoPositionsFromRapidApi then we use BfsSearchGeoNodes to returns a list of (lon, lat)
   // the BfsSearchGeoNodes writes in a "GeoNodes" Kafka topic that triggers getRawPlacesFromGeoPosition for each geoPosition
    @GetMapping(value="/api/allGeoPositions/{PlaceName}")
    public Set<GeoPosition> getAllGeoPositionsFromBfsAlgo(@PathVariable String PlaceName){
        return geoNodeService.getAllGeoPositionsFromBfsAlgo(PlaceName);
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
