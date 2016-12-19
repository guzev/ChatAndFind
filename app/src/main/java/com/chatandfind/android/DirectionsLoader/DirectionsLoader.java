package com.chatandfind.android.DirectionsLoader;


import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.chatandfind.android.DirectionsAPI.DirectionsAPI;
import com.chatandfind.android.utils.IOUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ivan on 16.12.16.
 */

public class DirectionsLoader extends AsyncTaskLoader<List<List<HashMap<String, String>>>> {

    private static String TAG = "DirectionsLoader";
    private double firstLat, firstLong, secondLat, secondLong;

    public DirectionsLoader(Context context, double firstLat, double firstLong, double secondLat, double secondLong) {
        super(context);
        this.firstLat = firstLat;
        this.firstLong = firstLong;
        this.secondLat = secondLat;
        this.secondLong = secondLong;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<List<HashMap<String, String>>> loadInBackground() {
        HttpURLConnection connection = null;
        List<List<HashMap<String, String>>> result = null;
        InputStream in = null;

        try {
            connection = DirectionsAPI.getDirection(firstLat, firstLong, secondLat, secondLong);
            Log.d(TAG, "Performing request: " + connection.getURL());

            connection.connect();


            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "HTTP_OK");

                in = connection.getInputStream();

                result = DOMParser.parseDirection(in);

                Log.d("result size is ", result.size() + "");

                ArrayList<LatLng> points;
                PolylineOptions lineOptions = null;

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.RED);

                    Log.d("onPostExecute","onPostExecute lineoptions decoded");

                }

            } else {
                throw new BadResponseException("HTTP: " + connection.getResponseCode() + ", " + connection.getResponseMessage());
            }

        } catch (IOException e) {

        } catch (JSONException e) {

        } catch (BadResponseException e) {

        } finally {
            IOUtils.closeSilently(in);
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

}
