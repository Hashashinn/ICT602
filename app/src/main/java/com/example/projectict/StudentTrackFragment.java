package com.example.projectict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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

public class StudentTrackFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final Map<String, Marker> busMarkers = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.student_track, container, false);

        // Dynamically load the SupportMapFragment
        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));
            } else {
                Toast.makeText(requireContext(), "Unable to get current location. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        trackBuses();
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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateOrAddBus(DataSnapshot snapshot) {
        String busId = snapshot.getKey();
        Double lat = snapshot.child("latitude").getValue(Double.class);
        Double lng = snapshot.child("longitude").getValue(Double.class);
        Long timestamp = snapshot.child("timestamp").getValue(Long.class);

        if (busId != null && lat != null && lng != null) {
            LatLng position = new LatLng(lat, lng);
            String time = timestamp != null
                    ? DateFormat.format("hh:mm:ss a", new Date(timestamp)).toString()
                    : "Unknown";

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
                        )));
                if (marker != null) {
                    busMarkers.put(busId, marker);
                }
            }
        }
    }
}