package com.example.smartpool;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private static final String PUMP_MODE = "B";
    private static final String PUMP_WORKING = "W";

    private BluetoothManager bluetoothManager;

    Button buttonLights;
    Button buttonDewater;
    TextView rectanglePumpActionTextView;
    ImageView boy;

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

        ImageButton configButton = (ImageButton) findViewById(R.id.imageButton);
        buttonLights = findViewById(R.id.button);
        buttonDewater = findViewById(R.id.buttonDewater);
        rectanglePumpActionTextView = findViewById(R.id.rectangleTextView);

        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);
        bluetoothManager.setHandler(bluetoothIn);
        bluetoothManager.sendCommand(PUMP_MODE);
        Button buttonLights = (Button)findViewById(R.id.button);

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

    }

     final Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String receivedMessage = (String) msg.obj;
            Log.d("MainActivity", "Received message: " + receivedMessage);
            // Handle the received message
            if (receivedMessage.contains("Estado Final: FILTERING") || receivedMessage.contains("B, Filtrado")) {
                // Si la bomba esta en modo filtrado entonces se esconde el boton de desagote
                buttonDewater.setVisibility(View.GONE);
            } else {
                buttonDewater.setVisibility(View.VISIBLE);
            }

            if (receivedMessage.contains("PROCESS")) {
                // Si la bomba esta andando
                if (receivedMessage.contains("Estado Final: FILTERING")) {
                    // Y esta filtrando
                    rectanglePumpActionTextView.setText("Filtrando... ");
                } else {
                    // Sino, esta drenando
                    rectanglePumpActionTextView.setText("Desagotando... ");
                }
                rectanglePumpActionTextView.setVisibility(TextView.VISIBLE);
            } else {
                // Si la bomba no esta haciendo nada entonces se esconde el mensaje
                rectanglePumpActionTextView.setVisibility(TextView.GONE);
            }
        }};
    }


//            if (receivedMessage.contains(",")) {
//                String[] parts = receivedMessage.split(",", 2); // Split into two parts only
//                if (parts.length == 2) {
//                    String key = parts[0];
//                    String value = parts[1];
//                    Log.d("MainActivity", "Parsed key: " + key + ", value: " + value);
//                    // Handle the parsed key-value pair
//                    handleParsedKeyValue(key, value);
//                } else {
//                    Log.e("MainActivity", "Received message format is incorrect");
//                }
//            } else {
//                Log.e("MainActivity", "Received message does not contain a comma");
//            }

 //       }
   // };

//    private void handleParsedKeyValue(String key, String value) {
//        switch (key) {
//            case PUMP_MODE:
//                Log.d("MainActivity", "Handling PUMP_MODE with value: " + value);
//                if (value.contains("FILTERING_MODE")){
//                    // Si la bomba esta en modo filtrado entonces se esconde el boton de desagote
//                    buttonDewater.setVisibility(View.GONE);
//                }
//                break;
//            case PUMP_WORKING:
//                Log.d("MainActivity", "Handling PUMP_WORKING with value: " + value);
//                if (value.contains("PROCESS")){
//                    rectanglePumpActionTextView.setText(value);
//                    rectanglePumpActionTextView.setVisibility(TextView.VISIBLE);
//                } else {
//                    rectanglePumpActionTextView.setVisibility(TextView.GONE);
//                }
//               // if (value.contains("Filtrando")){
//                 //   rectanglePumpActionTextView.setText(value);
//                   // rectanglePumpActionTextView.setVisibility(TextView.VISIBLE);
//                    // Si la bomba esta Filtrando se muestra un coso que dice filtrando
//                    //buttonDewater.setVisibility(View.);
//                //}
//                if (value.contains("NO")){
//                }
//                break;
//            default:
//                Log.d("MainActivity", "Unknown command: " + key + " with value: " + value);
//                //Toast.makeText(MainActivity.this, "Unknown command: " + key + " with value: " + value, Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }
//}