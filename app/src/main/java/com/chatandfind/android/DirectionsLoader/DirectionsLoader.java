package com.chatandfind.android.DirectionsLoader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chatandfind.android.DirectionsAPI.DirectionsAPI;
import com.chatandfind.android.utils.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ivan on 16.12.16.
 */

public class DirectionsLoader extends AsyncTaskLoader<List<List<HashMap<String, String>>>> {

    private static String TAG = "DirectionsLoader";
    private long firstLat, firstLong, secondLat, secondLong;

    public DirectionsLoader(Context context, long firstLat, long firstLong, long secondLat, long secondLong) {
        super(context);
        this.firstLat = firstLat;
        this.firstLong = firstLong;
        this.secondLat = secondLat;
        this.secondLong = secondLong;
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
