package com.example.smartpool;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

public class StatsActivity extends AppCompatActivity {

    private static final String STATS = "I";
    private static String PREFS_NAME = "StatsPrefs";
    private static String DEWATER_TIME_KEY = "LastDewaterTime";
    private BluetoothManager bluetoothManager;

    private TextView temperatureTextView;
    private TextView waterLevelTextView;
    private TextView filterLastTimeTextView;
    private TextView drainingLastTimeTextView;

    private double temperature;
    private double waterLevel;
    private String filterLastTime;
    private String drainingLastTime;

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

        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);
        bluetoothManager.setHandler(bluetoothIn);
        bluetoothManager.sendCommand(STATS);

        // Simulate receiving data from sensors or other sources
        temperature = getTemperatureFromSensor();
        waterLevel = getWaterLevelFromSensor();
        filterLastTime = getFilterLastTime();
        drainingLastTime = getDrainingLastTime();

        // Find the TextViews and set the text
        temperatureTextView = findViewById(R.id.temperatureTextView);
        waterLevelTextView = findViewById(R.id.WaterLevelTextView);
        filterLastTimeTextView = findViewById(R.id.FilterLastTimeTextView);
        drainingLastTimeTextView = findViewById(R.id.DrainingLastTimeTextView);
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

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("StatsActivity", "Received message: " + receivedMessage);

            // Parse Message

            // Obtener ultimo desagote
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            drainingLastTime = preferences.getString(DEWATER_TIME_KEY, "");

            // Set text
            temperatureTextView.setText("Temperatura: " + temperature + "°C");
            waterLevelTextView.setText("Nivel de Agua: " + waterLevel + "%");
            filterLastTimeTextView.setText("Se filtro por ultima vez el: " + filterLastTime);
            drainingLastTimeTextView.setText("Se desagoto por ultima vez el: " + drainingLastTime);
        }};

}
