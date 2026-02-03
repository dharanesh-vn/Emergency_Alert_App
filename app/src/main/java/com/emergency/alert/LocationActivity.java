package com.emergency.alert;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {

    private TextView tvLatitude, tvLongitude, tvAddress, tvAccuracy;
    private Button btnRefresh, btnCopy, btnOpenMaps, btnShareContacts;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;

    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private static final String TAG = "LocationActivity";
    private static final int SMS_PERMISSION_REQUEST = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupListeners();
        getCurrentLocation();
    }

    private void initializeViews() {
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        tvAddress = findViewById(R.id.tv_address);
        tvAccuracy = findViewById(R.id.tv_accuracy);

        btnRefresh = findViewById(R.id.btn_refresh_location);
        btnCopy = findViewById(R.id.btn_copy_location);
        btnOpenMaps = findViewById(R.id.btn_open_maps);
        btnShareContacts = findViewById(R.id.btn_share_to_contacts);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> getCurrentLocation());
        btnCopy.setOnClickListener(v -> copyLocationToClipboard());
        btnOpenMaps.setOnClickListener(v -> openInGoogleMaps());
        btnShareContacts.setOnClickListener(v -> shareLocationWithContacts());
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        tvLatitude.setText("📍 Latitude: Getting location...");
        tvLongitude.setText("📍 Longitude: Getting location...");
        tvAddress.setText("📫 Address: Loading...");
        tvAccuracy.setText("🎯 Accuracy: --");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        updateLocationDisplay(location);
                        getAddressFromLocation(location);
                    } else {
                        Toast.makeText(this, "Unable to get location. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        tvLatitude.setText("📍 Latitude: Not available");
                        tvLongitude.setText("📍 Longitude: Not available");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateLocationDisplay(Location location) {
        tvLatitude.setText(String.format(Locale.US, "📍 Latitude: %.6f", location.getLatitude()));
        tvLongitude.setText(String.format(Locale.US, "📍 Longitude: %.6f", location.getLongitude()));
        tvAccuracy.setText(String.format(Locale.US, "🎯 Accuracy: ±%.1f meters", location.getAccuracy()));
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder fullAddress = new StringBuilder("📫 Address:\n");

                if (address.getFeatureName() != null)
                    fullAddress.append(address.getFeatureName()).append(", ");
                if (address.getLocality() != null)
                    fullAddress.append(address.getLocality()).append(", ");
                if (address.getAdminArea() != null)
                    fullAddress.append(address.getAdminArea()).append(", ");
                if (address.getCountryName() != null)
                    fullAddress.append(address.getCountryName());

                tvAddress.setText(fullAddress.toString());
            } else {
                tvAddress.setText("📫 Address: Not available");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
            tvAddress.setText("📫 Address: Unable to retrieve");
        }
    }

    private void copyLocationToClipboard() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            Toast.makeText(this, "No location to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationText = String.format(Locale.US,
                "My Location:\nLatitude: %.6f\nLongitude: %.6f\nGoogle Maps: https://maps.google.com/?q=%.6f,%.6f",
                currentLatitude, currentLongitude, currentLatitude, currentLongitude);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Location", locationText);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "✅ Location copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void openInGoogleMaps() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            Toast.makeText(this, "No location available", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = String.format(Locale.US, "geo:%.6f,%.6f?q=%.6f,%.6f(My Location)",
                currentLatitude, currentLongitude, currentLatitude, currentLongitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // If Google Maps not installed, open in browser
            String browserUri = String.format(Locale.US,
                    "https://maps.google.com/?q=%.6f,%.6f",
                    currentLatitude, currentLongitude);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

    private void shareLocationWithContacts() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            Toast.makeText(this, "No location available. Please refresh.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DatabaseHelper.EmergencyContact> contacts = dbHelper.getAllEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts found. Please add contacts first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Check SMS permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission required. Requesting...",
                    Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST);
            return;
        }

        String locationMessage = String.format(Locale.US,
                "📍 I'm sharing my location with you:\n\nLatitude: %.6f\nLongitude: %.6f\n\nOpen in Google Maps:\nhttps://maps.google.com/?q=%.6f,%.6f",
                currentLatitude, currentLongitude, currentLatitude, currentLongitude);

        int successCount = 0;
        int failCount = 0;

        Log.d(TAG, "Attempting to send location to " + contacts.size() + " contacts");

        for (DatabaseHelper.EmergencyContact contact : contacts) {
            try {
                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();

                // Split long messages if needed
                ArrayList<String> parts = smsManager.divideMessage(locationMessage);

                Log.d(TAG, "Sending to: " + contact.name + " (" + contact.phone + "), Parts: " + parts.size());

                if (parts.size() > 1) {
                    // Send multi-part SMS
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null);
                } else {
                    // Send single SMS
                    smsManager.sendTextMessage(contact.phone, null, locationMessage, null, null);
                }

                successCount++;
                Log.d(TAG, "✅ Location sent to: " + contact.name + " (" + contact.phone + ")");

            } catch (SecurityException e) {
                failCount++;
                Log.e(TAG, "❌ SMS permission denied for " + contact.name, e);
            } catch (Exception e) {
                failCount++;
                Log.e(TAG, "❌ Failed to send to " + contact.name + " (" + contact.phone + ")", e);
            }
        }

        if (successCount > 0 && failCount == 0) {
            Toast.makeText(this,
                    "✅ Location shared with all " + successCount + " contact(s)!",
                    Toast.LENGTH_LONG).show();
        } else if (successCount > 0 && failCount > 0) {
            Toast.makeText(this,
                    "⚠️ Sent to " + successCount + " contacts. Failed: " + failCount,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "❌ Failed to send location to any contacts!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ SMS permission granted! Try sharing again.",
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "SMS permission granted");
            } else {
                Toast.makeText(this, "❌ SMS permission denied. Cannot share location via SMS.",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "SMS permission denied");
            }
        }
    }
}