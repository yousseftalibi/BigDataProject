package com.isep.dataengineservice.Models.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatMessage {
    private String id;
    public String message;
}
