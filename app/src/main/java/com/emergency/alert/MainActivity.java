package com.emergency.alert;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private TextView tvWelcome, tvStatus;
    private Button btnEmergencyMode, btnSOS, btnCallEmergency;
    private Button btnProfile, btnContacts, btnLocation, btnCalculator, btnWebInfo, btnImageEditor;

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private boolean isEmergencyMode = false;

    private static final int PERMISSION_REQUEST = 100;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate started");

        try {
            dbHelper = new DatabaseHelper(this);
            Log.d(TAG, "Database initialized");
        } catch (Exception e) {
            Log.e(TAG, "Database initialization failed", e);
        }

        prefs = getSharedPreferences("EmergencyAlertPrefs", MODE_PRIVATE);

        initializeViews();
        requestPermissions();
        setupListeners();
        verifyDatabase();

        Log.d(TAG, "onCreate completed");
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews started");

        mainLayout = findViewById(R.id.main_layout);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvStatus = findViewById(R.id.tv_status);

        btnEmergencyMode = findViewById(R.id.btn_emergency_mode);
        btnSOS = findViewById(R.id.btn_sos);
        btnCallEmergency = findViewById(R.id.btn_call_emergency);

        btnProfile = findViewById(R.id.btn_profile);
        btnContacts = findViewById(R.id.btn_contacts);
        btnLocation = findViewById(R.id.btn_location);
        btnCalculator = findViewById(R.id.btn_calculator);
        btnWebInfo = findViewById(R.id.btn_web_info);
        btnImageEditor = findViewById(R.id.btn_image_editor);

        tvWelcome.setText("Welcome, User!");
        tvStatus.setText("Status: Normal");

        Log.d(TAG, "initializeViews completed");
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners started");

        btnEmergencyMode.setOnClickListener(v -> toggleEmergencyMode());
        btnSOS.setOnClickListener(v -> sendSOS());
        btnCallEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnContacts.setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsActivity.class)));
        btnCalculator.setOnClickListener(v -> startActivity(new Intent(this, CalculatorActivity.class)));
        btnWebInfo.setOnClickListener(v -> startActivity(new Intent(this, WebViewActivity.class)));
        btnImageEditor.setOnClickListener(v -> startActivity(new Intent(this, ImageEditorActivity.class)));
        btnLocation.setOnClickListener(v -> startLocationTracking());

        // Safety Tips button - SAFE VERSION
        try {
            Button btnSafetyTips = findViewById(R.id.btn_safety_tips);
            if (btnSafetyTips != null) {
                btnSafetyTips.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Safety Tips button clicked");
                        try {
                            Intent intent = new Intent(MainActivity.this, SafetyTipsActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening SafetyTipsActivity", e);
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Log.d(TAG, "Safety Tips button listener set");
            } else {
                Log.w(TAG, "Safety Tips button not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Safety Tips button", e);
        }

        // Database Test button - SAFE VERSION
        try {
            Button btnDatabaseTest = findViewById(R.id.btn_database_test);
            if (btnDatabaseTest != null) {
                btnDatabaseTest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Database Test button clicked");
                        try {
                            Intent intent = new Intent(MainActivity.this, DatabaseTestActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening DatabaseTestActivity", e);
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Log.d(TAG, "Database Test button listener set");
            } else {
                Log.w(TAG, "Database Test button not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Database Test button", e);
        }

        Log.d(TAG, "setupListeners completed");
    }

    private void toggleEmergencyMode() {
        isEmergencyMode = !isEmergencyMode;

        if (isEmergencyMode) {
            mainLayout.setBackgroundColor(Color.RED);
            tvStatus.setText("⚠️ EMERGENCY MODE ACTIVE");
            startLocationTracking();

            Intent i = new Intent(this, MediaService.class);
            i.setAction("PLAY_SIREN");
            startService(i);

        } else {
            mainLayout.setBackgroundColor(Color.WHITE);
            tvStatus.setText("Status: Normal");

            stopService(new Intent(this, LocationTrackingService.class));

            Intent i = new Intent(this, MediaService.class);
            i.setAction("STOP_SIREN");
            startService(i);
        }
    }

    private void sendSOS() {
        if (dbHelper == null) {
            Toast.makeText(this, "Database not available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DatabaseHelper.EmergencyContact> contacts = dbHelper.getAllEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts", Toast.LENGTH_SHORT).show();
            return;
        }

        for (DatabaseHelper.EmergencyContact c : contacts) {
            try {
                android.telephony.SmsManager.getDefault()
                        .sendTextMessage(c.phone, null,
                                "⚠️ EMERGENCY! Please help!", null, null);
            } catch (Exception e) {
                Log.e(TAG, "Error sending SMS to " + c.phone, e);
            }
        }

        Toast.makeText(this, "SOS sent to " + contacts.size() + " contacts", Toast.LENGTH_SHORT).show();
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

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.POST_NOTIFICATIONS
                }, PERMISSION_REQUEST);
    }

    private void verifyDatabase() {
        if (dbHelper == null) {
            Log.e(TAG, "Database is null");
            Toast.makeText(this, "Database not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int tipCount = dbHelper.getAllSafetyTips().size();
            int contactCount = dbHelper.getAllEmergencyContacts().size();

            Log.d(TAG, "✅ Database connected!");
            Log.d(TAG, "   Safety Tips: " + tipCount);
            Log.d(TAG, "   Contacts: " + contactCount);

            Toast.makeText(this, "DB Ready: " + tipCount + " tips, " + contactCount + " contacts",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "❌ Database error", e);
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}