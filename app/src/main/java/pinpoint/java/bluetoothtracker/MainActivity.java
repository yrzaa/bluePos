package pinpoint.java.bluetoothtracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT =  1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    //list of pair of bluetooth devices and corresponding signal strength
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private HashMap<BluetoothDevice, Integer> deviceSignalStrength = new HashMap<>();

    private Button refreshButton;
    private TextView displayTextView;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "onReceive");
            String action = intent.getAction();
            Log.d("BroadcastReceiver", "action: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("BroadcastReceiver", "ACTION_FOUND");
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BroadcastReceiver", "device: " + device);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.d("BroadcastReceiver", "rssi: " + rssi);
                deviceList.add(device);
                deviceSignalStrength.put(device, rssi);
                updateUI();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshButton = findViewById(R.id.btn_refresh);
        displayTextView = findViewById(R.id.textView);



        // Use this check to determine whether Bluetooth classic is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //check ACCESS_FINE_LOCATION permission
        //if not granted, request permission
        //if granted, continue
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth, log
            Log.d("Bluetooth", "Device doesn't support Bluetooth");
        } else {
            // Device supports Bluetooth, log
            Log.d("Bluetooth", "Device supports Bluetooth");
        }
        if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                someActivityResultLauncher.launch(enableBtIntent);
            }

        // Add devices to list when discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter); // Don't forget to unregister during onDestroy


    }


    // update recycler view on scan button click
    @SuppressLint("MissingPermission")
    public void refreshButton(View view) {
        Log.d("refreshButton", "refreshButton");
        // clear list
        deviceList.clear();
        // start discovery
        bluetoothAdapter.startDiscovery();
        //disable button
        refreshButton.setEnabled(false);
        // wait for discovery to finish
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // enable button
        refreshButton.setEnabled(true);

        Log.d("refreshButton", "deviceList: " + deviceList);




    }

    @SuppressLint("MissingPermission")
    private void updateUI() {

        // first line of textview, colummn names
        String displayText = "Device Name, Device Address, RSSI, Distance\n";
        // update text view
        for (BluetoothDevice device : deviceList) {
            // name, mac, signal strength, distance, type, newline

            //double distance = calculateDistance(rssi);
            displayText += device.getName() + ", " + device.getAddress() + ", " + deviceSignalStrength.get(device) + ", " + device.getType() + "\n";




        }
        displayTextView.setText(displayText);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                    }
                }
            });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }






    }



