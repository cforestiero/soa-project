package com.example.smartpool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.lang.ref.WeakReference;

public class DewaterActivity extends AppCompatActivity
{

    private BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dewater);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button confirmButton = findViewById(R.id.confirmFilter);

        bluetoothManager = BluetoothManager.getInstance(new WeakReference<>(this), this);
        bluetoothManager.setContext(this);

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bluetoothManager.sendCommand(Constants.DEWATER_SIGNAL_READY);
                saveDewaterDate();

                Intent intent = new Intent(DewaterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveDewaterDate()
    {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.STATS_PREFS, MODE_PRIVATE).edit();
        editor.putString(Constants.DEWATER_TIME_KEY, Common.getCurrentDateTime());
        editor.apply();
    }
}