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

    @GetMapping(value="/api/ingestGeoPositions")
    public void ingestGeoPositions() throws IOException {
        bigDataService.ingestGeoPositions();
    }

    @GetMapping(value="/api/ingestRawPlaces")
    public void ingestAndStoreRawPlaces() throws IOException {
        bigDataService.ingestRawPlaces();
    }

}
