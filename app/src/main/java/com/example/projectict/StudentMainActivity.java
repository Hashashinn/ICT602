package com.example.projectict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentMainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }

        bottomNav = findViewById(R.id.bottom_nav);

        // BottomNav Listener
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_trace) {
                selectedFragment = new StudentTrackFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new StudentProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Set initial tab only if first launch
        if (savedInstanceState == null) {
            String target = getIntent().getStringExtra("fragment");
            if ("profile".equals(target)) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_trace);
            }
        }
    }
}
