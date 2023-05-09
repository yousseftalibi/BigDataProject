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

}
