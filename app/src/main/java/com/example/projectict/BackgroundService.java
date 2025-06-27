package com.example.projectict;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "tracking_channel",
                    "Bus Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        startForeground(1, createNotification());
        startTracking();

        return START_STICKY;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, DriverQr.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, "tracking_channel")
                .setContentTitle("Bus Tracking Active")
                .setContentText("Your location is being updated.")
                .setSmallIcon(R.drawable.bus)  // Make sure R.drawable.bus exists
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
                    BusLocation location = new BusLocation(
                            loc.getLatitude(),
                            loc.getLongitude(),
                            System.currentTimeMillis()
                    );
                    locationRef.setValue(location);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
