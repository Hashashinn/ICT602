package com.example.projectict;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    private String source;  // either "student" or "driver"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus);

        // Retrieve source
        source = getIntent().getStringExtra("source");

        Button backButton = findViewById(R.id.backButton);
        String source = getIntent().getStringExtra("source");

        backButton.setOnClickListener(v -> {
            if ("student".equals(source)) {
                Intent intent = new Intent(this, StudentMainActivity.class);
                intent.putExtra("fragment", "profile");
                startActivity(intent);
                finish();
            } else if ("driver".equals(source)) {
                Intent intent = new Intent(this, DriverMainActivity.class);
                intent.putExtra("fragment", "profile");
                startActivity(intent);
                finish();
            }
        });
    }
}
