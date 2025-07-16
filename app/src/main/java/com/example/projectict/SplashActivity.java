package com.example.projectict;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get currently authenticated user from Firebase (if any)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is already logged in — get their UID
            String uid = currentUser.getUid();
            // Find user role
            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("role");
            // Retrieve the role from db (student or driver)
            roleRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    // Redirect based on user role
                    if ("student".equals(role)) {
                        startActivity(new Intent(this, StudentMainActivity.class));
                    } else if ("driver".equals(role)) {
                        startActivity(new Intent(this, DriverMainActivity.class));
                    } else {
                        // Role not recognized — sign out and redirect to login
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                    }
                } else {
                    // Role entry not found — force logout
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                }
                finish();// Prevent returning to splash on back press
            }).addOnFailureListener(e -> {
                // Handle read error (e.g., no internet)
                Toast.makeText(this, "Autologin Failed", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });

        } else {
            // Not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
