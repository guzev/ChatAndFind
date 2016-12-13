package com.chatandfind.android.databaseObjects;

/**
 * Created by Vlad on 13.12.2016.
 */

public class MemberOfChat {
    String photoUrl;
    String displayName;

    MemberOfChat() {};

    MemberOfChat(String photoUrl, String displayName) {
        this.photoUrl = photoUrl;
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
