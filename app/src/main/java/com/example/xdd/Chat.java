package com.example.xdd;

import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private String lastMessage;

    public Chat(List<String> participants, String lastMessage) {
        this.participants = participants;
        this.lastMessage = lastMessage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}