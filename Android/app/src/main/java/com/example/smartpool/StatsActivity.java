package com.example.smartpool;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Simulate receiving data from sensors or other sources
        double temperature = getTemperatureFromSensor();
        double waterLevel = getWaterLevelFromSensor();
        String filterLastTime = getFilterLastTime();
        String drainingLastTime = getDrainingLastTime();

        // Find the TextViews and set the text
        TextView temperatureTextView = findViewById(R.id.temperatureTextView);
        TextView waterLevelTextView = findViewById(R.id.WaterLevelTextView);
        TextView filterLastTimeTextView = findViewById(R.id.FilterLastTimeTextView);
        TextView drainingLastTimeTextView = findViewById(R.id.DrainingLastTimeTextView);

        temperatureTextView.setText("Temperatura: " + temperature + "°C");
        waterLevelTextView.setText("Nivel de Agua: " + waterLevel + "%");
        filterLastTimeTextView.setText("Sé filtro por ultima vez el: " + filterLastTime);
        drainingLastTimeTextView.setText("Sé desagoto por ultima vez el: " + drainingLastTime);
    }

    // Simulated methods for getting data from sensors
    private double getTemperatureFromSensor() {
        // Replace with actual sensor data retrieval
        return 25.5;
    }

    private double getWaterLevelFromSensor() {
        // Replace with actual sensor data retrieval
        return 75.0;
    }

    private String getFilterLastTime() {
        // Replace with actual data retrieval
        return "2024-06-01";
    }

    private String getDrainingLastTime() {
        // Replace with actual data retrieval
        return "2024-06-15";
    }
}
