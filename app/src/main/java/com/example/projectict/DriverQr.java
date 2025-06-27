package com.example.projectict;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import java.util.List;

public class DriverQr extends AppCompatActivity{
    private static final String TAG = "Driver QR";
    private DecoratedBarcodeView barcodeView;
    private FrameLayout frameLayoutCamera;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference locationRef;
    private TextView gpsText;
    private GoogleMap mMap;
    private Marker driverMarker;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Camera permission granted. Setting up scanner.");
                    setupAndStartScanner();
                } else {
                    Log.w(TAG, "Camera permission denied.");
                    Toast.makeText(this, "Camera permission is required to scan codes.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_qr);
        gpsText = findViewById(R.id.gpsText);
        frameLayoutCamera = findViewById(R.id.frame_layout_camera);
        checkCameraPermission();


    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupAndStartScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    private void setupAndStartScanner() {
        if (barcodeView == null) {
            barcodeView = new DecoratedBarcodeView(this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            barcodeView.setLayoutParams(layoutParams);
            frameLayoutCamera.addView(barcodeView);
            barcodeView.decodeContinuous(barcodeCallback);
        }
        barcodeView.resume();
    }
    private BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) {
                return;
            }
            barcodeView.pause();
            String scannedData = result.getText();
            String formatName = result.getBarcodeFormat().toString();
            Log.v(TAG, "Scanned Result: " + scannedData);
            Log.v(TAG, "Barcode Format: " + formatName);
            AlertDialog.Builder builder = new AlertDialog.Builder(DriverQr.this);
            builder.setTitle("Scan Result");
            builder.setMessage("Value: " + scannedData + "\nFormat: " + formatName);
            builder.setPositiveButton("Start Tracking", (dialog, which) -> {
                dialog.dismiss();

                // Start DriverTrackingActivity and pass scannedDriverId
                dialog.dismiss();
                frameLayoutCamera.removeAllViews();  // remove QR view
                barcodeView.pause();
                startTrackingWith(scannedData);
            });
            builder.setCancelable(false);
            builder.create().show();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        // Optional: for visual feedback
        }
    };
    private void startTrackingWith(String driverId) {

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_camera, mapFragment)
                .commit();

        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 15));
        });

        gpsText.setText("Starting location tracking for " + driverId);
        findViewById(R.id.btnStopTracking).setVisibility(View.VISIBLE);

        // Init Firebase
        locationRef = FirebaseDatabase.getInstance()
                .getReference("bus_locations").child(driverId);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null && mMap != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    gpsText.setText("Lat: " + lat + "\nLng: " + lng);

                    LatLng pos = new LatLng(lat, lng);
                    if (driverMarker == null) {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(pos).title("Bus Location"));
                    } else {
                        driverMarker.setPosition(pos);
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

                    // Optionally push to Firebase
                    locationRef.setValue(new BusLocation(lat, lng));
                }

            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, getMainLooper());

        findViewById(R.id.btnStopTracking).setOnClickListener(v -> {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            gpsText.setText("Tracking stopped.");
            findViewById(R.id.btnStopTracking).setVisibility(View.GONE);
        });
    }

    public static class BusLocation {
        public double latitude, longitude;
        public BusLocation() {}
        public BusLocation(double lat, double lng) {
            this.latitude = lat;
            this.longitude = lng;
        }
    }
    private void startScanner() {
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }
    private void pauseScanner() {
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        startScanner();
    }
    @Override
    public void onPause() {
        super.onPause();
        pauseScanner();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
            if (barcodeView.getParent() instanceof ViewGroup) {
                ((ViewGroup)
                        barcodeView.getParent()).removeView(barcodeView);
            }
        }
    }

}
