package com.emergency.alert;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private TextView tvWelcome, tvStatus;
    private Button btnEmergencyMode, btnSOS, btnCallEmergency, btnSafetyTips;
    private Button btnProfile, btnContacts, btnLocation, btnCalculator, btnWebInfo, btnImageEditor;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private String currentUsername;
    private boolean isEmergencyMode = false;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EmergencyAlertPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "User");

        initializeViews();
        requestPermissions();
        applyUserTheme();
        setupListeners();
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.main_layout);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvStatus = findViewById(R.id.tv_status);
        btnEmergencyMode = findViewById(R.id.btn_emergency_mode);
        btnSOS = findViewById(R.id.btn_sos);
        btnCallEmergency = findViewById(R.id.btn_call_emergency);
        btnSafetyTips = findViewById(R.id.btn_safety_tips);
        btnProfile = findViewById(R.id.btn_profile);
        btnContacts = findViewById(R.id.btn_contacts);
        btnLocation = findViewById(R.id.btn_location);
        btnCalculator = findViewById(R.id.btn_calculator);
        btnWebInfo = findViewById(R.id.btn_web_info);
        btnImageEditor = findViewById(R.id.btn_image_editor);

        tvWelcome.setText("Welcome, " + currentUsername + "!");
    }

    private void requestPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS,
                        PERMISSION_REQUEST_CODE
                );
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "All permissions are required for emergency features",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    private void applyUserTheme() {
        DatabaseHelper.UserProfile profile = dbHelper.getUserProfile(currentUsername);
        if (profile != null) {
            try {
                mainLayout.setBackgroundColor(Color.parseColor(profile.bgColor));
                tvWelcome.setTextSize(profile.fontSize);
                tvStatus.setTextSize(profile.fontSize - 2);
            } catch (Exception ignored) {}
        }
    }

    private void setupListeners() {

        btnEmergencyMode.setOnClickListener(v -> toggleEmergencyMode());

        btnSOS.setOnClickListener(v -> sendSOS());

        btnCallEmergency.setOnClickListener(v -> callEmergencyContact());

        btnSafetyTips.setOnClickListener(v ->
                startActivity(new Intent(this, SafetyTipsActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnContacts.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyContactsActivity.class)));

        btnLocation.setOnClickListener(v -> startLocationTracking());

        btnCalculator.setOnClickListener(v ->
                startActivity(new Intent(this, CalculatorActivity.class)));

        btnWebInfo.setOnClickListener(v ->
                startActivity(new Intent(this, WebViewActivity.class)));

        btnImageEditor.setOnClickListener(v ->
                startActivity(new Intent(this, ImageEditorActivity.class)));
    }

    private void toggleEmergencyMode() {
        isEmergencyMode = !isEmergencyMode;

        if (isEmergencyMode) {
            mainLayout.setBackgroundColor(Color.RED);
            tvStatus.setText("⚠️ EMERGENCY MODE ACTIVATED ⚠️");
            tvStatus.setTextColor(Color.WHITE);
            btnEmergencyMode.setText("Exit Emergency Mode");

            startLocationTracking();

            Intent sirenIntent = new Intent(this, MediaService.class);
            sirenIntent.setAction("PLAY_SIREN");
            startService(sirenIntent);

            Toast.makeText(this, "Emergency Mode Activated", Toast.LENGTH_SHORT).show();

        } else {
            applyUserTheme();
            tvStatus.setText("Status: Normal");
            tvStatus.setTextColor(Color.BLACK);
            btnEmergencyMode.setText("Activate Emergency Mode");

            Intent sirenIntent = new Intent(this, MediaService.class);
            sirenIntent.setAction("STOP_SIREN");
            startService(sirenIntent);

            stopService(new Intent(this, LocationTrackingService.class));

            Toast.makeText(this, "Emergency Mode Deactivated", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSOS() {
        List<DatabaseHelper.EmergencyContact> contacts =
                dbHelper.getAllEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(this,
                    "No emergency contacts found",
                    Toast.LENGTH_LONG).show();
            return;
        }

        for (DatabaseHelper.EmergencyContact c : contacts) {
            try {
                android.telephony.SmsManager.getDefault()
                        .sendTextMessage(
                                c.phone,
                                null,
                                "⚠️ EMERGENCY! I need help immediately.",
                                null,
                                null
                        );
            } catch (Exception ignored) {}
        }

        NotificationHelper.showEmergencyNotification(
                this,
                "SOS Sent",
                "SOS sent to " + contacts.size() + " contacts"
        );
    }

    private void callEmergencyContact() {
        List<DatabaseHelper.EmergencyContact> contacts =
                dbHelper.getAllEmergencyContacts();

        String number = contacts.isEmpty() ? "911" : contacts.get(0).phone;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {

            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + number)));
        }
    }

    private void startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        Intent intent = new Intent(this, LocationTrackingService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
