package com.example.projectict;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverProfileFragment extends Fragment {

    private TextView textName, textEmail, textId;
    private ImageView profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.driver_profile, container, false);

        // Layout binding
        textName = view.findViewById(R.id.tvName);
        textEmail = view.findViewById(R.id.tvEmail);
        textId = view.findViewById(R.id.tvId);
        profileImage = view.findViewById(R.id.profileImage);

        // Load driver profile data from Firebase
        loadDriverData();

        // Bind logout and change password buttons
        LinearLayout changePasswordBtn = view.findViewById(R.id.btnChangePassword);
        LinearLayout logoutBtn = view.findViewById(R.id.btnLogout);
        // Handle password reset
        changePasswordBtn.setOnClickListener(v -> {
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Reset link sent to email", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        // Handle logout button
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

        //About US Button listener
        LinearLayout aboutUsBtn = view.findViewById(R.id.aboutUs);

        aboutUsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AboutUsActivity.class);
            intent.putExtra("source", "driver");
            startActivity(intent);
        });

        return view;
    }

//    Load the current user's profile data from Firebase Realtime Database
    private void loadDriverData() {
        // Get the current user UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Find user_profiles in database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_profiles").child(uid);
        // Fetch user data
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Profile profile = snapshot.getValue(Profile.class);
                if (profile != null) {
                    // Display set profile data in layout
                    textName.setText(profile.name);
                    textEmail.setText(profile.email);
                    textId.setText(profile.studentId);
                    // Load profile image if user upload image, otherwise show default profile picture
                    if (profile.imageUrl != null && !profile.imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profile.imageUrl)
                                .placeholder(R.drawable.profile_pic)
                                .into(profileImage);
                    }
                }
            }
        });
    }
}
