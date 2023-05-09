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


    @GetMapping(value="/api/GeoPositions/{city}")
    public GeoPosition geoPositions(@PathVariable String city){
        return geoNodeService.getGeoPosition(city);
    }

    @GetMapping(value="/api/allGeoPositions")
    public Set<GeoPosition> allGeoPositions(/*@RequestParam GeoPosition _geoPosition*/){
        GeoPosition geoPosition = geoPositions("Paris");
        return geoNodeService.BfsSearchGeoNodes(geoPosition, new LinkedHashSet<>());
    }


    //gets geoNodes and sends rawPlaces
    @KafkaListener(topics= "GeoNodes", groupId = "geoNodes-group", containerFactory = "geoPositionListenerContainerFactory")
    public void listenGeoNode(@NotNull ConsumerRecord<String, GeoPosition> record)  {
        GeoPosition currentGeoPosition = record.value();
        System.out.println("received geoNodes from GeoNodeService.");
        List<Place> rawPlacesFromPosition = placeService.getRawPlaces(currentGeoPosition.getLon(), currentGeoPosition.getLat());
        System.out.println("Got rawPlaces from that geoNode. Sending to rawPlacesController.");
        if(!rawPlacesFromPosition.isEmpty()){
            kafkaPlaceTemplate.send("rawPlaces", rawPlacesFromPosition);
        }
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
