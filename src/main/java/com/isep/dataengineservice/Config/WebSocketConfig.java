package com.isep.dataengineservice.Config;

import com.isep.dataengineservice.Controllers.MessageWebSocketHandler;
import com.isep.dataengineservice.Controllers.TripWebSocketHandler;
import com.isep.dataengineservice.Services.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    TripWebSocketHandler tripWebSocketHandler;
    @Autowired
    MessageWebSocketHandler messageWebSocketHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tripWebSocketHandler, "/places").setAllowedOrigins("*");
        registry.addHandler(messageWebSocketHandler, "/chat").setAllowedOrigins("*");
    }

}
