package com.example.smartpool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;
    Button buttonLights;
    Button buttonDewater;
    TextView rectanglePumpActionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton configButton = findViewById(R.id.imageButton);
        buttonLights = findViewById(R.id.button);
        buttonDewater = findViewById(R.id.buttonDewater);
        rectanglePumpActionTextView = findViewById(R.id.rectangleTextView);
        Button buttonStats = findViewById(R.id.buttonInfo);
        Button buttonDewater = findViewById(R.id.buttonDewater);

        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);
        bluetoothManager.setHandler(bluetoothIn);
        bluetoothManager.sendCommand(Constants.PUMP_MODE);

        buttonLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LightActivity.class));
            }
        });

        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ConfigurationsFilterActivity.class));
            }
        });

        buttonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            }
        });

        buttonDewater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DewaterActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothManager.sendCommand(Constants.PUMP_MODE);
    }

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("MainActivity", "Received message: " + receivedMessage);
            String[] parts = receivedMessage.split(",");

            switch (parts[Constants.MESSAGE_CODE]) {
                case Constants.PUMP_MODE:
                    handleModeChange(parts[Constants.CURRENT_PUMP_MODE]);
                    break;
                case Constants.FINAL_STATE_CURRENT_EVENT_INFO:
                    handleEvent(parts[Constants.FINAL_STATE], parts[Constants.CURRENT_EVENT]);
                    break;
                default:
                    Log.d("MainActivity", "Unknown message: " + receivedMessage);
                    break;
            }
        }

        private void handleModeChange(String mode) {
            if (isFilteringMode(mode)) {
                buttonDewater.setVisibility(View.GONE);
            } else {
                buttonDewater.setVisibility(View.VISIBLE);
            }
        }

        private void handleEvent(String finalState, String currentEvent) {
            if (isFilteringProcess(finalState)) {
                rectanglePumpActionTextView.setText(R.string.filteringProcessMessage);
                rectanglePumpActionTextView.setVisibility(TextView.VISIBLE);
                saveFilterDate();
                return;
            }
            if (isDewateringProcess(finalState)) {
                rectanglePumpActionTextView.setText(R.string.dewateringProcessMessage);
                rectanglePumpActionTextView.setVisibility(TextView.VISIBLE);
                return;
            }

            rectanglePumpActionTextView.setVisibility(TextView.GONE);
        }

        private boolean isFilteringMode(String message) {
            return message.equals(Constants.PUMP_MODE_FILTER);
        }

        private boolean isFilteringProcess(String message) {
            return message.equals(Constants.STATE_FILTERING_PROCESS_DAY) ||
                    message.equals(Constants.STATE_FILTERING_PROCESS_NIGHT);
        }

        private boolean isDewateringProcess(String message) {
            return message.equals(Constants.STATE_DRAINING_PROCESS_DAY) ||
                    message.equals(Constants.STATE_DRAINING_PROCESS_NIGHT);
        }

        private void saveFilterDate() {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.STATS_PREFS, MODE_PRIVATE).edit();
            editor.putString(Constants.FILTER_TIME_KEY, Common.getCurrentDateTime());
            editor.apply();
        }
    };
}