package com.example.smartpool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

public class DewaterActivity extends AppCompatActivity {

    private static final String DEWATER_SIGNAL_READY = "D";
    private BluetoothManager bluetoothManager;

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

        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);
        bluetoothManager.setHandler(bluetoothIn);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se envia command para que la bomba ande
                bluetoothManager.sendCommand(DEWATER_SIGNAL_READY);

                // Luego, puedes volver a MainActivity
                Intent intent = new Intent(DewaterActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Esto evita que la actividad actual quede en el stack
            }
        });
    }

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("DewaterActivity", "Received message: " + receivedMessage);

        }};

}