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

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        NumberPicker numberPicker = findViewById(R.id.numberPicker);
        Button incrementButton = findViewById(R.id.incrementButton);
        Button decrementButton = findViewById(R.id.decrementButton);

        // Set valor maximo y minimo para seleccionar las horas en el numberPicker
        numberPicker.setMinValue(Constants.MIN_FILTER_HOURS);
        numberPicker.setMaxValue(Constants.MAX_FILTER_HOURS);
        numberPicker.setValue(Constants.DEFAULT_FILTER_HOURS); // Set valor inicial

        // Establece un listener para el NumberPicker para actualizar el valor del numberPicker
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            numberPicker.setValue(newVal);
        });

        // Establece un listener para el botón de incremento(+)
        incrementButton.setOnClickListener(v -> {
            // Incrementa el valor del NumberPicker
            int currentVal = numberPicker.getValue();
            if (currentVal < numberPicker.getMaxValue()) {
                numberPicker.setValue(++currentVal);
            }
        });

        // Establece un listener para el botón de decremento(-)
        decrementButton.setOnClickListener(v -> {
            // Decrementa el valor del NumberPicker
            int currentVal = numberPicker.getValue();
            if (currentVal > numberPicker.getMinValue()) {
                numberPicker.setValue(--currentVal);
            }
        });

        Button confirmButton = findViewById(R.id.confirmFilter);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Envio del dato que selecciono el usuario al Arduino
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
