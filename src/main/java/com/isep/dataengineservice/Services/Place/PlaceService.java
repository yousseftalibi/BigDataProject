package com.isep.dataengineservice.Services.Place;

import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import com.isep.dataengineservice.Models.StreetKeywords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {
    @Autowired
    PlacesWebSocketHandler placesWebSocketHandler;
    @Autowired
    PlaceClusteringService placeClusteringService;
    @Autowired
    KafkaTemplate<String, List<Place>> kafkaPlaceTemplate;
    private final List<String> allStreetKeywords = new ArrayList<>();
    static final int MIN_RATE_FILTERED = 4;
    public static Boolean stop = Boolean.FALSE;


    public List<Place> getRawPlaces(Double lon, Double lat) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "https://opentripmap-places-v1.p.rapidapi.com/en/places/radius?radius=500&lon=" + lon + "&lat=" + lat;
        HttpHeaders headers = new HttpHeaders();
        //headers.add("X-RapidAPI-Key", "6a4f81847bmsh8785c9220ccebdfp1b97bfjsn74f82815c241");
        //headers.add("X-RapidAPI-Host", "opentripmap-places-v1.p.rapidapi.com");
        //headers.add("X-RapidAPI-Key", "01f3cd1780mshb2b87fa150c52f3p195ac3jsn0517fb556b09");
        headers.add("X-RapidAPI-Key", "c4d4c4a3afmsh8073c2210da8497p1bf278jsne8174b51a3ec");
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Place.ApiResponse> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Place.ApiResponse.class);
        Place.ApiResponse places = response.getBody();
        return filterPlaces(places.getFeatures().stream()
                .map(e -> e.getProperties())
                .collect(Collectors.toList()));
    }
    private List<Place> filterPlaces(List<Place> places) {

        allStreetKeywords.addAll(StreetKeywords.frenchStreets);
        allStreetKeywords.addAll(StreetKeywords.arabicStreets);
        allStreetKeywords.addAll(StreetKeywords.afrikaansStreets);
        allStreetKeywords.addAll(StreetKeywords.chineseStreets);
        allStreetKeywords.addAll(StreetKeywords.dutchStreets);
        allStreetKeywords.addAll(StreetKeywords.englishStreets);
        allStreetKeywords.addAll(StreetKeywords.greekStreets);
        allStreetKeywords.addAll(StreetKeywords.germanStreets);
        allStreetKeywords.addAll(StreetKeywords.hindiStreets);
        allStreetKeywords.addAll(StreetKeywords.italianStreets);
        allStreetKeywords.addAll(StreetKeywords.japaneseStreets);
        allStreetKeywords.addAll(StreetKeywords.spanishStreets);
        allStreetKeywords.addAll(StreetKeywords.swedishStreets);
        allStreetKeywords.addAll(StreetKeywords.swahiliStreets);
        allStreetKeywords.addAll(StreetKeywords.portugueseStreets);
        allStreetKeywords.addAll(StreetKeywords.polishStreets);

        return places.stream()
                .filter(p -> p.getRate() >= MIN_RATE_FILTERED)
                .filter(p -> p.getName() != null && !p.getName().trim().isEmpty())
                .filter(p -> p.getKinds() != null && !p.getKinds().trim().isEmpty())
                .filter(p -> allStreetKeywords.stream().noneMatch(keyword -> p.getName().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }


    public List<Place> getRawPlacesFromGeoPosition(GeoPosition currentGeoPosition)  {
        List<Place> rawPlacesFromPosition = getRawPlaces(currentGeoPosition.getLon(), currentGeoPosition.getLat());
        return rawPlacesFromPosition;
    }

    @KafkaListener(topics= "rawPlaces", containerFactory = "placeListListenerContainerFactory")
    public void getInterestingPlacesFromRawPlaces(@NotNull ConsumerRecord<String, List<Place>> record){
        if(stop){
            return;
        }
        List<Place> rawPlacesFromPosition = record.value().stream().collect(Collectors.toList());
        List<Place> interestingPlaces = new ArrayList<>();
        if(!rawPlacesFromPosition.isEmpty()) {
            interestingPlaces = placeClusteringService.DbscanCluster(rawPlacesFromPosition).get();
        }

        if(!interestingPlaces.isEmpty()) {
            for (Place place : interestingPlaces) {
                placesWebSocketHandler.sendPlace(place);
            }
        }
    }
    public List<Place> getInterestingPlacesFromRawPlaces(List<Place> rawPlacesFromPosition){
        List<Place> interestingPlaces = new ArrayList<>();
        if(!rawPlacesFromPosition.isEmpty()) {
            interestingPlaces = placeClusteringService.DbscanCluster(rawPlacesFromPosition).get();
        }
        return interestingPlaces;
    }

    //is triggered by "interestingPlaces" kafka, in the function above getInterestingPlacesFromRawPlaces. This function takes each Place and prints it. In the future it should save it in gcp cloud.
    @KafkaListener(topics="interestingPlaces", containerFactory = "placeListListenerContainerFactory")
    public void storeInterestingPlaces(@NotNull ConsumerRecord<String, List<Place>> record) throws IOException {
        System.out.println("received interesetingPlaces from rawPlacesListener in PlaceController");
        List<Place> interestingPlaces = record.value();

        if(!interestingPlaces.isEmpty()) {
            File resultFile = new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\result"+record.offset()+".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile.getAbsoluteFile()));
            System.out.println("processing interesting places.");
            System.out.println("Interesting Places");
            interestingPlaces.forEach(clusteredPlace -> {
                try {
                    writer.write(clusteredPlace.getName() + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.close();
        }
    }


}