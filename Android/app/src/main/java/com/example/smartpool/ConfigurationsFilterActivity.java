package com.example.smartpool;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

public class ConfigurationsFilterActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurations_filter);

        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        NumberPicker numberPicker = findViewById(R.id.numberPicker);
        Button incrementButton = findViewById(R.id.incrementButton);
        Button decrementButton = findViewById(R.id.decrementButton);

        numberPicker.setMinValue(Constants.MIN_FILTER_HOURS);
        numberPicker.setMaxValue(Constants.MAX_FILTER_HOURS);
        numberPicker.setValue(Constants.DEFAULT_FILTER_HOURS);

        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            numberPicker.setValue(newVal);
        });

        incrementButton.setOnClickListener(v -> {
            int currentVal = numberPicker.getValue();
            if (currentVal < numberPicker.getMaxValue()) {
                numberPicker.setValue(++currentVal);
            }
        });

        decrementButton.setOnClickListener(v -> {
            int currentVal = numberPicker.getValue();
            if (currentVal > numberPicker.getMinValue()) {
                numberPicker.setValue(--currentVal);
            }
        });

        Button confirmButton = findViewById(R.id.confirmFilter);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int filterHours = numberPicker.getValue();
                String command = String.format("%s %d",Constants.FILTER_SCHEDULE, Common.hoursToMilliseconds(filterHours));
                bluetoothManager.sendCommand(command);

                Intent intent = new Intent(ConfigurationsFilterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
