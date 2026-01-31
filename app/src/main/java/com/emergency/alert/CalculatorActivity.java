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
        updateInputLabels();
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

        tvResult.setText(getString(R.string.result_placeholder));
    }

    private void setupListeners() {
        rgCalculatorType.setOnCheckedChangeListener((group, checkedId) -> updateInputLabels());
        btnCalculate.setOnClickListener(v -> performCalculation());
    }

    private void updateInputLabels() {
        int selectedId = rgCalculatorType.getCheckedRadioButtonId();

        etInput3.setVisibility(View.GONE);
        tvLabel3.setVisibility(View.GONE);

        if (selectedId == R.id.rb_distance) {
            tvLabel1.setText(getString(R.string.lat_1));
            tvLabel2.setText(getString(R.string.lon_1));
            tvLabel3.setText(getString(R.string.lat_2));
            etInput3.setVisibility(View.VISIBLE);
            tvLabel3.setVisibility(View.VISIBLE);

        } else if (selectedId == R.id.rb_travel_time) {
            tvLabel1.setText(getString(R.string.distance_km));
            tvLabel2.setText(getString(R.string.speed_kmh));

        } else if (selectedId == R.id.rb_dosage) {
            tvLabel1.setText(getString(R.string.age_years));
            tvLabel2.setText(getString(R.string.weight_kg));

        } else if (selectedId == R.id.rb_battery) {
            tvLabel1.setText(getString(R.string.battery_percent));
            tvLabel2.setText(getString(R.string.usage_rate));
        }

        etInput1.setText("");
        etInput2.setText("");
        etInput3.setText("");
        tvResult.setText(getString(R.string.result_placeholder));
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
            Toast.makeText(this, getString(R.string.invalid_numbers), Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateDistance() {
        double lat1 = Double.parseDouble(etInput1.getText().toString());
        double lon1 = Double.parseDouble(etInput2.getText().toString());

        double lat2 = lat1 + 0.1;
        double lon2 = lon1 + 0.1;

        double distance = Math.sqrt(
                Math.pow(lat2 - lat1, 2) +
                        Math.pow(lon2 - lon1, 2)
        ) * 111;

        tvResult.setText(String.format("Distance to nearest hospital:\n%.2f km", distance));
    }

    private void calculateTravelTime() {
        double distance = Double.parseDouble(etInput1.getText().toString());
        double speed = Double.parseDouble(etInput2.getText().toString());

        if (speed == 0) {
            Toast.makeText(this, getString(R.string.speed_zero), Toast.LENGTH_SHORT).show();
            return;
        }

        double time = distance / speed;
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);

        tvResult.setText("Estimated travel time:\n" + hours + "h " + minutes + "m");
    }

    private void calculateDosage() {
        int age = Integer.parseInt(etInput1.getText().toString());
        double weight = Double.parseDouble(etInput2.getText().toString());

        double dosage = weight * 0.15;
        if (age < 12) dosage *= 0.5;
        if (age > 65) dosage *= 0.75;

        tvResult.setText("Recommended dosage:\n" + String.format("%.2f mg", dosage));
    }

    private void calculateBatteryTime() {
        double battery = Double.parseDouble(etInput1.getText().toString());
        double rate = Double.parseDouble(etInput2.getText().toString());

        if (rate == 0) {
            Toast.makeText(this, getString(R.string.usage_zero), Toast.LENGTH_SHORT).show();
            return;
        }

        double hoursLeft = battery / rate;
        int hours = (int) hoursLeft;
        int minutes = (int) ((hoursLeft - hours) * 60);

        tvResult.setText("Battery remaining:\n" + hours + "h " + minutes + "m");
    }
}
