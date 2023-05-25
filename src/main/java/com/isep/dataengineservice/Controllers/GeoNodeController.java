package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.GeoNodeService;
import com.isep.dataengineservice.Services.PlaceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController

public class GeoNodeController {

    @Autowired
    GeoNodeService geoNodeService;
    @Autowired
    PlaceService placeService;
    @Autowired
    KafkaTemplate<String, GeoPosition> kafkaGeoPosTemplate;
    @Autowired
    KafkaTemplate<String, List<Place>> kafkaPlaceTemplate;


    //we give the name of a city and use RapidAPI to get its geoPosition (lon, lat). It's used mainly as a private function but we added endpoint just in case.
    @GetMapping(value="/api/GeoPositions/{city}")
    public GeoPosition getGeoPositionsFromRapidApi(@PathVariable String city){
        return geoNodeService.getGeoPosition(city);
    }

    //starting point, we give the name of a place and we get geoPosition from method getGeoPositionsFromRapidApi then we use BfsSearchGeoNodes to returns a list of (lon, lat)
   // the BfsSearchGeoNodes writes in a "GeoNodes" Kafka topic that triggers getRawPlacesFromGeoPosition for each geoPosition
    @GetMapping(value="/api/allGeoPositions/{PlaceName}")
    public Set<GeoPosition> getAllGeoPositionsFromBfsAlgo(@PathVariable String PlaceName){
        GeoPosition geoPosition = getGeoPositionsFromRapidApi(PlaceName);
        return geoNodeService.BfsSearchGeoNodes(geoPosition, new LinkedHashSet<>());
    }

    //triggered by BfsSearchGeoNodes in getAllGeoPositionsFromBfsAlgo, for each geoPosition with get list of places from RapidAPI and send it to "rawPlace"
    //in kafka tha triggers getInterestingPlacesFromRawPlaces() in PlaceController
    @KafkaListener(topics= "GeoNodes", groupId = "geoNodes-group", containerFactory = "geoPositionListenerContainerFactory")
    public void getRawPlacesFromGeoPositionKafka(@NotNull ConsumerRecord<String, GeoPosition> record)  {
        GeoPosition currentGeoPosition = record.value();
        System.out.println("received geoNodes from GeoNodeService.");
        List<Place> rawPlacesFromPosition = placeService.getRawPlaces(currentGeoPosition.getLon(), currentGeoPosition.getLat());
        System.out.println("Got rawPlaces from that geoNode. Sending to rawPlacesController.");
        if(!rawPlacesFromPosition.isEmpty()){
            kafkaPlaceTemplate.send("rawPlaces", rawPlacesFromPosition);
        }
    }

    public List<Place> getRawPlacesFromGeoPosition(GeoPosition currentGeoPosition)  {
        List<Place> rawPlacesFromPosition = placeService.getRawPlaces(currentGeoPosition.getLon(), currentGeoPosition.getLat());
       return rawPlacesFromPosition;
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
