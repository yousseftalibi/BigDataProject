package com.isep.dataengineservice.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isep.dataengineservice.Models.Trip.Place;
import com.isep.dataengineservice.Services.Trip.GeoNodeService;
import com.isep.dataengineservice.Services.Trip.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TripWebSocketHandler implements WebSocketHandler {
    private final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    @Autowired
    GeoNodeService geoNodeService;
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        if (payload.equals("stop")) {
            TripService.stop = true; GeoNodeService.stop = true;
            session.close();
            sessions.remove(session);

        } else {
            TripService.stop = false; GeoNodeService.stop = false;
            String destination = payload;
            geoNodeService.getAllGeoPositionsFromBfsAlgo(destination);
            sessions.put(session, destination);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("webSocket connected for places");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    public void sendPlace(Place place) {
        for (Map.Entry<WebSocketSession, String> entry : sessions.entrySet()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String placeJson = mapper.writeValueAsString(place);
                TextMessage textMessage = new TextMessage(placeJson);
                if (entry.getKey().isOpen()) {
                    entry.getKey().sendMessage(textMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}