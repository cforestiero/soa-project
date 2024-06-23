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
import android.widget.TextView;
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

    private static final String LIGHTS = "L";
    private static final String CHANGE_COLOUR = "C";
    private static final String SWITCH_MODE = "W";

    private static final String PREFS_NAME = "LightActivityPrefs";
    private static final String SWITCH_STATE_KEY = "switch_state";
    private Boolean switchDefaultValue = false;
    private Boolean justSinchronizeSwitch = false;


    private static final String SELECTED_COLOR_KEY = "selected_color";

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

    @SuppressLint("CutPasteId")
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

        layout = findViewById(R.id.linearlayout);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accelerometerEventListener = new AccelerometerEventListener(layout);
        }

        // Buttons
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the color
                selectedColor = ((ColorDrawable) layout.getBackground()).getColor();
                Toast.makeText(LightActivity.this, "Color seleccionado guardado", Toast.LENGTH_SHORT).show();

                imageView.setColorFilter(selectedColor);

                int red = Color.red(selectedColor);
                int green = Color.green(selectedColor);
                int blue = Color.blue(selectedColor);
                String colorData = CHANGE_COLOUR + " " + red + " " + green + " " + blue + "\n";
                bluetoothManager.sendCommand(colorData);

                // Save the color to SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(SELECTED_COLOR_KEY, selectedColor);
                editor.apply();
            }
        });
        // Color elements
        switchPower = findViewById(R.id.switch1);
        textView = findViewById(R.id.textView);
        linearlayout = findViewById(R.id.linearlayout);
        imageView = findViewById(R.id.imageView);

        // Load the switch state and selected color from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean switchState = preferences.getBoolean(SWITCH_STATE_KEY, switchDefaultValue);
        selectedColor = preferences.getInt(SELECTED_COLOR_KEY, 0); // Default color 0 (usually black)
        switchPower.setChecked(switchState);

        // Si no esta seleccionado escondo todo lo de los colores
        if (!switchState) {
            textView.setVisibility(View.INVISIBLE);
            linearlayout.setVisibility(View.INVISIBLE);
            btnConfirm.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
        bluetoothManager.sendCommand(LIGHTS);

        // Set the initial color of the ImageView if a color was previously selected
        if (selectedColor != 0) {
            // Cambiar el color de fondo del layout
            layout.setBackgroundColor(selectedColor);
            // Cambiar el color del ImageView
            imageView.setColorFilter(selectedColor);
            // Enviar comando de color al Arduino
            int red = Color.red(selectedColor);
            int green = Color.green(selectedColor);
            int blue = Color.blue(selectedColor);
            String colorData = CHANGE_COLOUR + " " + red + " " + green + " " + blue + "\n";
            bluetoothManager.sendCommand(colorData); // Enviar el RGB
        }

        // Set the listener for the Switch
        switchPower.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
            textView.setVisibility(visibility);
            linearlayout.setVisibility(visibility);
            btnConfirm.setVisibility(visibility);
            imageView.setVisibility(visibility);

            // Si no necesita solo sincronizar el switch de la app
            // le envio el comando para que se apague o se prenda tambien
            if (!justSinchronizeSwitch)
                bluetoothManager.sendCommand(SWITCH_MODE);

            // Save the switch state to SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(SWITCH_STATE_KEY, isChecked);
            editor.apply();
        });
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
        // Load the switch state and selected color from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean switchState = preferences.getBoolean(SWITCH_STATE_KEY, switchDefaultValue);
        selectedColor = preferences.getInt(SELECTED_COLOR_KEY, 0); // Default color 0 (usually black)
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
            // Si el evento es por sensor entonces no hago nada
            if (receivedMessage.contains("Evento: LOW_LIGHT") ||
                    receivedMessage.contains("Evento: MEDIUM_LIGHT") ||
                    receivedMessage.contains("Evento: HIGH_LIGHT")) {
                // Sincronizar el switch de la app pero sin cambiar en el circuito
                justSinchronizeSwitch = true;
                return;
            }
            // Sino me fijo el modo
            if (receivedMessage.contains("DAY")) {
                // Si esta en modo dia la luz se apaga
                switchPower.setChecked(false);
                // Cambia el default para cuando inicia la pantalla de nuevo
                switchDefaultValue = false;
                Log.d("HandleMessageModoDia", "Received message: " + receivedMessage);
            } else {
                // Sino esta en modo noche y la luz se prende
                switchPower.setChecked(true);
                // Cambia el default para cuando inicia la pantalla de nuevo
                switchDefaultValue = true;
                Log.d("HandleMessageModoNoche", "Received message: " + receivedMessage);
            }
        }
    };

}
