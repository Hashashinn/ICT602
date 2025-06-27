package com.example.projectict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudentTrack extends FragmentActivity {

    private GoogleMap mMap;
    private final Map<String, Marker> busMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_track);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    mMap = googleMap;

                    if (ActivityCompat.checkSelfPermission(StudentTrack.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }

                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(StudentTrack.this);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));
                        }
                    });

                    trackBuses(); // Start tracking once map is ready
                }
            });
        }
    }

    private void trackBuses() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("bus_locations");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateOrAddBus(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateOrAddBus(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String busId = snapshot.getKey();
                if (busId != null && busMarkers.containsKey(busId)) {
                    busMarkers.get(busId).remove();
                    busMarkers.remove(busId);
                }
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                // Optional: log error
            }
        });
    }

    private void updateOrAddBus(DataSnapshot snapshot) {
        String busId = snapshot.getKey();
        Double lat = snapshot.child("latitude").getValue(Double.class);
        Double lng = snapshot.child("longitude").getValue(Double.class);
        Long timestamp = snapshot.child("timestamp").getValue(Long.class);

        if (busId != null && lat != null && lng != null) {
            LatLng position = new LatLng(lat, lng);
            String time = timestamp != null ? DateFormat.format("hh:mm:ss a", new Date(timestamp)).toString() : "Unknown";

            if (busMarkers.containsKey(busId)) {
                busMarkers.get(busId).setPosition(position);
                busMarkers.get(busId).setSnippet("Last updated: " + time);
            } else {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Bus: " + busId)
                        .snippet("Last updated: " + time)
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                Bitmap.createScaledBitmap(
                                        BitmapFactory.decodeResource(getResources(), R.drawable.marker),
                                        80, 80, false
                                )
                        ))
                );
                if (marker != null) {
                    busMarkers.put(busId, marker);
                }
            }
        }
    }
}
