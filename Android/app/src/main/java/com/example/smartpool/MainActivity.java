package com.example.smartpool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;

    Button buttonLights;
    Button buttonDewater;

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

        buttonLights = findViewById(R.id.button);
        buttonDewater = findViewById(R.id.buttonDewater);

        bluetoothManager = BluetoothManager.getInstance();
        bluetoothManager.setContext(this);
        bluetoothManager.setHandler(bluetoothIn);

        buttonLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LightActivity.class));
            }
        });
    }

    private final Handler bluetoothIn = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String data = (String) msg.obj;
            // Parse and update UI
            String[] parts = data.split(" ");
            for (String part : parts) {
                // Ver como viene el mensaje
                if (part.startsWith("F:")) { // Si tiene una F de filtrado o si no empieza con D
                    // Esconde el boton de drenado
                    buttonDewater.setVisibility(View.GONE);
                } else if (part.startsWith("D:")) {
                    // muestra el boton para drenar

                } else if (part.startsWith("A:")) {
                    // Aca estaria haciendo una accion y se muestra si filtra o drena

                }
            }
            return true;
        }
    });
}