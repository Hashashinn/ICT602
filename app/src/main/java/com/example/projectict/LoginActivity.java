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
        mAuth = FirebaseAuth.getInstance();
    }
    public void loginUser(View v) {
        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fields cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("role");

                        ref.get().addOnCompleteListener(roleTask -> {
                            if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                                String role = roleTask.getResult().getValue(String.class);

                                Toast.makeText(getApplicationContext(), "Logged in as " + role, Toast.LENGTH_SHORT).show();

                                if ("driver".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(LoginActivity.this, DriverMainActivity.class));
                                } else if ("student".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(LoginActivity.this, StudentMainActivity.class));
                                } else {
                                    Toast.makeText(this, "Unknown role.", Toast.LENGTH_SHORT).show();
                                }

                                finish();
                            } else {
                                Toast.makeText(this, "Failed to detect role.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void openRegister(View v){
        Intent i = new Intent(this,RegisterActivity.class);
        startActivity(i);
    }
}