package com.chatandfind.android;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.chatandfind.android.config.Config;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Vlad on 18.12.2016.
 */

public class UpdatingLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = "UpdatingLocationService";
    DatabaseReference databaseReference;
    String encodedEmail;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        encodedEmail = Config.encodeForFirebaseKey(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException {
        Log.d(TAG, "onConnected");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG, "Last Location lat: " + mLastLocation.getLatitude() + " long: " + mLastLocation.getLongitude());
        }
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged" + " long: " + location.getLongitude() + " lat: " + location.getLatitude());
        databaseReference.child(Config.USERS).child(encodedEmail).child("latitude").setValue(location.getLatitude());
        databaseReference.child(Config.USERS).child(encodedEmail).child("longitude").setValue(location.getLongitude());
    }
}
