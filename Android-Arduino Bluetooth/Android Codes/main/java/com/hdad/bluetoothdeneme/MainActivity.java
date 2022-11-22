package com.hdad.bluetoothdeneme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private final int REQUEST_BLUETOOTH_SCAN = 1;

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button discoverButton;
    Spinner spinner;
    EditText editText;
    TextView textView;

    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    BluetoothDevice theDevice;
    BluetoothSocket theBluetoothSocket = null;

    String[] bondedNames;
    Set<BluetoothDevice> bondedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        discoverButton = findViewById(R.id.discoverButton);
        editText = findViewById(R.id.editTextMessage);
        spinner = findViewById(R.id.spinner);
        textView = findViewById(R.id.bondedDeviceNameText);

        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        IntentFilter filterBond = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiverBond, filterBond);

        arrangeConnectableList();
    }

    @SuppressLint("MissingPermission")
    private void arrangeConnectableList() {
        checkBTPermissions();
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<String> namesOfBondedDevices = new ArrayList<>();

        for(BluetoothDevice btDevice : bondedDevices) {
            namesOfBondedDevices.add(btDevice.getName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, namesOfBondedDevices);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    @SuppressLint("MissingPermission")
    public void connectButton(View view) {
        String deviceName = String.valueOf(spinner.getSelectedItem());
        textView.setText(deviceName);

        for(BluetoothDevice btDevice : bondedDevices) {
            if(deviceName.equals(btDevice.getName())) {
                theDevice = btDevice;
            }
        }

        connectBT();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_NONE)
                    mBTDevices.add(device);
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver receiverBond = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(MainActivity.this, "BONDED", Toast.LENGTH_SHORT).show();
                }

                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(MainActivity.this, "BONDING", Toast.LENGTH_SHORT).show();
                }

                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(MainActivity.this, "NONE", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    public void buttonDiscover(View view) {
        mBTDevices.clear();
        discover();
    }

    public void buttonSend(View view) {

        if(theDevice == null) return;


        try {
            OutputStream outputStream = theBluetoothSocket.getOutputStream();
            String str = String.valueOf(editText.getText());
            outputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /* if (requestCode == REQUEST_BLUETOOTH_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                discover();
            }
        } else {
            Toast.makeText(this, "FUCK YOU", Toast.LENGTH_LONG).show();
        } */


    }

    @SuppressLint("MissingPermission")
    private void discover() {

        checkBTPermissions();

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, discoverDevicesIntent);
        } else if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, discoverDevicesIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        try {
            theBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothAdapter.cancelDiscovery();

        theDevice = mBTDevices.get(position);
        connectBT();
    }

    @SuppressLint("MissingPermission")
    private void connectBT() {
        mBluetoothAdapter.cancelDiscovery();

        theDevice.createBond();
        textView.setText(theDevice.getName());

        int counter = 0;
        do {
            try {
                theBluetoothSocket = theDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
                theBluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        } while(counter < 3 && (!theBluetoothSocket.isConnected()));
    }
}