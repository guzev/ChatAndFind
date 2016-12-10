package com.chatandfind.android.databaseObjects;

/**
 * Created by Vlad on 10.12.2016.
 */

public class Chat {
    private String title;
    private String lastMessage;
    private long lastMessageTime;

    public Chat() {}

    public Chat(String title, String lastMessage, long lastMessageTime) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
