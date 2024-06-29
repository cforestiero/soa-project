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

    private BluetoothManager bluetoothManager;

    private TextView temperatureTextView;
    private TextView waterLevelTextView;
    private TextView filterLastTimeTextView;
    private TextView drainingLastTimeTextView;

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
        bluetoothManager.sendCommand(Constants.STATS);

        temperatureTextView = findViewById(R.id.temperatureTextView);
        waterLevelTextView = findViewById(R.id.WaterLevelTextView);
        filterLastTimeTextView = findViewById(R.id.FilterLastTimeTextView);
        drainingLastTimeTextView = findViewById(R.id.DrainingLastTimeTextView);
    }

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("StatsActivity", "Received message: " + receivedMessage);
            String[] parts = receivedMessage.split(",");

            switch (parts[Constants.MESSAGE_CODE]) {
                case Constants.STATS:
                    handleInformation(parts[Constants.WATER_TEMPERATURE], parts[Constants.WATER_DISTANCE]);
                    break;
                case Constants.FINAL_STATE_CURRENT_EVENT_INFO:
                    handleEvent(parts[Constants.FINAL_STATE], parts[Constants.CURRENT_EVENT]);
                    break;
                default:
                    Log.d("StatsActivity", "Unknown message: " + receivedMessage);
                    break;
            }
        }

        private void handleInformation(String temperature, String distance) {
            temperatureTextView.setText(temperature);

            if (Float.parseFloat(distance) < Constants.WATER_LEVEL_TRESHOLD) {
                waterLevelTextView.setText(R.string.waterLevelHigh);
            } else {
                waterLevelTextView.setText(R.string.waterLevelLow);
            }

            // Obtener ultimo desagote
            SharedPreferences preferences = getSharedPreferences(Constants.STATS_PREFS, MODE_PRIVATE);
            String drainingLastTime = preferences.getString(Constants.DEWATER_TIME_KEY, "");
            drainingLastTimeTextView.setText(String.format("%s%s", getString(R.string.lastDewaterTimeLabel), drainingLastTime));

            // Obtener ultimo filtrado
            String filterLastTime = preferences.getString(Constants.FILTER_TIME_KEY, "");
            filterLastTimeTextView.setText(String.format("%s%s", getString(R.string.lastFilterTimeLabel), filterLastTime));
        }

        private void handleEvent(String finalState, String currentEvent) {
            if (isFilteringProcess(finalState)) {
                saveFilterDate();
            }
        }

        private boolean isFilteringProcess(String message) {
            return message.equals(Constants.STATE_FILTERING_PROCESS_DAY) ||
                    message.equals(Constants.STATE_FILTERING_PROCESS_NIGHT);
        }

        private void saveFilterDate() {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.STATS_PREFS, MODE_PRIVATE).edit();
            editor.putString(Constants.FILTER_TIME_KEY, Common.getCurrentDateTime());
            editor.apply();
        }
    };
}
