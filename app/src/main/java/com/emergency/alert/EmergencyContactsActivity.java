package com.emergency.alert;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText etContactName, etContactPhone, etContactRelation;
    private Button btnAddContact;
    private ListView lvContacts;

    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.EmergencyContact> contactsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<String> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void initializeViews() {
        etContactName = findViewById(R.id.et_contact_name);
        etContactPhone = findViewById(R.id.et_contact_phone);
        etContactRelation = findViewById(R.id.et_contact_relation);
        btnAddContact = findViewById(R.id.btn_add_contact);
        lvContacts = findViewById(R.id.lv_contacts);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        lvContacts.setAdapter(adapter);
    }

    private void loadContacts() {
        try {
            contactsList = dbHelper.getAllEmergencyContacts();
        } catch (Exception e) {
            contactsList = new ArrayList<>();
        }

        displayList.clear();

        if (contactsList.isEmpty()) {
            displayList.add("No emergency contacts added");
        } else {
            for (DatabaseHelper.EmergencyContact c : contactsList) {
                displayList.add(c.name + "\n" + c.phone + " (" + c.relation + ")");
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        btnAddContact.setOnClickListener(v -> addContact());

        lvContacts.setOnItemLongClickListener((parent, view, pos, id) -> {
            if (!contactsList.isEmpty()) showDeleteDialog(pos);
            return true;
        });
    }

    private void addContact() {
        String name = etContactName.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();
        String relation = etContactRelation.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || relation.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long res = dbHelper.addEmergencyContact(name, phone, relation);
        if (res != -1) {
            etContactName.setText("");
            etContactPhone.setText("");
            etContactRelation.setText("");
            loadContacts();
        }
    }

    private void showDeleteDialog(int pos) {
        DatabaseHelper.EmergencyContact c = contactsList.get(pos);
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage(c.name + "\n" + c.phone)
                .setPositiveButton("Delete", (d, w) -> {
                    dbHelper.deleteEmergencyContact(c.id);
                    loadContacts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
