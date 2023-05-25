package com.isep.dataengineservice.Services;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {
    private final List<String> allStreetKeywords = new ArrayList<>();
    static final int MIN_RATE_FILTERED = 4;
    public List<Place> getRawPlaces(Double lon, Double lat) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "https://opentripmap-places-v1.p.rapidapi.com/en/places/radius?radius=500&lon=" + lon + "&lat=" + lat;
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RapidAPI-Key", "6a4f81847bmsh8785c9220ccebdfp1b97bfjsn74f82815c241");
        headers.add("X-RapidAPI-Host", "opentripmap-places-v1.p.rapidapi.com");
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
}