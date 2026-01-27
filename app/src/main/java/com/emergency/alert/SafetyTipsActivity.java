package com.emergency.alert;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SafetyTipsActivity extends AppCompatActivity {

    private ListView lvSafetyTips;
    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.SafetyTip> tipsList;
    private ArrayAdapter<String> adapter;
    private List<String> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_tips);

        dbHelper = new DatabaseHelper(this);
        tipsList = new ArrayList<>();
        displayList = new ArrayList<>();

        initializeViews();
        loadSafetyTips();
        setupListeners();
    }

    private void initializeViews() {
        lvSafetyTips = findViewById(R.id.lv_safety_tips);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        lvSafetyTips.setAdapter(adapter);
    }

    private void loadSafetyTips() {
        tipsList = dbHelper.getAllSafetyTips();
        displayList.clear();

        for (DatabaseHelper.SafetyTip tip : tipsList) {
            displayList.add("[" + tip.category + "] " + tip.title);
        }

        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        lvSafetyTips.setOnItemClickListener((parent, view, position, id) -> {
            DatabaseHelper.SafetyTip tip = tipsList.get(position);
            showTipDetails(tip);
        });
    }

    private void showTipDetails(DatabaseHelper.SafetyTip tip) {
        new AlertDialog.Builder(this)
                .setTitle(tip.title)
                .setMessage(tip.content)
                .setPositiveButton("OK", null)
                .show();
    }
}
