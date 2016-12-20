package com.chatandfind.android;

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

import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<List<List<HashMap<String, String>>>> {
    private static final String TAG = "GoogleMapsActivity";

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference userReference;
    private DatabaseReference usersChatSettingsReference;
    private DatabaseReference generalMarkerChatSettingsReference;
    private ChildEventListener chatSettingsListener;

    private String chatId;
    private String encodedEmail;
    private Marker myLocationMarker;
    private Marker generalMarker;
    private double friendLat, friendLng;
    private HashMap<String, ValueEventListener> listenersMap;
    ValueEventListener generalMarkerListener;
    private static boolean isLine = false;
    private Polyline line;
    private Button changingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        changingButton = (Button) findViewById(R.id.changing_button);
        chatId = getIntent().getStringExtra(Config.CHAT_ID_TAG);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        encodedEmail = Config.encodeForFirebaseKey(mFirebaseUser.getEmail());
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userReference = databaseReference.child(Config.USERS).child(encodedEmail);
        usersChatSettingsReference = databaseReference.child(Config.CHATS_SETTINGS).child(chatId).child("users");
        generalMarkerChatSettingsReference = databaseReference.child(Config.CHATS_SETTINGS).child(chatId).child("generalMarker");
        listenersMap = new HashMap<>();
        chatSettingsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

            public void removeListeners() {

            }


        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(savedInstanceState != null) {
            Log.d(TAG, "really it's worked");
            isLine = savedInstanceState.getBoolean("isLine");
            friendLat = savedInstanceState.getDouble("lat");
            friendLng = savedInstanceState.getDouble("lon");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean("isLine", isLine);
        outState.putDouble("lat", friendLat);
        outState.putDouble("lon", friendLng);
        if(isLine && line != null) {
            line.remove();
            getSupportLoaderManager().destroyLoader(0);
        }
    }


    @Override
    protected void onDestroy() {
        for (Map.Entry<String, ValueEventListener> entry : listenersMap.entrySet()) {
            databaseReference.child(Config.USERS).child(entry.getKey()).removeEventListener(entry.getValue());
        }
        generalMarkerChatSettingsReference.removeEventListener(generalMarkerListener);
        Log.d(TAG, "onDestroy");
        if(isLine && line != null) {
            line.remove();
           getSupportLoaderManager().destroyLoader(0);
        }
        super.onDestroy();
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
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationMarker.getPosition()));
                }
                return false;
            }
        });

        changingButton.setOnClickListener(new View.OnClickListener() {
            int state = 0;

            @Override
            public void onClick(View view) {
                if (state == 0) {
                    changingButton.setText("Путь");
                    state = 1;

                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            Log.d(TAG, "setOnMapLongClickListener");
                            if (myLocationMarker != null) {
                                Log.d(TAG, "setOnMapLongClickListener");
                                //LatLng myPos = myLocationMarker.getPosition();
                                friendLat = latLng.latitude;
                                friendLng = latLng.longitude;
                                if(isLine && line != null) {
                                    line.remove();
                                    getSupportLoaderManager().destroyLoader(0);
                                }
                                getSupportLoaderManager().initLoader(0, null, GoogleMapsActivity.this).forceLoad();
                            }

                        }
                    });
                } else {
                    changingButton.setText("Маркер");
                    state = 0;

                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            generalMarkerChatSettingsReference.child("longitude").setValue(latLng.longitude);
                            generalMarkerChatSettingsReference.child("latitude").setValue(latLng.latitude);
                        }
                    });
                }
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                generalMarkerChatSettingsReference.child("longitude").setValue(latLng.longitude);
                generalMarkerChatSettingsReference.child("latitude").setValue(latLng.latitude);
            }
        });

        addAllUsersMarkers();

        generalMarkerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("latitude") && dataSnapshot.hasChild("longitude")) {
                    if (generalMarker == null) {
                        generalMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng((double) dataSnapshot.child("latitude").getValue(), (double) dataSnapshot.child("longitude").getValue())));
                    } else {
                        generalMarker.setPosition(new LatLng((double) dataSnapshot.child("latitude").getValue(), (double) dataSnapshot.child("longitude").getValue()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        generalMarkerChatSettingsReference.addValueEventListener(generalMarkerListener);

    }

    @Override
    public Loader<List<List<HashMap<String, String>>>> onCreateLoader(int id, Bundle args) {
        LatLng myPos = myLocationMarker.getPosition();
        return new DirectionsLoader(this, myPos.latitude, myPos.longitude, friendLat, friendLng);
    }

    @Override
    public void onLoaderReset(Loader<List<List<HashMap<String, String>>>> loader) {
    }

    @Override
    public void onLoadFinished
            (Loader<List<List<HashMap<String, String>>>> loader, List<List<HashMap<String, String>>> result) {
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
            line = mMap.addPolyline(lineOptions);
            isLine = true;
        } else {
            isLine = false;
            Log.d("showDirection", "without Polylines drawn");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) throws SecurityException {
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
        usersChatSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "addAllUsersMarkers : " + dataSnapshot.getChildrenCount() + " " + dataSnapshot.getKey());
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String childEncEmail = child.getKey();
                    Log.d("addAllUsersMarkers", child.getKey());
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        private Marker marker = null;

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
                                    if (myLocationMarker != null) {
                                        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationMarker.getPosition()));
                                        if(isLine) {
                                            getSupportLoaderManager().initLoader(0, null, GoogleMapsActivity.this).forceLoad();
                                        }
                                    }
                                }
                            } else {
                                marker.setPosition(new LatLng((double) dataSnapshot.child("latitude").getValue(), (double) dataSnapshot.child("longitude").getValue()));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    listenersMap.put(childEncEmail, valueEventListener);
                    databaseReference.child(Config.USERS).child(childEncEmail).addValueEventListener(valueEventListener);
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
