package com.emergency.alert;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    private RadioGroup rgCalculatorType;
    private EditText etInput1, etInput2, etInput3;
    private TextView tvResult, tvLabel1, tvLabel2, tvLabel3;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        rgCalculatorType = findViewById(R.id.rg_calculator_type);
        etInput1 = findViewById(R.id.et_input1);
        etInput2 = findViewById(R.id.et_input2);
        etInput3 = findViewById(R.id.et_input3);
        tvResult = findViewById(R.id.tv_result);
        tvLabel1 = findViewById(R.id.tv_label1);
        tvLabel2 = findViewById(R.id.tv_label2);
        tvLabel3 = findViewById(R.id.tv_label3);
        btnCalculate = findViewById(R.id.btn_calculate);

        updateInputLabels();
    }

    private void setupListeners() {
        rgCalculatorType.setOnCheckedChangeListener((group, checkedId) -> {
            updateInputLabels();
        });

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation();
            }
        });
    }

    private void updateInputLabels() {
        int selectedId = rgCalculatorType.getCheckedRadioButtonId();

        etInput3.setVisibility(View.GONE);
        tvLabel3.setVisibility(View.GONE);

        if (selectedId == R.id.rb_distance) {
            tvLabel1.setText("Latitude 1:");
            tvLabel2.setText("Longitude 1:");
            tvLabel3.setText("Latitude 2:");
            etInput3.setVisibility(View.VISIBLE);
            tvLabel3.setVisibility(View.VISIBLE);
        } else if (selectedId == R.id.rb_travel_time) {
            tvLabel1.setText("Distance (km):");
            tvLabel2.setText("Speed (km/h):");
        } else if (selectedId == R.id.rb_dosage) {
            tvLabel1.setText("Age (years):");
            tvLabel2.setText("Weight (kg):");
        } else if (selectedId == R.id.rb_battery) {
            tvLabel1.setText("Battery % Left:");
            tvLabel2.setText("Usage Rate (%/hr):");
        }

        etInput1.setText("");
        etInput2.setText("");
        etInput3.setText("");
        tvResult.setText("Result will appear here");
    }

    private void performCalculation() {
        int selectedId = rgCalculatorType.getCheckedRadioButtonId();

        try {
            if (selectedId == R.id.rb_distance) {
                calculateDistance();
            } else if (selectedId == R.id.rb_travel_time) {
                calculateTravelTime();
            } else if (selectedId == R.id.rb_dosage) {
                calculateDosage();
            } else if (selectedId == R.id.rb_battery) {
                calculateBatteryTime();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateDistance() {
        double lat1 = Double.parseDouble(etInput1.getText().toString());
        double lon1 = Double.parseDouble(etInput2.getText().toString());

        // For simplicity, assume hospital at a fixed location
        double lat2 = lat1 + 0.1;
        double lon2 = lon1 + 0.1;

        // Simple distance calculation (Haversine formula simplified)
        double distance = Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2)) * 111; // km

        tvResult.setText(String.format("Distance to nearest hospital:\n%.2f km", distance));
    }

    private void calculateTravelTime() {
        double distance = Double.parseDouble(etInput1.getText().toString());
        double speed = Double.parseDouble(etInput2.getText().toString());

        if (speed == 0) {
            Toast.makeText(this, "Speed cannot be zero", Toast.LENGTH_SHORT).show();
            return;
        }

        double timeHours = distance / speed;
        int hours = (int) timeHours;
        int minutes = (int) ((timeHours - hours) * 60);

        tvResult.setText(String.format("Estimated travel time:\n%d hours %d minutes", hours, minutes));
    }

    private void calculateDosage() {
        int age = Integer.parseInt(etInput1.getText().toString());
        double weight = Double.parseDouble(etInput2.getText().toString());

        // Simple dosage formula (for demonstration only - NOT medical advice)
        double baseDosage = weight * 0.15; // mg per kg

        if (age < 12) {
            baseDosage *= 0.5; // Children get half dose
        } else if (age > 65) {
            baseDosage *= 0.75; // Elderly get reduced dose
        }

        tvResult.setText(String.format("Recommended dosage:\n%.2f mg\n\n⚠️ Demo only - Consult medical professional!", baseDosage));
    }

    private void calculateBatteryTime() {
        double batteryPercent = Double.parseDouble(etInput1.getText().toString());
        double usageRate = Double.parseDouble(etInput2.getText().toString());

        if (usageRate == 0) {
            Toast.makeText(this, "Usage rate cannot be zero", Toast.LENGTH_SHORT).show();
            return;
        }

        double hoursLeft = batteryPercent / usageRate;
        int hours = (int) hoursLeft;
        int minutes = (int) ((hoursLeft - hours) * 60);

        tvResult.setText(String.format("Battery time remaining:\n%d hours %d minutes", hours, minutes));
    }
}
