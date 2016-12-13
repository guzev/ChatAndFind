package com.chatandfind.android.databaseObjects;

/**
 * Created by Vlad on 11.12.2016.
 */

public class Message {
    String id;
    String text;
    String name;
    String photoUrl;
    long time;

    public Message() {
    }

    public Message(String text, String name, String photoUrl, long time) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
