package com.example.smartpool;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class BluetoothManager {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final String DEVICE_ADDRESS = "00:11:22:33:44:55"; // Reemplazar con la dirección MAC del módulo Bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothManager instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BufferedReader bufferedReader;
    private Handler handler;
    private WeakReference<Context> contextWeakReference;

    private BluetoothManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
//            if (ActivityCompat.checkSelfPermission(contextWeakReference.get(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                //ActivityCompat.requestPermissions(this,
//                //      new String[]{Manifest.permission.BLUETOOTH_CONNECT},
//                //    REQUEST_BLUETOOTH_PERMISSION);
//                return;
//            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
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
            while (true) {
                try {
                    if(bufferedReader != null){
                        String data = bufferedReader.readLine();
                        if (data != null && handler != null) {
                            Message msg = handler.obtainMessage(0, data);
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public void sendCommand(String command) {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.getOutputStream().write(command.getBytes());
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
