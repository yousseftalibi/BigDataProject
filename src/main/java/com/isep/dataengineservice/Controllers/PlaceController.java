package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Services.GeoNodeService;
import com.isep.dataengineservice.Services.PlaceClusteringService;
import com.isep.dataengineservice.Services.PlaceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PlaceController {
    @Autowired
    PlaceService placeService;
    @Autowired
    PlaceClusteringService placeClusteringService;

    @Autowired
    GeoNodeService geoNodeService;

    @GetMapping(value="/api/rawPlaces")
    public List<Place> rawPlaces(@RequestParam Double lon, Double lat ){
        return placeService.getRawPlaces(lon, lat);
    }
    @PostMapping(value="/api/formatPlaces")
    public List<Place> formattedPlaces(@RequestBody List<Place> places){
        return placeService.getFormattedPlaces(places);
    }
    @GetMapping(value="/api/getInterestingPlaces")
    public List<Place> clusterPlaces(@RequestParam @NotNull List<Place> places){
        //2.2945&lat=48.8584
        //List<Place> places = rawPlaces(2.2945, 48.8584);
        List<Place> clusteredPlaces  = placeClusteringService.DbscanCluster(places).get().stream().filter(place -> place.getRate()>=5 && ! (place.getName().toLowerCase().contains("rue") || place.getName().toLowerCase().contains("cour") || place.getName().toLowerCase().contains("immeuble") || place.getName().toLowerCase().contains("street") || place.getName().toLowerCase().contains("way") || place.getName().toLowerCase().contains("house"))).collect(Collectors.toList());
       // clusteredPlaces.forEach(place -> System.out.println(place.getName()));
        return clusteredPlaces;
    }

    //cron job getting geoPosition
    //once gotten, kafka listenner that gets raw Places in each
    //once done, kafka listener that clusters list

    @GetMapping(value="/api/logic")
    public void logic(){
        GeoPosition Paris = geoNodeService.getGeoPosition("Paris");
        Set<GeoPosition> geoPositions = geoNodeService.BfsSearchGeoNodes(Paris, new HashSet<>());
        List<Place> placesInParis = new ArrayList<>();

        geoPositions.forEach(
                position -> rawPlaces(position.getLon(), position.getLat()).forEach(
                        place -> placesInParis.add(place)
                )
        );
        List<Place> clusteredPlaces = clusterPlaces(placesInParis);
        System.out.println("Places");
        clusteredPlaces.forEach(clusteredPlace -> System.out.println(clusteredPlace.getName()));
    }

    @KafkaListener(topics= "GeoNodes", groupId = "geoNodes-group")
    public void listenGeoNode(@NotNull ConsumerRecord<String, GeoPosition> record)  {
        System.out.println("geo node lon : "+ record.value().getLon());
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
