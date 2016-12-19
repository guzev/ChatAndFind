package com.chatandfind.android.databaseObjects;

/**
 * Created by Vlad on 10.12.2016.
 */

public class Chat {
    private String id;
    private String title;
    private String lastMessage;
    private long lastMessageTime;
    private long lastSeenMessageTime;
    private String photoUrl;

    public Chat() {}

    public Chat(String title, String lastMessage, long lastMessageTime, long lastSeenMessageTime, String photoUrl) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastSeenMessageTime = lastSeenMessageTime;
        this.photoUrl = photoUrl;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getLastSeenMessageTime() {
        return lastSeenMessageTime;
    }

    public void setLastSeenMessageTime(long lastSeenMessageTime) {
        this.lastSeenMessageTime = lastSeenMessageTime;
    }
}
