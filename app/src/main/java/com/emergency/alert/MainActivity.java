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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
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
    private static final int SMS_PERMISSION_REQUEST = 200;
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

        // Safety Tips button
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

        // Database Test button
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

        // View Location button
        try {
            Button btnViewLocation = findViewById(R.id.btn_view_location);
            if (btnViewLocation != null) {
                btnViewLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "View Location button clicked");
                        try {
                            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening LocationActivity", e);
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Log.d(TAG, "View Location button listener set");
            } else {
                Log.w(TAG, "View Location button not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up View Location button", e);
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
            Toast.makeText(this, "No emergency contacts. Add contacts first!", Toast.LENGTH_LONG).show();
            return;
        }

        // Check SMS permission first
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission required. Requesting...", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST);
            return;
        }

        // Get current location and send SOS with location
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        String sosMessage;
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            sosMessage = String.format(java.util.Locale.US,
                                    "🚨 EMERGENCY! I need help!\n\n" +
                                            "My Location:\nhttps://maps.google.com/?q=%.6f,%.6f\n\n" +
                                            "Please respond immediately!",
                                    lat, lng);
                            Log.d(TAG, "SOS with location: " + lat + ", " + lng);
                        } else {
                            sosMessage = "🚨 EMERGENCY! I need help!\n\nLocation unavailable. Please call me immediately!";
                            Log.d(TAG, "SOS without location");
                        }

                        sendSOSMessages(contacts, sosMessage);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                        String sosMessage = "🚨 EMERGENCY! I need help!\n\nPlease call me immediately!";
                        sendSOSMessages(contacts, sosMessage);
                    });
        } else {
            String sosMessage = "🚨 EMERGENCY! I need help!\n\nPlease call me immediately!";
            sendSOSMessages(contacts, sosMessage);
        }
    }

    private void sendSOSMessages(List<DatabaseHelper.EmergencyContact> contacts, String message) {
        // Double-check SMS permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "❌ SMS permission denied. Cannot send messages.",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "SMS permission not granted");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        Log.d(TAG, "Attempting to send SOS to " + contacts.size() + " contacts");

        for (DatabaseHelper.EmergencyContact c : contacts) {
            try {
                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();

                // Split long messages if needed
                ArrayList<String> parts = smsManager.divideMessage(message);

                Log.d(TAG, "Sending SMS to: " + c.name + " (" + c.phone + "), Parts: " + parts.size());

                if (parts.size() > 1) {
                    // Send multi-part SMS
                    smsManager.sendMultipartTextMessage(c.phone, null, parts, null, null);
                } else {
                    // Send single SMS
                    smsManager.sendTextMessage(c.phone, null, message, null, null);
                }

                successCount++;
                Log.d(TAG, "✅ SOS sent successfully to: " + c.name + " (" + c.phone + ")");

            } catch (SecurityException e) {
                failCount++;
                Log.e(TAG, "❌ SMS permission denied for " + c.name, e);
                Toast.makeText(this, "SMS permission denied!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                failCount++;
                Log.e(TAG, "❌ Error sending SMS to " + c.name + " (" + c.phone + ")", e);
            }
        }

        // Show detailed result
        if (successCount > 0 && failCount == 0) {
            Toast.makeText(this, "✅ SOS sent to all " + successCount + " contact(s)!",
                    Toast.LENGTH_LONG).show();
        } else if (successCount > 0 && failCount > 0) {
            Toast.makeText(this, "⚠️ Sent to " + successCount + " contacts. Failed: " + failCount,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "❌ Failed to send SOS to any contacts!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, LocationTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST) {
            // Check which permissions were granted
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.SEND_SMS)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "✅ SMS permission granted");
                    } else {
                        Log.e(TAG, "❌ SMS permission denied");
                    }
                }
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ SMS permission granted! Try sending SOS again.",
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "SMS permission granted via request");
            } else {
                Toast.makeText(this, "❌ SMS permission denied. Cannot send SOS messages.",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "SMS permission denied via request");
            }
        }
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