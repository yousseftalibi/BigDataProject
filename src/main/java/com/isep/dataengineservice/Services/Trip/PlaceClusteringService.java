package com.isep.dataengineservice.Services.Trip;

import com.isep.dataengineservice.Models.Trip.Place;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PlaceClusteringService {

    SparkConf sparkConf = new SparkConf().set("spark.ui.port", "3000");
    SparkSession spark = SparkSession.builder().config(sparkConf).appName("clustering").master("local[*]").getOrCreate();
    JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

    @NotNull
    final double NAME_WEIGHT = 1.2;
    final double FEATURE_WEIGHT = 1;
    @NotNull
    final double EPSILON = 5;
    final int MIN_POINTS = 1;

    public Optional<List<Place>> DbscanCluster(List<Place> places) {
        JavaRDD<Place> placesRDD = getPlacesRDD(places);
        JavaRDD<Place> placesNormalized = normalize(placesRDD);
        JavaRDD<Place> placesNormalizedDistancesAndRates = normalizeDistancesAndRates(placesNormalized);

        List<double[]> placeFeatures = placesNormalizedDistancesAndRates.map(place -> new double[]{place.getDist(), place.getRate()}).collect();

        List<DoublePoint> indexPoints = IntStream.range(0, placesNormalizedDistancesAndRates.map(Place::getName).collect().size())
                .mapToObj(i -> new double[]{(double) i})
                .map(DoublePoint::new)
                .collect(Collectors.toList());

        //we can change nameWeight and featureWeight to prioritize one over the other.
        LevenshteinDistanceMeasure myLevenshteinDistanceIMP = new LevenshteinDistanceMeasure(placesNormalizedDistancesAndRates, placeFeatures, NAME_WEIGHT, FEATURE_WEIGHT);

        //epsilon & minPts affect the result a lot.
        DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<>(EPSILON, MIN_POINTS, myLevenshteinDistanceIMP);

        List<Cluster<DoublePoint>> clusters = dbscan.cluster(indexPoints);

        List<Place> uniquePlaces = new ArrayList<>();

        for (Cluster<DoublePoint> cluster : clusters) {

            if (!cluster.getPoints().isEmpty()) {
                //just like in the Matrix, neoPlace is the Chosen place in a cluster. we take the first.
                DoublePoint neoPlace = cluster.getPoints().get(0);

                int neoIndex = indexPoints.indexOf(neoPlace);
                uniquePlaces.add(places.get(neoIndex));
            }
        }
        //jsc.close();
        return Optional.of(uniquePlaces);
    }

    private JavaRDD<Place> getPlacesRDD(List<Place> places) {
        return jsc.parallelize(places);
    }
    private static Double getMoyenne(List<Double> features){
        Optional<Double> somme = features.stream().reduce(Double::sum);
        int count = features.size();
        return somme.get() / count;
    }
    private static Double getVariance(List<Double> placeFeature, double moyenne){
        int count = placeFeature.size();
        double sumHolder = 0;
        for (double feat:
                placeFeature) {
            sumHolder += Math.pow( (feat - moyenne), 2 );
        }
        double variance = sumHolder / count;
        return variance;
    }
    private static Double getEcartType(List<Double> placeFeature, double moyenne){
        double variance = getVariance(placeFeature, moyenne);
        double ecartType = Math.sqrt(variance);
        return ecartType;
    }

    private static Double standarize(List features, Double element){
        double moyenne = getMoyenne(features);
        double ecartType = getEcartType(features, moyenne);
        return (element - moyenne) / ecartType;
    }

    private JavaRDD<Place> normalize(JavaRDD<Place> places) {
        return places.map(place -> {
            String name = StringUtils.stripAccents(place.getName()).toLowerCase();
            name = name.replaceAll("[^a-zA-Z0-9]+", " ").trim();
            return Place.builder().name(name).dist(place.getDist()).rate(place.getRate()).build();
        });
    }

    private static JavaRDD<Place> normalizeDistancesAndRates(JavaRDD<Place> places) {
        //since distance & rates are of different scales, distances can be huge and rate go from 1 to 7, we must bring them to the same scale.
        //we do this with standarization, the formula is: (x - moyenne) / écartType.

        List<Double> distances = places.map( place -> place.getDist() ).collect();
        List<Double> rates = places.map( place -> (double) place.getRate()).collect();

        return places.map(place -> Place.builder().name(place.getName()).dist(standarize(distances, place.getDist())).rate(standarize(rates, (double) place.getRate()).intValue() ).build()
        );
    }

}