package com.msaggik.smarthomecontrolviabluetooth.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class PermissionApp {

    private Context context;
    private ComponentActivity activity;

    private ActivityResultLauncher<String[]> storagePermissionLauncher; // для нескольких разрешений
    @RequiresApi(api = Build.VERSION_CODES.S)
    private final String[] PERMISSION_NEW = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN}; // разрешения

    private final String[] PERMISSION_OLD = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN}; // разрешения

    public PermissionApp(Context context, ComponentActivity activity) {
        this.context = context;
        this.activity = activity;
        registerPermission();
        checkPermission();
    }

    // метод проверки разрешений
    private void checkPermission() {
        // проверка разрешений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // если версия ОС Андроид больше равно API 31, то
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context.getApplicationContext(), "Данны разрешения на работу с bluetooth", Toast.LENGTH_SHORT).show();
            } else { // запрос нескольких разрешений
                storagePermissionLauncher.launch(PERMISSION_NEW);
            }
        } else { // иначе регистрация разрешений для более ранних ОС Андроида
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context.getApplicationContext(), "Данны разрешения на работу с bluetooth", Toast.LENGTH_SHORT).show();
            } else { // запрос нескольких разрешений
                storagePermissionLauncher.launch(PERMISSION_OLD);
            }
        }
    }

    // регистрация разрешений
    private void registerPermission() {
        storagePermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.containsValue(true)) { // если разрешение дано
                Toast.makeText(context.getApplicationContext(), "Разрешение работы с bluetooth имеется", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context.getApplicationContext(), "Разрешение работы с bluetooth отсутствует", Toast.LENGTH_SHORT).show();
            }
        });
    }
}