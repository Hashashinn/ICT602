package com.example.projectict;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText e1, e2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        e1 = (EditText)findViewById(R.id.editEmail);
        e2 = (EditText)findViewById(R.id.editPassword);
        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
    }
    public void loginUser(View v) {
        //get input from user
        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();
        //show error if no input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fields cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        // Authentication process
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get current user UID
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        // find users data in databases
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("role");
                        // Check for the role of the user
                        ref.get().addOnCompleteListener(roleTask -> {
                            if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                                String role = roleTask.getResult().getValue(String.class);

                                Toast.makeText(getApplicationContext(), "Logged in as " + role, Toast.LENGTH_SHORT).show();
                                //Send user to different activities based on role
                                if ("driver".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(LoginActivity.this, DriverMainActivity.class));
                                } else if ("student".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(LoginActivity.this, StudentMainActivity.class));
                                } else {
                                    Toast.makeText(this, "Unknown role.", Toast.LENGTH_SHORT).show();
                                }
                                // End current activity to prevent returning on back press
                                finish();
                            }
                        });
                    } else { //If wrong login details
                        Toast.makeText(getApplicationContext(), "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //move to register activity if register button clicked
    public void openRegister(View v){
        Intent i = new Intent(this,RegisterActivity.class);
        startActivity(i);
    }
}