package com.example.smartpool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ConfigurationsFilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurations_filter);

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

        // Encuentra las referencias de los botones de incremento y decremento
        Button incrementButton = findViewById(R.id.incrementButton);
        Button decrementButton = findViewById(R.id.decrementButton);

        // Establece un listener para el NumberPicker para actualizar el TextView cuando cambie
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            // Actualiza el TextView con el nuevo valor del NumberPicker
            textViewNumber.setText(String.valueOf(newVal));
        });

        // Establece un listener para el botón de incremento
        incrementButton.setOnClickListener(v -> {
            // Incrementa el valor del NumberPicker
            numberPicker.setValue(numberPicker.getValue() + 1);
        });

        // Establece un listener para el botón de decremento
        decrementButton.setOnClickListener(v -> {
            // Decrementa el valor del NumberPicker
            numberPicker.setValue(numberPicker.getValue() - 1);
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
}
