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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("role");

            roleRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);

                    if ("student".equals(role)) {
                        startActivity(new Intent(this, StudentMainActivity.class));
                    } else if ("driver".equals(role)) {
                        startActivity(new Intent(this, DriverMainActivity.class));
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                    }
                } else {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                }
                finish();
            }).addOnFailureListener(e -> {
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
