package com.isep.dataengineservice.Services;

import com.isep.dataengineservice.Models.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PlaceService {
    private String places = "Agadir, Paris, Hong kong";

   /* @KafkaListener(topics ="Places", groupId = "places")
    public void consume(ConsumerRecord<String, List<Place>> record) throws IOException {
        System.out.println("consumed message "+ record.value());
        List<Place> rawPlaces = Collections.singletonList(new ObjectMapper().readValue((JsonParser) record.value(), Place.class));

        List<Place> uniquePlaces = new ArrayList<>();

        for (Object distinctName : rawPlaces) {
            if (distinctName.toString().trim().isEmpty()) {
                continue;
            }
            for (Place place : places) {
                String placeNameNormalized = place.getName().trim().toLowerCase();
                String distinctNameNormalized = distinctName.toString().trim().toLowerCase();

                if (placeNameNormalized.isEmpty() || distinctNameNormalized.isEmpty()) {
                    continue;
                }

                if (placeNameNormalized.equals(distinctNameNormalized) || placeNameNormalized.contains(distinctNameNormalized) || distinctNameNormalized.contains(placeNameNormalized)) {
                    uniquePlaces.add(place);
                    break;
                }
            }
        }
    }*/

    public List<Place> getRawPlaces(Double lon, Double lat) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "https://opentripmap-places-v1.p.rapidapi.com/en/places/radius?radius=500&lon="+lon+"&lat="+lat;
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RapidAPI-Key", "6a4f81847bmsh8785c9220ccebdfp1b97bfjsn74f82815c241");
        headers.add("X-RapidAPI-Host", "opentripmap-places-v1.p.rapidapi.com");
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Place.ApiResponse> response =  restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Place.ApiResponse.class );
        Place.ApiResponse places = response.getBody();
        return places.getFeatures().stream().map(e -> e.getProperties()).collect(Collectors.toList());
    }

    public List<Place> getFormattedPlaces(List<Place> places){

        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:5000/cluster_data";
        List<String> duplicatePlaces = new ArrayList<>();
        for (Place p:
                places) {
            duplicatePlaces.add(p.getName());
        }
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(duplicatePlaces, headers);
        ResponseEntity<List> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, List.class);

        List<Place> uniquePlaces = new ArrayList<>();

        for (Object distinctName : response.getBody()) {
            if (distinctName.toString().trim().isEmpty()) {
                continue;
            }
            for (Place place : places) {
                String placeNameNormalized = place.getName().trim().toLowerCase();
                String distinctNameNormalized = distinctName.toString().trim().toLowerCase();

                if (placeNameNormalized.isEmpty() || distinctNameNormalized.isEmpty()) {
                    continue;
                }
                if (placeNameNormalized.equals(distinctNameNormalized) || placeNameNormalized.contains(distinctNameNormalized) || distinctNameNormalized.contains(placeNameNormalized)) {
                    uniquePlaces.add(place);
                    break;
                }
            }
        }
        return uniquePlaces;
    }
}
