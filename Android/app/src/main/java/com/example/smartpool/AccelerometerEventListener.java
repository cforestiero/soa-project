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
            float x = event.values[Constants.X_AXIS];
            float y = event.values[Constants.Y_AXIS];
            float z = event.values[Constants.Z_AXIS];

            // Calculate color based on accelerometer values
            int red = (int) Math.abs(x * Constants.RGB_MAX_VALUE / Constants.EARTHS_GRAVITY); // Normalized to Earth's gravity
            int green = (int) Math.abs(y * Constants.RGB_MAX_VALUE / Constants.EARTHS_GRAVITY);
            int blue = (int) Math.abs(z * Constants.RGB_MAX_VALUE / Constants.EARTHS_GRAVITY);

            // Ensure the values are within 0-255 range
            red = Math.min(Constants.RGB_MAX_VALUE, red);
            green = Math.min(Constants.RGB_MAX_VALUE, green);
            blue = Math.min(Constants.RGB_MAX_VALUE, blue);

            // Set the background color
            int color = Color.rgb(red, green, blue);
            layout.setBackgroundColor(color);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
