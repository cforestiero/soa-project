package com.example.smartpool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LightActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "LightActivityPrefs";
    private static final String SWITCH_STATE_KEY = "switch_state";
    private static final String SELECTED_COLOR_KEY = "selected_color";

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private AccelerometerEventListener accelerometerEventListener;
    private LinearLayout layout;
    private int selectedColor = 0;
    private Switch switch1;
    private View textView;
    private View linearlayout;
    private View btnConfirm;
    private ImageView imageView; // Asegúrate de que es un ImageView

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

        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the color
                selectedColor = ((ColorDrawable) layout.getBackground()).getColor();
                Toast.makeText(LightActivity.this, "Color seleccionado guardado", Toast.LENGTH_SHORT).show();

                // Cambiar el color del VectorDrawable
                imageView.setColorFilter(selectedColor); // Cambiar el color del ImageView

                // Save the color to SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(SELECTED_COLOR_KEY, selectedColor);
                editor.apply();
            }
        });

        // Find the Switch
        switch1 = findViewById(R.id.switch1);
        // Find the components to make invisible
        textView = findViewById(R.id.textView);
        linearlayout = findViewById(R.id.linearlayout);
        imageView = findViewById(R.id.imageView); // Casting a ImageView

        // Load the switch state and selected color from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean switchState = preferences.getBoolean(SWITCH_STATE_KEY, false);
        selectedColor = preferences.getInt(SELECTED_COLOR_KEY, 0); // Default color 0 (usually black)
        switch1.setChecked(switchState);

        // Set initial visibility based on the Switch state
        int initialVisibility = switchState ? View.VISIBLE : View.INVISIBLE;
        textView.setVisibility(initialVisibility);
        linearlayout.setVisibility(initialVisibility);
        btnConfirm.setVisibility(initialVisibility);
        imageView.setVisibility(initialVisibility);

        // Set the initial color of the ImageView if a color was previously selected
        if (selectedColor != 0) {
            layout.setBackgroundColor(selectedColor); // Cambiar el color de fondo del layout
            imageView.setColorFilter(selectedColor); // Cambiar el color del ImageView
        }

        // Set the listener for the Switch
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
            textView.setVisibility(visibility);
            linearlayout.setVisibility(visibility);
            btnConfirm.setVisibility(visibility);
            imageView.setVisibility(visibility);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerSensor != null) {
            sensorManager.unregisterListener(accelerometerEventListener);
        }
    }
}
