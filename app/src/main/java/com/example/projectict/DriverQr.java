package com.example.projectict;

import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.Intent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import java.util.List;

public class DriverQr extends AppCompatActivity{
    private static final String TAG = "Driver QR";
    private DecoratedBarcodeView barcodeView;
    private FrameLayout frameLayoutCamera;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Camera permission granted. Setting up scanner.");
                    setupAndStartScanner();
                } else {
                    Log.w(TAG, "Camera permission denied.");
                    Toast.makeText(this, "Camera permission is required to scan codes.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_qr);
        frameLayoutCamera = findViewById(R.id.frame_layout_camera);
        checkCameraPermission();
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupAndStartScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    private void setupAndStartScanner() {
        if (barcodeView == null) {
            barcodeView = new DecoratedBarcodeView(this);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            barcodeView.setLayoutParams(layoutParams);
            frameLayoutCamera.addView(barcodeView);
            barcodeView.decodeContinuous(barcodeCallback);
        }
        barcodeView.resume();
    }
    private BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) {
                return;
            }
            barcodeView.pause();
            String scannedData = result.getText();
            String formatName = result.getBarcodeFormat().toString();
            Log.v(TAG, "Scanned Result: " + scannedData);
            Log.v(TAG, "Barcode Format: " + formatName);
            AlertDialog.Builder builder = new AlertDialog.Builder(DriverQr.this);
            builder.setTitle("Scan Result");
            builder.setMessage("Value: " + scannedData + "\nFormat: " + formatName);
            builder.setPositiveButton("Start Tracking", (dialog, which) -> {
                dialog.dismiss();

                // Start DriverTrackingActivity and pass scannedDriverId
                Intent intent = new Intent(DriverQr.this, DriverTrackingActivity.class);
                intent.putExtra("driverId", scannedData);  // this is your driver ID
                startActivity(intent);
                finish();
            });
            builder.setCancelable(false);
            builder.create().show();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
// Optional: for visual feedback
        }
    };
    private void startScanner() {
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }
    private void pauseScanner() {
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        startScanner();
    }
    @Override
    public void onPause() {
        super.onPause();
        pauseScanner();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
            if (barcodeView.getParent() instanceof ViewGroup) {
                ((ViewGroup)
                        barcodeView.getParent()).removeView(barcodeView);
            }
        }
    }
}
