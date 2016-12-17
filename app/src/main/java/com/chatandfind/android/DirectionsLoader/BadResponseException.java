package com.chatandfind.android.DirectionsLoader;

/**
 * Created by ivan on 16.12.16.
 */

public class BadResponseException extends Exception{


    public BadResponseException(String message) {
        super(message);
    }

    public BadResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadResponseException(Throwable cause) {
        super(cause);
    }

}

