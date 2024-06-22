package com.example.smartpool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.Button;

import androidx.annotation.NonNull;
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
        bluetoothManager.setHandler(bluetoothIn);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Encuentra la referencia del NumberPicker y del TextView
        NumberPicker numberPicker = findViewById(R.id.numberPicker);
        TextView textViewNumber = findViewById(R.id.textViewNumber);

        // Set the range for the NumberPicker
        numberPicker.setMinValue(0); // Set to your desired minimum value
        numberPicker.setMaxValue(100); // Set to your desired maximum value
        numberPicker.setValue(50); // Set to your desired initial value

        // Encuentra las referencias de los botones de incremento y decremento
        Button incrementButton = findViewById(R.id.incrementButton);
        Button decrementButton = findViewById(R.id.decrementButton);

        // Establece un listener para el NumberPicker para actualizar el TextView cuando cambie
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            numberPicker.setValue(newVal);
            Log.d("CAMBIO", "Received message: " + newVal);

            // Actualiza el TextView con el nuevo valor del NumberPicker
            //textViewNumber.setText(String.valueOf(newVal));
        });

        // Establece un listener para el botón de incremento
        incrementButton.setOnClickListener(v -> {
            // Incrementa el valor del NumberPicker
            Log.d("incremento", "Received message: " + (numberPicker.getValue() + 1));

            int currentVal = numberPicker.getValue();
            if (currentVal < numberPicker.getMaxValue()) {
                numberPicker.setValue(currentVal + 1);
            }
        });

        // Establece un listener para el botón de decremento
        decrementButton.setOnClickListener(v -> {
            // Decrementa el valor del NumberPicker
            Log.d("decremento", "Received message: " + (numberPicker.getValue() - 1));
            int currentVal = numberPicker.getValue();
            if (currentVal > numberPicker.getMinValue()) {
                numberPicker.setValue(currentVal - 1);
            }
        });

        Button confirmButton = findViewById(R.id.button2);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar código para realizar alguna acción antes de volver a MainActivity
                // Por ejemplo, guardar datos, realizar operaciones, etc.

                // Luego, puedes volver a MainActivity
                Intent intent = new Intent(ConfigurationsFilterActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Esto evita que la actividad actual quede en el stack
            }
        });
    }

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("ConfigurationFilterActivity", "Received message: " + receivedMessage);
            // Handle the received message

        }
    };
}
