package com.chatandfind.android.config;

import java.text.SimpleDateFormat;

/**
 * Created by Vlad on 11.12.2016.
 */

public class Config {
    public static final String CHAT_ID_TAG = "chat_id";
    public static final String NEW_USER_EMAIL = "new_user_email";
    public static final String DEFAULT_CHAT_NAME = "new Chat!";
    public static final String NEW_CHAT_NAME = "new_chat_name";
    public static final String CHATS = "chats";
    public static final String CHAT_LIST = "chat_list";
    public static final String CHATS_SETTINGS = "chats_settings";
    public static final String USERS = "users";
    public static final String ENC_EMAIL_TAG = "enc_email";

    public static final int MY_LOCATION_REQUEST_CODE = 1;

    public static final SimpleDateFormat sdfDate = new SimpleDateFormat("yy.MM.dd HH:mm");

    public static String encodeForFirebaseKey(String s) {
        return s
                .replace("_", "__")
                .replace(".", "_P")
                .replace("$", "_D")
                .replace("#", "_H")
                .replace("[", "_O")
                .replace("]", "_C")
                .replace("/", "_S");
    }
}
