package com.emergency.alert;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
    private Button btnDatabaseTest; // ✅ ADDED - Database test button

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private boolean isEmergencyMode = false;

    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("EmergencyAlertPrefs", MODE_PRIVATE);

        initializeViews();
        requestPermissions();
        setupListeners();

        // ✅ ADDED - Verify database connection
        verifyDatabase();
    }

    private void initializeViews() {
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

        // ✅ ADDED - Initialize database test button
        btnDatabaseTest = findViewById(R.id.btn_database_test);

        tvWelcome.setText("Welcome, User!");
        tvStatus.setText("Status: Normal");
    }

    private void setupListeners() {

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

        // ✅ ADDED - Database test button listener
        if (btnDatabaseTest != null) {
            btnDatabaseTest.setOnClickListener(v ->
                    startActivity(new Intent(this, DatabaseTestActivity.class)));
        }
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
        List<DatabaseHelper.EmergencyContact> contacts = dbHelper.getAllEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts", Toast.LENGTH_SHORT).show();
            return;
        }

        for (DatabaseHelper.EmergencyContact c : contacts) {
            android.telephony.SmsManager.getDefault()
                    .sendTextMessage(c.phone, null,
                            "⚠️ EMERGENCY! Please help!", null, null);
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

    // ✅ ADDED - Verify database is working
    private void verifyDatabase() {
        try {
            int tipCount = dbHelper.getAllSafetyTips().size();
            int contactCount = dbHelper.getAllEmergencyContacts().size();

            android.util.Log.d("MainActivity", "✅ Database connected!");
            android.util.Log.d("MainActivity", "   Safety Tips: " + tipCount);
            android.util.Log.d("MainActivity", "   Contacts: " + contactCount);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "❌ Database error: " + e.getMessage());
        }
    }
}