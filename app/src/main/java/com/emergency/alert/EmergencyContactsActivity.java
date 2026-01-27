package com.emergency.alert;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText etContactName, etContactPhone, etContactRelation;
    private Button btnAddContact;
    private ListView lvContacts;

    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.EmergencyContact> contactsList;
    private ArrayAdapter<String> adapter;
    private List<String> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        dbHelper = new DatabaseHelper(this);
        contactsList = new ArrayList<>();
        displayList = new ArrayList<>();

        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts(); // ensures refresh after add/delete
    }

    private void initializeViews() {
        etContactName = findViewById(R.id.et_contact_name);
        etContactPhone = findViewById(R.id.et_contact_phone);
        etContactRelation = findViewById(R.id.et_contact_relation);
        btnAddContact = findViewById(R.id.btn_add_contact);
        lvContacts = findViewById(R.id.lv_contacts);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayList
        );
        lvContacts.setAdapter(adapter);
    }

    private void loadContacts() {
        contactsList = dbHelper.getAllEmergencyContacts();
        displayList.clear();

        if (contactsList.isEmpty()) {
            displayList.add("No emergency contacts added");
        } else {
            for (DatabaseHelper.EmergencyContact contact : contactsList) {
                displayList.add(
                        contact.name + "\n" +
                                contact.phone + " (" + contact.relation + ")"
                );
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {

        btnAddContact.setOnClickListener(v -> addContact());

        lvContacts.setOnItemClickListener((parent, view, position, id) -> {
            if (!contactsList.isEmpty()) {
                Toast.makeText(
                        this,
                        "Long press to delete contact",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        lvContacts.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!contactsList.isEmpty()) {
                showDeleteDialog(position);
            }
            return true;
        });
    }

    private void addContact() {
        String name = etContactName.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();
        String relation = etContactRelation.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || relation.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("^[+]?[0-9]{10,15}$")) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        for (DatabaseHelper.EmergencyContact c : contactsList) {
            if (c.phone.equals(phone)) {
                Toast.makeText(this, "Contact already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        long result = dbHelper.addEmergencyContact(name, phone, relation);

        if (result != -1) {
            Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
            etContactName.setText("");
            etContactPhone.setText("");
            etContactRelation.setText("");
            loadContacts();
        } else {
            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDialog(final int position) {
        DatabaseHelper.EmergencyContact contact = contactsList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage(
                        "Delete this emergency contact?\n\n" +
                                contact.name + "\n" + contact.phone
                )
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteEmergencyContact(contact.id)) {
                        Toast.makeText(
                                EmergencyContactsActivity.this,
                                "Contact deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadContacts();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
