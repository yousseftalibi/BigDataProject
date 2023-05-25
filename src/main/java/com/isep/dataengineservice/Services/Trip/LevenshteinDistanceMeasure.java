package com.isep.dataengineservice.Services.Trip;

import com.isep.dataengineservice.Models.Trip.Place;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.spark.api.java.JavaRDD;

import java.util.List;

/*
    from wikipedia:
    La distance de Levenshtein est une distance, au sens mathématique du terme, donnant une mesure de la différence entre deux chaînes de caractères.
    Elle est égale au nombre minimal de caractères qu'il faut supprimer, insérer ou remplacer pour passer d’une chaîne à l’autre.

 */
public class LevenshteinDistanceMeasure implements DistanceMeasure {
    private LevenshteinDistance levenshteinDistance;
    private List<String> placeNames;
    private List<double[]> placeFeatures;
    private double nameWeight;
    private double featureWeight;
    public LevenshteinDistanceMeasure(JavaRDD<Place> placeNames, List<double[]> placeFeatures, double nameWeight, double featureWeight) {

        this.placeNames = placeNames.map(Place::getName).collect();
        this.placeFeatures = placeFeatures;
        this.nameWeight = nameWeight;
        this.featureWeight = featureWeight;
        this.levenshteinDistance = new LevenshteinDistance();
    }
    @Override
    public double compute(double[] a, double[] b) {
        int indexA = (int) a[0];
        int indexB = (int) b[0];

        double nameDistance = levenshteinDistance.apply(placeNames.get(indexA), placeNames.get(indexB));

        //distance euclidienne
        double featureDistance = Math.sqrt(Math.pow(placeFeatures.get(indexA)[0] - placeFeatures.get(indexB)[0], 2) + Math.pow(placeFeatures.get(indexA)[1] - placeFeatures.get(indexB)[1], 2));

        return nameWeight * nameDistance + featureWeight * featureDistance;

    }
}
