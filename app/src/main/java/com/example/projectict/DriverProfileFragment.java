package com.example.projectict;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverProfileFragment extends Fragment {

    private TextView textName, textEmail, textId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_profile, container, false);

        // Bind text views (make sure IDs match your XML)
        textName = view.findViewById(R.id.tvName);
        textEmail = view.findViewById(R.id.tvEmail);
        textId = view.findViewById(R.id.tvId);

        loadDriverData();

        return view;
    }

    private void loadDriverData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_profiles").child(uid);

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Profile profile = snapshot.getValue(Profile.class);
                if (profile != null) {
                    textName.setText(profile.name);
                    textEmail.setText(profile.email);
                    textId.setText(profile.studentId);
                }
            }
        });
    }
}
