package com.example.smartpool;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GyroscopeEventListener implements SensorEventListener {

    private TextView layout;

    public GyroscopeEventListener(TextView layout) {
        this.layout = layout;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Read the sensor data
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Change background color based on the sensor data
            int red = (int) Math.abs(x * 255 / Math.PI);
            int green = (int) Math.abs(y * 255 / Math.PI);
            int blue = (int) Math.abs(z * 255 / Math.PI);

            int color = Color.rgb(red, green, blue);
            layout.setBackgroundColor(color);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if sensor accuracy changes
    }
}

