package com.isep.dataengineservice.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.isep.dataengineservice.Models.User.Posts;
import com.isep.dataengineservice.Repository.User.ProfileRepository;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    ProfileRepository profileRepository;
    private final List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, @NotNull TextMessage message) throws JsonProcessingException {
        String payload = message.getPayload();
        ObjectMapper postMapper = new JsonMapper();
        Posts post = postMapper.readValue(payload, Posts.class);
        var postMessage = Posts.builder().id(post.getId()).message(post.getMessage()).build();

        profileRepository.saveMessage(postMessage);
        ObjectMapper myob = new JsonMapper();
        var openedSessions = sessions.stream().filter(s -> s.isOpen()).collect(Collectors.toList());
        openedSessions.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(myob.writeValueAsString(postMessage)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
