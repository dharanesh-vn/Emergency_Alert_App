package com.emergency.alert;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DatabaseTestActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView tvResults;
    private Button btnRefresh, btnAddTestData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_test);

        dbHelper = new DatabaseHelper(this);

        tvResults = findViewById(R.id.tvResults);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnAddTestData = findViewById(R.id.btnAddTestData);

        btnRefresh.setOnClickListener(v -> showDatabaseInfo());
        btnAddTestData.setOnClickListener(v -> addTestData());

        // Show data immediately
        showDatabaseInfo();
    }

    private void showDatabaseInfo() {
        StringBuilder result = new StringBuilder();
        result.append("═══════════════════════════════════\n");
        result.append("     DATABASE INSPECTION\n");
        result.append("═══════════════════════════════════\n\n");

        // Database Location
        result.append("📂 DATABASE FILE:\n");
        result.append("/data/data/com.emergency.alert/databases/\n");
        result.append("EmergencyAlert.db\n\n");

        // Safety Tips
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        result.append("💡 SAFETY TIPS\n");
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        List<DatabaseHelper.SafetyTip> tips = dbHelper.getAllSafetyTips();
        result.append("Total: ").append(tips.size()).append(" tips\n\n");

        for (DatabaseHelper.SafetyTip tip : tips) {
            result.append("• ").append(tip.title).append("\n");
            result.append("  Category: ").append(tip.category).append("\n");
            result.append("  ID: ").append(tip.id).append("\n\n");
        }

        // Emergency Contacts
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        result.append("📞 EMERGENCY CONTACTS\n");
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        List<DatabaseHelper.EmergencyContact> contacts = dbHelper.getAllEmergencyContacts();
        result.append("Total: ").append(contacts.size()).append(" contacts\n\n");

        if (contacts.isEmpty()) {
            result.append("(No contacts added yet)\n\n");
        } else {
            for (DatabaseHelper.EmergencyContact contact : contacts) {
                result.append("• ").append(contact.name).append("\n");
                result.append("  Phone: ").append(contact.phone).append("\n");
                result.append("  Relation: ").append(contact.relation).append("\n");
                result.append("  ID: ").append(contact.id).append("\n\n");
            }
        }

        // Emergency Events
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        result.append("🚨 EMERGENCY EVENTS\n");
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        List<DatabaseHelper.EmergencyEvent> events = dbHelper.getAllEmergencyEvents();
        result.append("Total: ").append(events.size()).append(" events\n\n");

        if (events.isEmpty()) {
            result.append("(No events logged yet)\n\n");
        } else {
            for (DatabaseHelper.EmergencyEvent event : events) {
                result.append("• ").append(event.eventType).append("\n");
                result.append("  Date: ").append(event.eventDate).append("\n");
                result.append("  Location: ").append(event.location).append("\n");
                result.append("  ID: ").append(event.id).append("\n\n");
            }
        }

        result.append("═══════════════════════════════════\n");
        result.append("✅ DATABASE CONNECTED & WORKING!\n");
        result.append("═══════════════════════════════════\n");

        tvResults.setText(result.toString());
    }

    private void addTestData() {
        // Add test contact
        long contactId = dbHelper.addEmergencyContact(
                "Test Contact",
                "9876543210",
                "Friend"
        );

        // Add test event
        long eventId = dbHelper.addEmergencyEvent(
                "Test Emergency",
                "Test Location",
                "Test notes"
        );

        Toast.makeText(this,
                "Test data added!\nContact ID: " + contactId + "\nEvent ID: " + eventId,
                Toast.LENGTH_LONG).show();

        // Refresh display
        showDatabaseInfo();
    }
}