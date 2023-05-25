package com.isep.dataengineservice.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isep.dataengineservice.Controllers.GeoNodeController;
import com.isep.dataengineservice.Models.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlacesWebSocketHandler implements WebSocketHandler {

    // A map to store the sessions and their corresponding destinations
    private final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    @Autowired
    GeoNodeController geoNodeController;

    // A method to handle the incoming messages from the frontend
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Parse the message payload as a string
        String destination = (String) message.getPayload();
        geoNodeController.getAllGeoPositionsFromBfsAlgo(destination);
        sessions.put(session, destination);
    }

    // A method to handle the connection establishment with the frontend
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Log a message when the connection is established
        System.out.println("WebSocket connected for places");

    }

    // A method to handle the connection termination with the frontend
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session from the map when the connection is closed
        sessions.remove(session);
    }

    // A method to handle any errors that occur during the communication
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Log the exception when an error occurs
        exception.printStackTrace();
    }


    // A method to indicate whether the handler supports partial messages or not
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // A method to send a place to the frontend based on the destination
    public void sendPlace(Place place, String destination) {
        // Iterate over the sessions and their destinations
        for (Map.Entry<WebSocketSession, String> entry : sessions.entrySet()) {
            System.out.println("trying to send place to frontend");
                try {
                    // Convert the place object to a JSON string
                    ObjectMapper mapper = new ObjectMapper();
                    String placeJson = mapper.writeValueAsString(place);
                    // Create a text message with the JSON string
                    TextMessage textMessage = new TextMessage(placeJson);
                    // Send the message to the session
                    entry.getKey().sendMessage(textMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }
}