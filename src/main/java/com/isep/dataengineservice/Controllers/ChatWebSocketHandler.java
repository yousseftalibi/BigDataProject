package com.isep.dataengineservice.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.isep.dataengineservice.Models.User.ChatMessage;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    Connection connection;
    private final List<WebSocketSession> sessions = new ArrayList<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        List<ChatMessage> oldChatMessages = getOldChatMessages();
        ObjectMapper objectMapper = new JsonMapper();
        for (ChatMessage message : oldChatMessages) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, @NotNull TextMessage message) throws JsonProcessingException {
        String payload = message.getPayload();
        ObjectMapper postMapper = new JsonMapper();
        ChatMessage chatMessage = postMapper.readValue(payload, ChatMessage.class);
        var _message = ChatMessage.builder().id(chatMessage.getId()).message(chatMessage.getMessage()).build();
        saveChatMessage(_message);
        ObjectMapper myob = new JsonMapper();
        var openedSessions = sessions.stream().filter(s -> s.isOpen()).collect(Collectors.toList());
        openedSessions.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(myob.writeValueAsString(_message)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void saveChatMessage(ChatMessage message) {
        try {
            String insertQuery = "INSERT INTO chatMessages (id, message) VALUES (?, ?)";
            PreparedStatement insertPs = connection.prepareStatement(insertQuery);
            insertPs.setString(1, message.getId() );
            insertPs.setString(2, message.getMessage());
            insertPs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<ChatMessage> getOldChatMessages() {
        try {
            List<ChatMessage> oldChatMessages = new ArrayList<>();
            String selectQuery = "SELECT id, message FROM chatMessages";
            PreparedStatement selectPs = connection.prepareStatement(selectQuery);
            ResultSet rs = selectPs.executeQuery();
            while (rs.next()) {
                oldChatMessages.add(new ChatMessage(rs.getString("id"), rs.getString("message")));
            }
            return oldChatMessages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
