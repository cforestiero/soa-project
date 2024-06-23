package com.example.smartpool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PUMP_MODE = "B";
    private BluetoothManager bluetoothManager;

    // TODO: Agregar mensajes del handler como constantes

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
        bluetoothManager.sendCommand(PUMP_MODE);

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
        bluetoothManager.sendCommand(PUMP_MODE);
    }

    final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("MainActivity", "Received message: " + receivedMessage);
            // Handle the received message
            if (receivedMessage.contains("Estado Final: FILTERING") || receivedMessage.contains("B,Filtrado")) {
                // Si la bomba esta en modo filtrado entonces se esconde el boton de desagote
                buttonDewater.setVisibility(View.GONE);
            } else {
                buttonDewater.setVisibility(View.VISIBLE);
            }

            if(!receivedMessage.contains("B,Filtrado") && !receivedMessage.contains("B,Drenaje")){
                // Si no entro por modo de bomba, quiere decir que estoy manejando otro tipo de mensaje
                // Entonces checkeo que haya un proceso en curso
                if (receivedMessage.contains("PROCESS")) {
                    // Si la bomba esta andando
                    if (receivedMessage.contains("Estado Final: FILTERING")) {
                        // Y esta filtrando
                        rectanglePumpActionTextView.setText(R.string.filteringProcessMessage);
                    } else {
                        // Sino, esta drenando
                        rectanglePumpActionTextView.setText(R.string.dewateringProcessMessage);
                    }
                    rectanglePumpActionTextView.setVisibility(TextView.VISIBLE);
                } else {
                    // Si la bomba no esta haciendo nada entonces se esconde el mensaje
                    rectanglePumpActionTextView.setVisibility(TextView.GONE);
                }
            }

            if (receivedMessage.contains("Estado Final: FILTERING_PROCESS")) {
                // Guarda la fecha de filtrado
                SharedPreferences.Editor editor = getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(Common.FILTER_TIME_KEY, Common.getCurrentDateTime());
                editor.apply();
            }
        }
    };
}