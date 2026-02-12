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

        btnRefresh.setOnClickListener(v -> displayDatabaseStatus());
        btnAddTestData.setOnClickListener(v -> insertTestData());

        displayDatabaseStatus();
    }

    private void displayDatabaseStatus() {

        StringBuilder sb = new StringBuilder();

        sb.append("DATABASE STATUS\n");
        sb.append("================================\n");
        sb.append("Database Name : EmergencyAlert.db\n");
        sb.append("Package       : com.emergency.alert\n\n");

        // Safety Tips
        List<DatabaseHelper.SafetyTip> tips = dbHelper.getAllSafetyTips();
        sb.append("SAFETY TIPS TABLE\n");
        sb.append("Records: ").append(tips.size()).append("\n\n");

        for (DatabaseHelper.SafetyTip tip : tips) {
            sb.append("ID       : ").append(tip.id).append("\n");
            sb.append("Title    : ").append(tip.title).append("\n");
            sb.append("Category : ").append(tip.category).append("\n\n");
        }

        // Contacts
        List<DatabaseHelper.EmergencyContact> contacts =
                dbHelper.getAllEmergencyContacts();

        sb.append("--------------------------------\n");
        sb.append("EMERGENCY CONTACTS TABLE\n");
        sb.append("Records: ").append(contacts.size()).append("\n\n");

        for (DatabaseHelper.EmergencyContact c : contacts) {
            sb.append("ID       : ").append(c.id).append("\n");
            sb.append("Name     : ").append(c.name).append("\n");
            sb.append("Phone    : ").append(c.phone).append("\n");
            sb.append("Relation : ").append(c.relation).append("\n\n");
        }

        // Events
        List<DatabaseHelper.EmergencyEvent> events =
                dbHelper.getAllEmergencyEvents();

        sb.append("--------------------------------\n");
        sb.append("EMERGENCY EVENTS TABLE\n");
        sb.append("Records: ").append(events.size()).append("\n\n");

        for (DatabaseHelper.EmergencyEvent e : events) {
            sb.append("ID       : ").append(e.id).append("\n");
            sb.append("Type     : ").append(e.eventType).append("\n");
            sb.append("Date     : ").append(e.eventDate).append("\n");
            sb.append("Location : ").append(e.location).append("\n\n");
        }

        sb.append("================================\n");
        sb.append("DATABASE CONNECTION: OK\n");

        tvResults.setText(sb.toString());
    }

    private void insertTestData() {

        long contactId = dbHelper.addEmergencyContact(
                "Database Test",
                "9999999999",
                "Test User"
        );

        long eventId = dbHelper.addEmergencyEvent(
                "Test Event",
                "Unknown",
                "Test record insertion"
        );

        Toast.makeText(this,
                "Test records inserted\nContact ID: " + contactId +
                        "\nEvent ID: " + eventId,
                Toast.LENGTH_LONG).show();

        displayDatabaseStatus();
    }
}
