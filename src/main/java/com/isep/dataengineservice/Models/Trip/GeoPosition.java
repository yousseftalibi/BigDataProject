package com.isep.dataengineservice.Models.Trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class GeoPosition implements Serializable {
    private String name;
    private String country;
    private Double lat;
    private Double lon;
    private int population;
    private double distanceFromStart;

}
