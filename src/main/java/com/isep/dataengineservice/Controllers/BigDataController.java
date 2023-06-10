package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Services.BigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
public class BigDataController {
    @Autowired
    BigDataService bigDataService;
    @GetMapping(value = "api/storeGeoPositions/{city}")
    public void storeGeoPositions(@PathVariable String city) throws IOException{
        bigDataService.storeGeoPositions(city);
    }

    @GetMapping(value="/api/ingestGeoPositions/{city}")
    public void ingestGeoPositions(@PathVariable String city) throws IOException {
        bigDataService.ingestGeoPositions(city);
    }

    @GetMapping(value="/api/ingestRawPlaces/{city}")
    public void ingestAndStoreRawPlaces(@PathVariable String city) throws IOException {
        bigDataService.ingestRawPlaces(city);
    }

    @GetMapping(value="/api/indexInterestingPlaces/{city}")
    public void indexInterestingPlaces(@PathVariable String city) throws IOException {
        bigDataService.fetchAndIndexPlaces(city);
    }


}
