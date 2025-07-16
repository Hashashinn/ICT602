package com.example.projectict;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity {

    EditText editName, editStudentID, editEmail, editPassword, editConfirmPassword;
    Button btnRegister;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    private ImageView profileImage;
    private Button btnSelectImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        FirebaseApp.initializeApp(this); // Explicit init

        // Init Firebase services
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("user_profiles");

        // Bind views
        editName = findViewById(R.id.editName);
        editStudentID = findViewById(R.id.editStudentID);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());

        //ProfileImageInput
        profileImage = findViewById(R.id.profileImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });
    }

    private void registerUser() {
        String name = editName.getText().toString().trim();
        String studentId = editStudentID.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(studentId) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + uid + ".jpg");

                        if (imageUri != null) {
                            storageRef.putFile(imageUri)
                                    .continueWithTask(task1 -> storageRef.getDownloadUrl())
                                    .addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        Profile profile = new Profile(uid, name, studentId, email, imageUrl);
                                        saveProfileToDatabase(profile, uid);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // No image selected, store null or default
                            Profile profile = new Profile(uid, name, studentId, email, null);
                            saveProfileToDatabase(profile, uid);
                        }

                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveProfileToDatabase(Profile profile, String uid) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("user_profiles").child(uid).setValue(profile);
        rootRef.child("users").child(uid).child("role").setValue("student");

        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
