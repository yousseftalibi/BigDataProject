package com.isep.dataengineservice.Models.Trip;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// private Integer tempsDeVisite;
// private Integer visitBudget;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place implements Serializable {
    private String name;
    private int rate;
    private String kinds;
    private double dist;
    private String osm;
    private String wikidata;
    private String xid;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry implements Serializable{
        private String type;
        private List<Double> coordinates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature implements Serializable{
        private String type;
        private int id;
        private Geometry geometry;
        private Place properties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponse implements Serializable {
        private String type;
        private List<Feature> features;
    }

}

