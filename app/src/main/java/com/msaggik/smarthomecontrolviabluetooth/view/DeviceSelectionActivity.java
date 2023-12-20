package com.msaggik.smarthomecontrolviabluetooth.view;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.msaggik.smarthomecontrolviabluetooth.R;
import com.msaggik.smarthomecontrolviabluetooth.adapter.ListDevicesAdapter;
import com.msaggik.smarthomecontrolviabluetooth.entity.BluetoothSmartDevice;
import com.msaggik.smarthomecontrolviabluetooth.utility.PermissionApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceSelectionActivity extends AppCompatActivity {

    // поля
    private RecyclerView listBluetooth;
    private ListDevicesAdapter listDevicesAdapter;
    private List<BluetoothSmartDevice> list;
    private BluetoothAdapter bluetoothAdapter; // адаптер для bluetooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_device);

        new PermissionApp(getApplicationContext(), this); // регистрация и выдача разрешений

        // обработка возврата обратно на главную активность
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listBluetooth = findViewById(R.id.list_bluetooth);
        list = getBluetoothDevices();
        if (list != null) {
            listDevicesAdapter = new ListDevicesAdapter(getApplicationContext(), this, list);
            listBluetooth.setAdapter(listDevicesAdapter);
        }
    }

    // обработка кнопки возврата обратно на главную активность
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: // кнопка назад
                finish(); // закрытие активности
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // создание списка bluetooth устройств
    private List<BluetoothSmartDevice> getBluetoothDevices() {
        List<BluetoothSmartDevice> list = new ArrayList<>();

        // если отсутствует разрешение на поиск устройств по близости
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return null; // то прерывание метода
        }
        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice: bluetoothDevices) {
            BluetoothSmartDevice device = new BluetoothSmartDevice();
            device.setName(bluetoothDevice.getName());
            device.setMacAddress(bluetoothDevice.getAddress());
            device.setChecked(false); // установление по умолчанию не выбранного значения
            list.add(device);
        }
        return list;
    }
}