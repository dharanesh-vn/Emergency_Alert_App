package com.emergency.alert;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvFontSizeValue;
    private EditText etBgColor;
    private SeekBar seekBarFontSize;
    private Button btnSaveTheme, btnLogout;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EmergencyAlertPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "User");

        initializeViews();
        loadCurrentTheme();
        setupListeners();
    }

    private void initializeViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvFontSizeValue = findViewById(R.id.tv_font_size_value);
        etBgColor = findViewById(R.id.et_bg_color);
        seekBarFontSize = findViewById(R.id.seekbar_font_size);
        btnSaveTheme = findViewById(R.id.btn_save_theme);
        btnLogout = findViewById(R.id.btn_logout);

        tvUsername.setText("Profile: " + currentUsername);
    }

    private void loadCurrentTheme() {
        DatabaseHelper.UserProfile profile = dbHelper.getUserProfile(currentUsername);
        if (profile != null) {
            etBgColor.setText(profile.bgColor);
            seekBarFontSize.setProgress(profile.fontSize - 10);
            tvFontSizeValue.setText(profile.fontSize + "sp");
        }
    }

    private void setupListeners() {
        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = progress + 10;
                tvFontSizeValue.setText(fontSize + "sp");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSaveTheme.setOnClickListener(v -> saveTheme());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void saveTheme() {
        String bgColorInput = etBgColor.getText().toString().trim();
        int fontSize = seekBarFontSize.getProgress() + 10;

        int parsedColor;

        try {
            // ✅ Allows ALL Android-supported colors
            parsedColor = Color.parseColor(bgColorInput);
        } catch (IllegalArgumentException e) {
            Toast.makeText(
                    this,
                    "Invalid color. Examples: #FF0000, #FFF, red, blue, transparent",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Save normalized hex value
        String normalizedColor = String.format("#%06X", (0xFFFFFF & parsedColor));

        if (dbHelper.updateUserTheme(currentUsername, normalizedColor, fontSize)) {
            Toast.makeText(this, "Theme saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save theme", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
