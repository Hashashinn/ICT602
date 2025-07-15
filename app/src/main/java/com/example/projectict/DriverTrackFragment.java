package com.example.projectict;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DriverTrackFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_track, container, false);

        view.findViewById(R.id.btnScanStart).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DriverScanActivity.class);
            startActivity(intent);
        });

        return view;
    }
}