package com.example.smartpool;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AccelerometerEventListener implements SensorEventListener {

    private LinearLayout layout;

    public AccelerometerEventListener(LinearLayout layout) {
        this.layout = layout;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Read the sensor data
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate color based on accelerometer values
            int red = (int) Math.abs(x * 255 / 9.8); // Normalized to Earth's gravity
            int green = (int) Math.abs(y * 255 / 9.8);
            int blue = (int) Math.abs(z * 255 / 9.8);

            // Ensure the values are within 0-255 range
            red = Math.min(255, red);
            green = Math.min(255, green);
            blue = Math.min(255, blue);

            // Set the background color
            int color = Color.rgb(red, green, blue);
            layout.setBackgroundColor(color);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if sensor accuracy changes
    }
}
