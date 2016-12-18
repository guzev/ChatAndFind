package com.chatandfind.android;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatandfind.android.config.Config;

import android.content.Context;
import android.graphics.Color;

import com.chatandfind.android.DirectionsLoader.DirectionsLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback{
    private static final String TAG = "GoogleMapsActivity";

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference userReference;
    private DatabaseReference chatSettingsReference;

    private String chatId;
    private String encodedEmail;
    private Marker myLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        chatId = getIntent().getStringExtra(Config.CHAT_ID_TAG);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        encodedEmail = Config.encodeForFirebaseKey(Config.makeShortEmail(mFirebaseUser.getEmail()));
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userReference = databaseReference.child(Config.USERS).child(encodedEmail);
        chatSettingsReference = databaseReference.child(Config.CHATS_SETTINGS).child(chatId).child("users");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Log.d(TAG, "don't have location permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Config.MY_LOCATION_REQUEST_CODE);
        }

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (myLocationMarker != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationMarker.getPosition()));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                }
                return false;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(TAG, "setOnMapLongClickListener");
                if (myLocationMarker != null) {
                    Log.d(TAG, "setOnMapLongClickListener");
                    LatLng myPos = myLocationMarker.getPosition();
                    showDirection(GoogleMapsActivity.this, myPos.latitude, myPos.longitude, latLng.latitude, latLng.longitude);
                }
            }
        });
        addAllUsersMarkers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) throws SecurityException {
        if (requestCode == Config.MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 && permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "permission was denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addAllUsersMarkers() {
        chatSettingsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "addAllUsersMarkers : " + dataSnapshot.getChildrenCount() + " " + dataSnapshot.getKey());
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String childEncEmail = child.getKey();
                    Log.d("addAllUsersMarkers", child.getKey());
                    databaseReference.child(Config.USERS).child(childEncEmail).addValueEventListener(new ValueEventListener() {
                        private Marker marker;

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (marker == null) {
                                if (dataSnapshot.hasChild("longitude") && dataSnapshot.hasChild("latitude")) {
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .title((String) dataSnapshot.child("displayName").getValue())
                                            .position(new LatLng((double) dataSnapshot.child("latitude").getValue(), (double) dataSnapshot.child("longitude").getValue())));
                                    if (dataSnapshot.hasChild("photoUrl")) {
                                        downloadUserIcon(marker, (String) dataSnapshot.child("photoUrl").getValue());
                                    }
                                }
                                if (dataSnapshot.getKey().equals(encodedEmail)) {
                                    myLocationMarker = marker;
                                }
                            } else {
                                marker.setPosition(new LatLng((double) dataSnapshot.child("latitude").getValue(), (double) dataSnapshot.child("longitude").getValue()));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    private void showDirection(Context context, double firstLat, double firstLong, double secondLat, double secondLong) {

        DirectionsLoader loader = new DirectionsLoader(context, firstLat, firstLong, secondLat, secondLong);

        List<List<HashMap<String, String>>> result = loader.loadInBackground();

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

            Log.d("onPostExecute", "onPostExecute lineoptions decoded");

        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            mMap.addPolyline(lineOptions);
        } else {
            Log.d("showDirection", "without Polylines drawn");
        }
    }

    private void downloadUserIcon(final Marker marker, final String photoUrl) {
        new AsyncTask<Void, Void, Void>() {
            Bitmap bitmap;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    bitmap = Glide.with(GoogleMapsActivity.this).load(photoUrl).asBitmap().into(-1, -1).get();
                    bitmap = getCircularBitmap(bitmap);
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                } catch (Exception e) {
                    Log.e(TAG, "downloadUserIcon finished with error:\n" + e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (bitmap != null) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                }
            }
        }.execute();
    }

}
