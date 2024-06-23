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
import java.util.Arrays;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private static final String STATS = "I";
    private BluetoothManager bluetoothManager;

    private TextView temperatureTextView;
    private TextView waterLevelTextView;
    private TextView filterLastTimeTextView;
    private TextView drainingLastTimeTextView;

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
            // Parse Message
            String[] lines = receivedMessage.split(",");
            for (String line : lines) {
                if (line.startsWith("Temperatura del Agua:")) {
                    temperatureTextView.setText(line);
                } else if (line.startsWith("Distancia del Agua:")) {
                    // Split words to set level
                    String[] words = line.split(" ");
                    String waterDistance = words[3]; // en este indice tenemos la distancia
                    if (Integer.parseInt(waterDistance) < 100) {
                        waterLevelTextView.setText(R.string.waterLevelHigh);
                    } else {
                        waterLevelTextView.setText(R.string.waterLevelLow);
                    }
                    // TODO: si esto anda, borrar este comentario
                    //waterLevelTextView.setText(line);
                }
            }
            // Obtener ultimo desagote
            SharedPreferences preferences = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
            drainingLastTime = preferences.getString(Common.DEWATER_TIME_KEY, "");
            drainingLastTimeTextView.setText(getString(R.string.lastDewaterTimeLabel) + drainingLastTime);

            // Obtener ultimo filtrado
            filterLastTime = preferences.getString(Common.FILTER_TIME_KEY, "");
            filterLastTimeTextView.setText(getString(R.string.lastFilterTimeLabel) + filterLastTime);
        }
    };

}
