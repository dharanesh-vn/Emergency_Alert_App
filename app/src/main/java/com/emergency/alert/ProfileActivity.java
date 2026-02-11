package com.emergency.alert;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout rootLayout;
    private TextView tvUsername, tvFontSizeValue;
    private EditText etBgColor;
    private SeekBar seekBarFontSize;
    private Button btnSaveTheme, btnLogout;

    private static final String PREFS = "theme_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rootLayout = findViewById(R.id.root_layout);
        tvUsername = findViewById(R.id.tv_username);
        tvFontSizeValue = findViewById(R.id.tv_font_size_value);
        etBgColor = findViewById(R.id.et_bg_color);
        seekBarFontSize = findViewById(R.id.seekbar_font_size);
        btnSaveTheme = findViewById(R.id.btn_save_theme);
        btnLogout = findViewById(R.id.btn_logout);

        loadTheme();

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = progress + 10;
                tvFontSizeValue.setText(size + "sp");
                tvUsername.setTextSize(size);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSaveTheme.setOnClickListener(v -> saveTheme());
        btnLogout.setOnClickListener(v -> finish());
    }

    private void saveTheme() {
        String colorText = etBgColor.getText().toString().trim();

        try {
            int color = Color.parseColor(colorText);

            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            prefs.edit()
                    .putString("bg_color", colorText)
                    .putInt("font_size", seekBarFontSize.getProgress())
                    .apply();

            rootLayout.setBackgroundColor(color);
            Toast.makeText(this, "Theme saved", Toast.LENGTH_SHORT).show();

        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Invalid color", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        String color = prefs.getString("bg_color", "#FFFFFF");
        int size = prefs.getInt("font_size", 6);

        try {
            rootLayout.setBackgroundColor(Color.parseColor(color));
            etBgColor.setText(color);
        } catch (Exception ignored) {}

        seekBarFontSize.setProgress(size);
        tvFontSizeValue.setText((size + 10) + "sp");
        tvUsername.setTextSize(size + 10);
    }
}
