package com.example.projectict;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DriverMainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        // Request location permission if not already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
        // Initialize the bottom navigation view
        bottomNav = findViewById(R.id.driver_bottom_nav);
        // Set the default fragment (DriverTrackFragment) on first launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.driver_fragment_container, new DriverTrackFragment())
                    .commit();
        }
        // Set listener for navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_scan) {
                selectedFragment = new DriverTrackFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new DriverProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.driver_fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });
    }
    // Handle back button presses
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.driver_fragment_container);

        if (!(currentFragment instanceof DriverTrackFragment)) {
            bottomNav.setSelectedItemId(R.id.nav_scan); // Reset to default tab
        } else {
            super.onBackPressed(); // Exit app if already on main tab
        }
    }
    // Ensures correct tab is selected when returning to the activity
    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_scan);
        }
    }

}
