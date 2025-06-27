package com.example.projectict;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BackgroundService extends Service {

    private static final String TAG = "LocationService";
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private DatabaseReference locationRef;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String driverId = intent.getStringExtra("driverId");
        if (driverId == null || driverId.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        locationRef = FirebaseDatabase.getInstance().getReference("bus_locations").child(driverId);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        startForeground(1, createNotification());
        startTracking();

        return START_STICKY;
    }

    private Notification createNotification() {
        // Intent to open the app (specifically DriverQr)
        Intent notificationIntent = new Intent(this, DriverQr.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE // or FLAG_UPDATE_CURRENT if targeting below API 31
        );

        return new NotificationCompat.Builder(this, "tracking_channel")
                .setContentTitle("Bus Tracking Active")
                .setContentText("Your location is being updated.")
                .setSmallIcon(R.drawable.bus)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }


    private void startTracking() {
        LocationRequest request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    locationRef.setValue(new DriverQr.BusLocation(loc.getLatitude(), loc.getLongitude()));
                }
            }
        };

        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedClient.requestLocationUpdates(request, locationCallback, getMainLooper());
        }
    }

    @Override
    public void onDestroy() {
        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}