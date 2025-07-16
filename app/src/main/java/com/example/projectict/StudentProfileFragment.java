package com.example.projectict;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentProfileFragment extends Fragment {

    private TextView nameTextView, emailTextView, studentIdTextView;
    private ImageView profileImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_profile, container, false);

        // Bind views (ensure these IDs match your XML)
        nameTextView = view.findViewById(R.id.tvName);
        emailTextView = view.findViewById(R.id.tvEmail);
        studentIdTextView = view.findViewById(R.id.tvId);
        profileImage = view.findViewById(R.id.profileImage);
        loadProfileFromFirebase();

        LinearLayout changePasswordBtn = view.findViewById(R.id.btnChangePassword);
        LinearLayout logoutBtn = view.findViewById(R.id.btnLogout);

        // Log Out btn
        logoutBtn.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Change Password (Forgot Password Email)
        changePasswordBtn.setOnClickListener(v -> {
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Reset link sent to email", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e  -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        return view;

    }
    /**
     * Loads the current student's profile data from Firebase
     */
    private void loadProfileFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_profiles").child(uid);
        // Read data
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Profile profile = snapshot.getValue(Profile.class);
                if (profile != null) {
                    // Update text to user information
                    nameTextView.setText(profile.name);
                    emailTextView.setText(profile.email);
                    studentIdTextView.setText(profile.studentId);
                    // Load profile image if available
                    if (profile.imageUrl != null && !profile.imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profile.imageUrl)
                                .placeholder(R.drawable.profile_pic)
                                .into(profileImage);
                    }
                }
                else {
                    Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
