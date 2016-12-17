package com.chatandfind.android.DirectionsAPI;

import android.net.Uri;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.android.gms.R.id.url;

/**
 * Created by ivan on 16.12.16.
 */

public class DirectionsAPI {
    private static Uri BASE_URL= Uri.parse("https://maps.googleapis.com/maps/api/directions/json?");

    private static String API_KEY = "AIzaSyAsrCZII1MWCOVZRtOounYSWi3mT5zZSzo";

    public static HttpURLConnection getDirection(long latitude, long longtitude, String adress) throws IOException{
        Uri url = (BASE_URL).buildUpon().appendQueryParameter("origin", latitude + "" + longtitude).appendQueryParameter("destination", adress).appendQueryParameter("key", API_KEY).build();
        return (HttpURLConnection) new URL(url.toString()).openConnection();
    }

    public static HttpURLConnection getDirection(long myLatitude, long myLongtitude, long destinationLatitude, long destinationLongtitude) throws IOException{
        Uri url = (BASE_URL).buildUpon().appendQueryParameter("origin", myLatitude + "" + myLongtitude).appendQueryParameter("destination", destinationLatitude + "" + destinationLongtitude).appendQueryParameter("key", API_KEY).build();
        return (HttpURLConnection) new URL(url.toString()).openConnection();
    }

    private DirectionsAPI(){}
}
