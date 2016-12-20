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

    public static HttpURLConnection getDirection(double myLatitude, double myLongtitude, double destinationLatitude, double destinationLongtitude) throws IOException{
        Uri url = (BASE_URL).buildUpon().appendQueryParameter("origin", myLatitude + "," + myLongtitude).appendQueryParameter("destination", destinationLatitude + "," + destinationLongtitude).appendQueryParameter("mode", "walking").appendQueryParameter("sensor", "false").build();
        return (HttpURLConnection) new URL(url.toString()).openConnection();
    }

    private DirectionsAPI(){}
}
