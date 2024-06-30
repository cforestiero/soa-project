package com.example.smartpool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference;

public class LightActivity extends AppCompatActivity {

    private Boolean switchDefaultValue = false;
    private Boolean justSinchronizeSwitch = false;

    private BluetoothManager bluetoothManager;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private AccelerometerEventListener accelerometerEventListener;

    private LinearLayout layout;
    private int selectedColor = 0;
    private Switch switchPower;
    private View textView;
    private View linearlayout;
    private View btnConfirm;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_light);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bluetooth
        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);
        bluetoothManager.setHandler(bluetoothIn);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Buttons
        btnConfirm = findViewById(R.id.btn_confirm);
        // Color elements
        switchPower = findViewById(R.id.switch1);
        textView = findViewById(R.id.textView);
        linearlayout = findViewById(R.id.linearlayout);
        imageView = findViewById(R.id.imageView);

        layout = findViewById(R.id.linearlayout);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accelerometerEventListener = new AccelerometerEventListener(layout);
        }

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = ((ColorDrawable) layout.getBackground()).getColor();
                Toast.makeText(LightActivity.this, R.string.selectedColourSavedMessage, Toast.LENGTH_SHORT).show();
                imageView.setColorFilter(selectedColor);
                sendColourToLed();
                // Save the color to SharedPreferences
                saveColour();
            }
        });

        // Load the switch state and selected color from SharedPreferences
        boolean switchState = loadSwitchPositionAndColour();
        switchPower.setChecked(switchState);
        // Si no esta seleccionado escondo todo lo de los colores
        if (!switchState) {
            setComponentsVisibility(View.INVISIBLE);
        }

        // Para saber la posicion inicial del embebido
        bluetoothManager.sendCommand(Constants.LIGHTS);

        // Set the initial color of the ImageView if a color was previously selected
        if (selectedColor != Constants.DEFAULT_COLOUR_BLACK) {
            // Cambiar el color de fondo del layout
            layout.setBackgroundColor(selectedColor);
            // Cambiar el color del ImageView
            imageView.setColorFilter(selectedColor);
            // Enviar comando de color al Arduino
            sendColourToLed();
        }

        // Set the listener for the Switch
        switchPower.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
            setComponentsVisibility(visibility);

            // Si no necesita solo sincronizar el switch de la app
            // le envio el comando para que se apague o se prenda tambien
            if (!justSinchronizeSwitch)
                bluetoothManager.sendCommand(Constants.SWITCH_LIGTHS_MODE);

            // Save the switch state to SharedPreferences
            saveSwitchState(isChecked);
        });
    }

    private void sendColourToLed() {
        int red = Color.red(selectedColor);
        int green = Color.green(selectedColor);
        int blue = Color.blue(selectedColor);
        String colorCommand = String.format("%s %d %d %d\n", Constants.CHANGE_COLOUR, red, green, blue);
        bluetoothManager.sendCommand(colorCommand);
    }

    private void setComponentsVisibility(int visibility) {
        textView.setVisibility(visibility);
        linearlayout.setVisibility(visibility);
        btnConfirm.setVisibility(visibility);
        imageView.setVisibility(visibility);
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.LIGHT_PREFS, MODE_PRIVATE).edit();
        editor.putBoolean(Constants.SWITCH_STATE_KEY, isChecked);
        editor.apply();
    }

    private void saveColour() {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.LIGHT_PREFS, MODE_PRIVATE).edit();
        editor.putInt(Constants.SELECTED_COLOR_KEY, selectedColor);
        editor.apply();
    }

    private boolean loadSwitchPositionAndColour() {
        // Load the switch state and selected color from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(Constants.LIGHT_PREFS, MODE_PRIVATE);
        boolean switchState = preferences.getBoolean(Constants.SWITCH_STATE_KEY, switchDefaultValue);
        selectedColor = preferences.getInt(Constants.SELECTED_COLOR_KEY, Constants.DEFAULT_COLOUR_BLACK); // Default color 0 (usually black)
        return switchState;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate back to the parent activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        boolean switchState = loadSwitchPositionAndColour();
        switchPower.setChecked(switchState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerSensor != null) {
            sensorManager.unregisterListener(accelerometerEventListener);
        }
    }

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("LightActivity", "Received message: " + receivedMessage);
            // Handle the received message
            String[] parts = receivedMessage.split(Constants.MESSAGE_SEPARATOR);

            switch (parts[Constants.MESSAGE_CODE]) {
                case Constants.LIGHTS:
                    handleLightModeChange(parts[Constants.CURRENT_STATE]);
                    break;
                case Constants.FINAL_STATE_CURRENT_EVENT_INFO:
                    handleEvent(parts[Constants.FINAL_STATE], parts[Constants.CURRENT_EVENT]);
                    break;
                default:
                    Log.d("LightActivity", "Unknown message: " + receivedMessage);
                    break;
            }
        }
    };

    private void handleLightModeChange(String currentState) {
        if (isDayMode(currentState)) {
            // Cambia el default para cuando inicia la pantalla de nuevo
            switchDefaultValue = false;
            // Si esta en modo dia la luz se apaga
            switchPower.setChecked(false);
        } else if (isNightMode(currentState)){
            // Cambia el default para cuando inicia la pantalla de nuevo
            switchDefaultValue = true;
            // Sino esta en modo noche y la luz se prende
            switchPower.setChecked(true);
        }
    }

    private void handleEvent(String finalState, String currentEvent) {
        // Si el evento es por sensor
        if (isLightEvent(currentEvent)) {
            // Sincronizar el switch de la app pero sin cambiar en el circuito
            justSinchronizeSwitch = true;
            return;
        }

        justSinchronizeSwitch = false;
        // Sino me fijo el modo
        if (isDayMode(finalState)) {
            // Cambia el default para cuando inicia la pantalla de nuevo
            switchDefaultValue = false;
            // Si esta en modo dia la luz se apaga
            switchPower.setChecked(false);
        } else if (isNightMode(finalState)){
            // Cambia el default para cuando inicia la pantalla de nuevo
            switchDefaultValue = true;
            // Sino esta en modo noche y la luz se prende
            switchPower.setChecked(true);
        }

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

    private boolean isLightEvent(String message) {
        return message.equals(Constants.EVENT_HIGH_LIGHT) ||
                message.equals(Constants.EVENT_MEDIUM_LIGHT) ||
                message.equals(Constants.EVENT_LOW_LIGHT);
    }

    private boolean isDayMode(String message) {
        return message.equals(Constants.STATE_FILTERING_PROCESS_DAY) ||
                message.equals(Constants.STATE_FILTERING_DAY_MODE) ||
                message.equals(Constants.STATE_DRAINING_PROCESS_DAY) ||
                message.equals(Constants.STATE_DRAINING_DAY_MODE);
    }

    private boolean isNightMode(String message) {
        return message.equals(Constants.STATE_FILTERING_PROCESS_NIGHT) ||
                message.equals(Constants.STATE_FILTERING_NIGHT_MODE) ||
                message.equals(Constants.STATE_DRAINING_PROCESS_NIGHT) ||
                message.equals(Constants.STATE_DRAINING_NIGHT_MODE);
    }
}
