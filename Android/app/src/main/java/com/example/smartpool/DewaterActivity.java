package com.example.smartpool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DewaterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dewater);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button confirmButton = findViewById(R.id.button2);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar código para realizar alguna acción antes de volver a MainActivity
                // Por ejemplo, guardar datos, realizar operaciones, etc.

                // Luego, puedes volver a MainActivity
                Intent intent = new Intent(DewaterActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Esto evita que la actividad actual quede en el stack
            }
        });

    }
}