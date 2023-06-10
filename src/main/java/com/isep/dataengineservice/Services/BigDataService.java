package com.isep.dataengineservice.Services;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.io.ByteStreams;
import com.isep.dataengineservice.Models.GeoPosition;
import com.isep.dataengineservice.Models.Place;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
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
import java.io.*;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BigDataService {
    @Autowired
    GeoNodeService geoNodeService;
    @Autowired
    PlaceClusteringService placeClusteringService;
    @Autowired
    KafkaTemplate<String, GeoPosition> geoPositionKafkaTemplate;
    @Autowired
    private KafkaTemplate<String, List<Place>> placeListKafkaTemplate;
    private List<Place> accumulatedPlaces = new ArrayList<>();
    private List<Place> interestingPlaces = new ArrayList<>();
    private int id = 0;

    public static String city = "";


    private List<Place> fetchPlacesFromApi(Double lon, Double lat) {
        String rapidApiKey = getValidApiKey();
        RestTemplate restTemplate = new RestTemplate();
        String uri = "https://opentripmap-places-v1.p.rapidapi.com/en/places/radius?radius=500&lon=" + lon + "&lat=" + lat;
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RapidAPI-Key", rapidApiKey);
        headers.add("X-RapidAPI-Host", "opentripmap-places-v1.p.rapidapi.com");
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Place.ApiResponse> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Place.ApiResponse.class);
        Place.ApiResponse places = response.getBody();
        return places.getFeatures().stream()
                .map(e -> e.getProperties())
                .collect(Collectors.toList());
    }


    private boolean testRapidApiKey(String key){
        RestTemplate restTemplate = new RestTemplate();
        String uri = "https://opentripmap-places-v1.p.rapidapi.com/en/places/radius?radius=500&lon=0&lat=0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RapidAPI-Key", key);
        headers.add("X-RapidAPI-Host", "opentripmap-places-v1.p.rapidapi.com");
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Place.ApiResponse> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Place.ApiResponse.class);
        return response.getStatusCodeValue() == 200;
    }
    private String getValidApiKey(){
        if(testRapidApiKey("6a4f81847bmsh8785c9220ccebdfp1b97bfjsn74f82815c241")){
            return "6a4f81847bmsh8785c9220ccebdfp1b97bfjsn74f82815c241";
        }
        else if(testRapidApiKey("01f3cd1780mshb2b87fa150c52f3p195ac3jsn0517fb556b09")){
            return "01f3cd1780mshb2b87fa150c52f3p195ac3jsn0517fb556b09";
        }
        else if(testRapidApiKey("951fb39597msh0bafa42f6ead260p1823fcjsnf3f2da6d6ce2")){
            return "951fb39597msh0bafa42f6ead260p1823fcjsnf3f2da6d6ce2";
        }
        else{
            return "c4d4c4a3afmsh8073c2210da8497p1bf278jsne8174b51a3ec";
        }
    }


    public void storeGeoPositions(String city) throws IOException {
        BigDataService.city = city;
        GeoPosition parisGeoPos = geoNodeService.getGeoPosition(BigDataService.city);
        Set<GeoPosition> geoPositionSet = geoNodeService.BfsSearchGeoNodes(parisGeoPos, new LinkedHashSet<>());
        if(!geoPositionSet.isEmpty()){
            Schema schema = new Schema.Parser().parse(new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\GeoPosition.avsc"));
            String bucketName = "src_apirapid";
            String blobName = "raw/"+BigDataService.city+"_rawGeoPositions.avro";
            Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\youssefGoogleCloudStorage.json"));
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
            BlobId blobId = BlobId.of(bucketName, blobName);
            OutputStream outputStream = Channels.newOutputStream(storage.writer(BlobInfo.newBuilder(blobId).build()));
            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
            DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
            dataFileWriter.create(schema, outputStream);
            for (GeoPosition geoPosition : geoPositionSet) {
                GenericRecord geoPositionRecord = new GenericData.Record(schema);
                geoPositionRecord.put("name", geoPosition.getName());
                geoPositionRecord.put("country", geoPosition.getCountry());
                geoPositionRecord.put("lat", geoPosition.getLat());
                geoPositionRecord.put("lon", geoPosition.getLon());
                geoPositionRecord.put("population", geoPosition.getPopulation());
                geoPositionRecord.put("distanceFromStart", geoPosition.getDistanceFromStart());
                dataFileWriter.append(geoPositionRecord);
            }
            dataFileWriter.close();
        }
    }

    public void ingestGeoPositions(String city) throws IOException {
        BigDataService.city = city;
        String bucketName = "src_apirapid";
        String blobName = "raw/"+BigDataService.city+"_rawGeoPositions.avro";
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\youssefGoogleCloudStorage.json"));
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);
        Schema schema = new Schema.Parser().parse(new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\GeoPosition.avsc"));

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(new ByteArrayInputStream(blob.getContent()), datumReader);

        while (dataFileStream.hasNext()) {
            GenericRecord geoPositionRecord = dataFileStream.next();
            GeoPosition geoPosition = new GeoPosition();
            geoPosition.setName(geoPositionRecord.get("name") == null ? "" : geoPositionRecord.get("name").toString());
            geoPosition.setCountry(geoPositionRecord.get("country") == null ? "" : geoPositionRecord.get("country").toString());
            geoPosition.setLat((Double) geoPositionRecord.get("lat"));
            geoPosition.setLon((Double) geoPositionRecord.get("lon"));
            geoPosition.setPopulation(geoPositionRecord.get("population") == null ? 0 : (Integer) geoPositionRecord.get("population"));
            geoPosition.setDistanceFromStart(geoPositionRecord.get("distanceFromStart") == null ? 0.0 : (Double) geoPositionRecord.get("distanceFromStart"));
            geoPositionKafkaTemplate.send("consumeGeoNodes", geoPosition);
        }

        dataFileStream.close();
    }

    @KafkaListener(topics = "consumeGeoNodes", containerFactory = "geoPositionListenerContainerFactory")
    public void consumeGeoPosition(@NotNull ConsumerRecord<String, GeoPosition> record) throws IOException {
        GeoPosition geoPosition = record.value();;
        double lon = geoPosition.getLon();
        double lat = geoPosition.getLat();

        List<Place> rawPlaces = fetchPlacesFromApi(lon, lat);
        accumulatedPlaces.addAll(rawPlaces);
        if (accumulatedPlaces.size() >= 50000) {
            storeRawPlaces(accumulatedPlaces, id++);
            accumulatedPlaces.clear();
        }
    }

    public void storeRawPlaces(List<Place> rawPlaces, Integer id) throws IOException {

        Schema schema = new Schema.Parser().parse(new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\Place.avsc"));
        String bucketName = "src_apirapid";
        String blobName = "raw/"+BigDataService.city+"_rawPlaces"+id+".avro";
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\youssefGoogleCloudStorage.json"));
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        BlobId blobId = BlobId.of(bucketName, blobName);
        OutputStream outputStream = Channels.newOutputStream(storage.writer(BlobInfo.newBuilder(blobId).build()));
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(schema, outputStream);

        rawPlaces.forEach(rawPlace -> {
            GenericRecord placeRecord = new GenericData.Record(schema);
            placeRecord.put("name", rawPlace.getName());
            placeRecord.put("rate", rawPlace.getRate());
            placeRecord.put("kinds", rawPlace.getKinds());
            placeRecord.put("dist", rawPlace.getDist());
            placeRecord.put("osm", rawPlace.getOsm());
            placeRecord.put("wikidata", rawPlace.getWikidata());
            placeRecord.put("xid", rawPlace.getXid());
            try {
                dataFileWriter.append(placeRecord);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        dataFileWriter.close();
        outputStream.close();

    }

    public void ingestRawPlaces(String city) throws IOException {
        BigDataService.city = city;
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\youssefGoogleCloudStorage.json"));
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        String bucketName = "src_apirapid";
        for(int i = 0; i<=5; i++){
            String blobName = "raw/"+BigDataService.city+"_rawPlaces"+i+".avro";
            BlobId blobId = BlobId.of(bucketName, blobName);
            Blob blob = storage.get(blobId);
            Schema schema = new Schema.Parser().parse(new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\Place.avsc"));
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
            DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(new ByteArrayInputStream(blob.getContent()), datumReader);
            List<Place> rawPlaces = new ArrayList<>();
            while (dataFileStream.hasNext()) {
                GenericRecord rawPlaceRecord = dataFileStream.next();
                Place rawPlace = new Place();
                rawPlace.setName(rawPlaceRecord.get("name") == null ? "" : rawPlaceRecord.get("name").toString());
                rawPlace.setRate(rawPlaceRecord.get("rate") == null ? 0 : (Integer)  rawPlaceRecord.get("rate"));
                rawPlace.setKinds(rawPlaceRecord.get("kinds") == null ? "" : rawPlaceRecord.get("kinds").toString());
                rawPlace.setDist(rawPlaceRecord.get("dist") == null ? 0 : (Double)  rawPlaceRecord.get("dist"));
                rawPlace.setOsm(rawPlaceRecord.get("osm") == null ? "" : rawPlaceRecord.get("osm").toString());
                rawPlace.setWikidata(rawPlaceRecord.get("wikidata") == null ? "" : rawPlaceRecord.get("wikidata").toString());
                rawPlace.setXid(rawPlaceRecord.get("xid") == null ? "" : rawPlaceRecord.get("xid").toString());
                rawPlaces.add(rawPlace);
                if(rawPlaces.size()>= 500){
                    placeListKafkaTemplate.send("consumeRawPlaces", rawPlaces);
                    System.out.println("rawPlaces sent to consumeRawPlaces");
                    rawPlaces.clear();
                }
            }
            dataFileStream.close();
        }

    }

    @KafkaListener(topics= "consumeRawPlaces", containerFactory = "placeListListenerContainerFactory")
    public void consumeRawPlaces(@NotNull ConsumerRecord<String, List<Place>> record) throws IOException {

        List<Place> rawPlaces = record.value().stream().collect(Collectors.toList());
        if(!rawPlaces.isEmpty()) {
             interestingPlaces.addAll(placeClusteringService.DbscanCluster(rawPlaces).get());
        }
        if(interestingPlaces.size()>=100) {
            storeInterestingPlaces(interestingPlaces);
            interestingPlaces.clear();
        }
    }

    public void storeInterestingPlaces(List<Place> rawPlaces) throws IOException {
        Schema schema = new Schema.Parser().parse(new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\Place.avsc"));
        String bucketName = "src_apirapid";
        String blobName = "lake/"+BigDataService.city+"_interestingPlaces"+id+".avro";
        id++;
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\youssefGoogleCloudStorage.json"));
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        BlobId blobId = BlobId.of(bucketName, blobName);
        OutputStream outputStream = Channels.newOutputStream(storage.writer(BlobInfo.newBuilder(blobId).build()));

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(schema, outputStream);



        rawPlaces.forEach(rawPlace -> {
            GenericRecord placeRecord = new GenericData.Record(schema);
            placeRecord.put("name", rawPlace.getName());
            placeRecord.put("rate", rawPlace.getRate());
            placeRecord.put("kinds", rawPlace.getKinds());
            placeRecord.put("dist", rawPlace.getDist());
            placeRecord.put("osm", rawPlace.getOsm());
            placeRecord.put("wikidata", rawPlace.getWikidata());
            placeRecord.put("xid", rawPlace.getXid());
            try {
                dataFileWriter.append(placeRecord);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        dataFileWriter.close();
        outputStream.close();

    }
    public void fetchAndIndexPlaces(String city) throws IOException {

        BigDataService.city = city;
        String bucketName = "src_apirapid";
        String blobName = "lake/"+BigDataService.city+"_interestingPlaces"+0+".avro";

        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\youssefGoogleCloudStorage.json"));
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        Blob blob = storage.get(BlobId.of(bucketName, blobName));
        InputStream targetStream = Channels.newInputStream(blob.reader());
        Schema schema = new Schema.Parser().parse(new File("C:\\Users\\youss\\OneDrive\\Desktop\\desktop\\Tripy\\PlacesApp\\src\\main\\java\\com\\isep\\dataengineservice\\Models\\Place.avsc"));

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(new SeekableByteArrayInput(ByteStreams.toByteArray(targetStream)), datumReader);

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        BulkRequest request = new BulkRequest();

        while (dataFileReader.hasNext()) {
            GenericRecord placeRecord = dataFileReader.next();
            String jsonString = placeRecord.toString();

            request.add(new IndexRequest(BigDataService.city+"_interestingplaces").source(jsonString, XContentType.JSON));
        }

        BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);

        if (bulkResponse.hasFailures()) {
            System.out.println("Error: " + bulkResponse.buildFailureMessage());
        }

        dataFileReader.close();
        targetStream.close();
        client.close();
    }

}
