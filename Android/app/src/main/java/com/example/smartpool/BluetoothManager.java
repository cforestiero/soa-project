package com.example.smartpool;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class BluetoothManager {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final String DEVICE_ADDRESS = "98:D3:31:F6:A0:71"; // Reemplazar con la dirección MAC del módulo Bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothManager instance;
    private OutputStream outputStream;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BufferedReader bufferedReader;
    private Handler handler;
    private WeakReference<Context> contextWeakReference;

    private BluetoothManager(WeakReference<Context> contextWeakReference, Activity activity) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
            if (ActivityCompat.checkSelfPermission(contextWeakReference.get(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSION);
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
            outputStream = bluetoothSocket.getOutputStream();

            Log.d("BluetoothManager", "Bluetooth connection established");

        } catch (IOException e) {
            Log.e("BluetoothManager", "Error establishing Bluetooth connection", e);
            e.printStackTrace();
        }
    }

    public static BluetoothManager getInstance(WeakReference<Context> contextWeakReference, Activity activity) {
        if (instance == null) {
            instance = new BluetoothManager(contextWeakReference, activity);
        }
        return instance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
        new ConnectedThread().start();
    }

    public void setContext(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    private class ConnectedThread extends Thread {
        public void run() {
            Log.d("BluetoothManager", "ConnectedThread started");
            while (true) {
                try {
                    if(bufferedReader != null){
                        String data = bufferedReader.readLine();
                        if (data != null && handler != null) {
                            Log.d("BluetoothManager", "Received data: " + data);
                            Message msg = handler.obtainMessage(0, data);
                            Log.d("RECIBO", "Data recibida: " + msg);
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    Log.e("BluetoothManager", "Error reading data", e);
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public void sendCommand(String command) {
        try {
            if (outputStream != null) {
                outputStream.write(command.getBytes());
                Log.d("ENVIO", "Data sent: " + command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
