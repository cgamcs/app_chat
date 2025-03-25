package com.example.xdd;

public class Chat {
    private String chatName;
    private String lastMessage;

    public Chat(String chatName, String lastMessage) {
        this.chatName = chatName;
        this.lastMessage = lastMessage;
    }

    public String getChatName() {
        return chatName;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}