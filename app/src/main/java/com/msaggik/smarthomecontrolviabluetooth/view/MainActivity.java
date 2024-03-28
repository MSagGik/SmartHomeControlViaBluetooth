package com.msaggik.smarthomecontrolviabluetooth.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.msaggik.smarthomecontrolviabluetooth.R;
import com.msaggik.smarthomecontrolviabluetooth.adapter.ListDevicesAdapter;
import com.msaggik.smarthomecontrolviabluetooth.entity.BluetoothSmartDevice;
import com.msaggik.smarthomecontrolviabluetooth.utility.PermissionApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Runnable {

    // поля разметки (общие)
    private ConstraintLayout lightingLayout, climateLayout, settingLayout;
    private BottomNavigationView navigationMenu;
    private int a = 10, r = 0, g = 0, b = 0;
    // поля разметки (lightingLayout)
    private ImageView setColor; // изображение палитры
    private TextView infoColor; // информация о выбранном цвете
    private ShapeAppearanceModel shapeAppearanceModel;
    private MaterialShapeDrawable materialShapeDrawable;
    private SeekBar setPowerLighting;
    // поля разметки (climateLayout)
    private TextView climate;
    // поля разметки (settingLayout)
    private RecyclerView listBluetooth;
    private ListDevicesAdapter listDevicesAdapter;
    private List<BluetoothSmartDevice> list;
    private BluetoothAdapter bluetoothAdapter; // адаптер для bluetooth
    private Button bluetoothConnect, bluetoothCheck;
    // поля работы с памятью устройства (настройки приложения)
    private SharedPreferences sharedPreferences; // настройки
    // поля bluetooth
    private BluetoothDevice device; // устройство
    private BluetoothSocket socket; // сокет для связи с устройством
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB"; // номер UUID последовательного порта Bluetooth
    // поля дополнительных потоков
    private Thread threadConnect; // поток соединения с устройством
    private Handler handler; // обработчик очереди сообщений
    // поля ввода вывода контроллера
    private InputStream inputStream; // входящие данные
    private OutputStream outputStream; // исходящие данные
    // вспомогательное поле private
    private String message = ""; // входные данные в виде строки

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // включение светлой темы при тесте
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // получение разрешений
        new PermissionApp(getApplicationContext(), this); // регистрация и выдача разрешений
        // считывание настроек приложения
        sharedPreferences = this.getSharedPreferences("device_mac", Context.MODE_PRIVATE); // полученние данных сохраннённого MAC-адреса устройства из настроек
        Toast.makeText(this, "В настройках устройство " + sharedPreferences.getString("keyMac", "отсутствует"), Toast.LENGTH_SHORT).show();
        // обработчик очереди сообщений (для обновления сообщений между устройством умного дома и смартфоном)
        handler = new Handler();

        // привязка к разметке
        lightingLayout = findViewById(R.id.lighting_layout);
        climateLayout = findViewById(R.id.climate_layout);
        settingLayout = findViewById(R.id.setting_layout);
        navigationMenu = findViewById(R.id.navigation_menu);
        setColor = findViewById(R.id.set_color);
        infoColor = findViewById(R.id.info_color);
        setPowerLighting = findViewById(R.id.set_power_lighting);
        climate = findViewById(R.id.climate);
        listBluetooth = findViewById(R.id.list_bluetooth);
        bluetoothConnect = findViewById(R.id.bluetooth_connect);
        bluetoothCheck = findViewById(R.id.bluetooth_check);

        // форма и содержание TextView
        shapeAppearanceModel = new ShapeAppearanceModel().toBuilder().setAllCorners(CornerFamily.ROUNDED, 35).build();
        materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        ViewCompat.setBackground(infoColor, materialShapeDrawable);
        materialShapeDrawable.setFillColor(ColorStateList.valueOf(Color.argb(a, r, g, b)));
        infoColor.setText(String.format("#%02x%02x%02x%02x", a, r, g, b));
        setPowerLighting.setProgress(a);

        // настройки (settingLayout)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        list = getBluetoothDevices();
        if (list != null) {
            listDevicesAdapter = new ListDevicesAdapter(getApplicationContext(), this, list);
            listBluetooth.setAdapter(listDevicesAdapter);
        }

        if (bluetoothAdapter.isEnabled()) { // проверка выключенного bluetooth
            bluetoothCheck.setText(R.string.bluetooth_off); // определение иконки выключенного bluetooth
            bluetoothCheck.getBackground().setTint(Color.argb(0xFF, 0x11, 0x6B, 0x68));
        }

        // задание слушателей
        // общий слушатель
        navigationMenu.setOnItemSelectedListener(itemSelectedListener);
        // слушатели выбора цвета (lightingLayout)
        setColor.setOnTouchListener(touchListener);
        setPowerLighting.setOnSeekBarChangeListener(seekBarChangeListener);
        // слушатели кнопок настроек (settingLayout)
        bluetoothConnect.setOnClickListener(listenerButton);
        bluetoothCheck.setOnClickListener(listenerButton);
    }

    private final View.OnClickListener listenerButton = new View.OnClickListener() {
        @SuppressLint({"DefaultLocale", "ResourceAsColor"})
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bluetooth_connect: // обработка нажатия на кнопку меню
                    if (threadConnect == null) { // если потока соединения ещё не создано, то
                        connectDevice(); // соединение с устройством
                        bluetoothConnect.setText(R.string.connect_controller_off);
                        bluetoothConnect.getBackground().setTint(Color.argb(0xFF, 0x11, 0x6B, 0x68));
                    } else { // иначе
                        connectDevice(); // обрыв соединения
                        threadConnect.isInterrupted(); // прерывание потока соединения
                        threadConnect = null; // обнуление потока соединения
                        bluetoothConnect.setText(R.string.connect_controller_on);
                        bluetoothConnect.getBackground().setTint(Color.argb(0xFF, 0x77, 0x85, 0x84));
                    }
                    break;
                case R.id.bluetooth_check: // обработка нажатия на кнопку bluetooth
                    if (!bluetoothAdapter.isEnabled()) { // проверка выключенного bluetooth
                        // включение bluetooth
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        activityResultLauncher.launch(intent);
                        bluetoothCheck.setText(R.string.bluetooth_off); // определение иконки выключенного bluetooth
                        bluetoothCheck.getBackground().setTint(Color.argb(0xFF, 0x11, 0x6B, 0x68));
                    } else {
                        // проверка наличия разрешения на включение bluetooth
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "Отсутствует разрешение на использование bluetooth", Toast.LENGTH_SHORT).show();
                        }
                        bluetoothAdapter.disable(); // выключение bluetooth
                        bluetoothCheck.setText(R.string.bluetooth_on); // определение иконки выключенного bluetooth
                        bluetoothCheck.getBackground().setTint(Color.argb(0xFF, 0x77, 0x85, 0x84));
                    }
                    break;
            }
        }
    };

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean value) {
            // задание прозрачности цвета
            a = progress;
            //infoColor.setBackgroundColor(Color.argb(a, r, g, b));
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(Color.argb(a, r, g, b)));
            infoColor.setText(String.format("#%02x%02x%02x%02x", a, r, g, b));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (outputStream != null) {
                dataOutputArrayByte(String.format("5 %d", a).getBytes());
            } else {
                Toast.makeText(getApplicationContext(), "Устройство умного дома не подключено к смартфону", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private final NavigationBarView.OnItemSelectedListener itemSelectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.lighting_menu:
                    lightingLayout.setVisibility(View.VISIBLE);
                    climateLayout.setVisibility(View.INVISIBLE);
                    settingLayout.setVisibility(View.INVISIBLE);
                    break;
                case R.id.climate_menu:
                    lightingLayout.setVisibility(View.INVISIBLE);
                    climateLayout.setVisibility(View.VISIBLE);
                    settingLayout.setVisibility(View.INVISIBLE);
                    break;
                case R.id.settings_menu:
                    lightingLayout.setVisibility(View.INVISIBLE);
                    climateLayout.setVisibility(View.INVISIBLE);
                    settingLayout.setVisibility(View.VISIBLE);
                    break;
            }
            return true;
        }
    };
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint({"DefaultLocale"})
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                setColor.setDrawingCacheEnabled(true); // включение возможности задания кэша изображения R.id.set_color
                setColor.buildDrawingCache(true); // включение создания кэша изображения R.id.set_color
                Bitmap bitmapColorImage = setColor.getDrawingCache(); // запись пикселей картинки в растровое изображение

                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                int pixel = 0;
                int radiusImage = 70;
                if ((x >= 0 && x <= bitmapColorImage.getWidth() && y >= 0 && y <= bitmapColorImage.getHeight())
                        && !(x <= radiusImage && y <= radiusImage)
                        && !(x <= radiusImage && y >= bitmapColorImage.getHeight() - radiusImage)
                        && !(x >= bitmapColorImage.getWidth() - radiusImage && y <= radiusImage)
                        && !(x >= bitmapColorImage.getWidth() - radiusImage && y >= bitmapColorImage.getHeight() - radiusImage)) {
                    try {
                        pixel = bitmapColorImage.getPixel(x, y);
                        //a = Color.alpha(pixel);
                        r = Color.red(pixel);
                        g = Color.green(pixel);
                        b = Color.blue(pixel);
                    } catch (Exception e) {
                        Log.e("getPixel(x, y)", String.format("Координаты %d и %d", x, y));
                    }
                }
                setColor.destroyDrawingCache();
                setColor.setDrawingCacheEnabled(false);
                setColor.buildDrawingCache(false);

                // задание цвета фона для View выбора цвета
                //infoColor.setBackgroundColor(Color.argb(a, r, g, b));
                materialShapeDrawable.setFillColor(ColorStateList.valueOf(Color.argb(a, r, g, b)));
                infoColor.setText(String.format("#%02x%02x%02x%02x", a, r, g, b));
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && outputStream != null) {
                // задание цвета на устройстве
                dataOutputArrayByte(String.format("3 %d %d %d\n", r, g, b).getBytes());
            }
            return true;
        }
    };

    // создание списка bluetooth устройств
    private List<BluetoothSmartDevice> getBluetoothDevices() {
        List<BluetoothSmartDevice> list = new ArrayList<>();

        // если отсутствует разрешение на поиск устройств по близости
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return null; // то прерывание метода
        }
        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
            BluetoothSmartDevice device = new BluetoothSmartDevice();
            device.setName(bluetoothDevice.getName());
            device.setMacAddress(bluetoothDevice.getAddress());
            device.setChecked(false); // установление по умолчанию не выбранного значения
            list.add(device);
        }
        return list;
    }

    // метод проверки и подключение устройства через bluetooth
    public void connectDevice() {
        // MAC адрес добавленного в настройки устройства
        String macDevice = sharedPreferences.getString("keyMac", ""); // считывание из настроек MAC адреса устройства
        if (bluetoothAdapter.isEnabled() && !macDevice.isEmpty()) { // если bluetooth включен и имеется MAC адрес, то
            device = bluetoothAdapter.getRemoteDevice(macDevice); // подключение к устройству по MAC адресу
            if (device != null) { // если устройство активно, то
                threadConnect = new Thread(this); // создание второго потока
                threadConnect.start(); // запуск второго потока
            } else {
                Toast.makeText(this, "Умное устройство выключено", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "bluetooth выключен или забыли выбрать устройство", Toast.LENGTH_SHORT).show();
        }
    }

    // реализация действий в дополнительном потоке
    @Override
    public void run() {
        // если отсутствует разрешение на поиск устройств по близости
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return; // то прерывание метода
        }
        bluetoothAdapter.cancelDiscovery(); // отмена текущего процесса обнаружения устройства
        try {
            socket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID)); // создание сокета соединения
            socket.connect(); // подключение устройства
            inputStream = socket.getInputStream();  // считывание входящих данных
            outputStream = socket.getOutputStream(); // отправка данных на устройство
            handler.postDelayed(dataUpdate, 0); // запуск потока с нулевой задержкой для обновления UI
            dataInput(); // считывание данных с устройства
        } catch (IOException e) {
            try {
                handler.removeCallbacks(dataUpdate); // удаление из очереди данного потока
                socket.close(); // приостановление соединения
            } catch (IOException x) {
                Log.i("ConnectBluetooth", "Не удалось отключиться");
            }
        }
    }

    // создание нового потока для обновления данных в UI
    private Runnable dataUpdate = new Runnable() {
        @Override
        public void run() {
            climate.append(message);
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(MainActivity.this, "Устройство выключилось", Toast.LENGTH_SHORT).show();
                threadConnect.isInterrupted(); // прерывание потока соединения
                threadConnect = null; // обнуление потока соединения
                bluetoothConnect.setText(R.string.connect_controller_on);
                bluetoothConnect.getBackground().setTint(Color.argb(0xFF, 0x77, 0x85, 0x84));
            }
            handler.postDelayed(this, 10000); // запуск потока с нулевой задержкой
        }
    };

    // метод получения данных с устройства через bluetooth
    public void dataInput() {
        // контейнер для входных данных
        byte[] bytes = new byte[1000]; // инициализация контейнера для данных
        // считывание данных
        while (true) {
            try {
                int size = inputStream.read(bytes); // запись входных данных в контейнер
                if (message.length() >= 128) {
                    message = new String(bytes, 0, size); // преобразование входных данных в строку
                } else {
                    message += new String(bytes, 0, size); // преобразование входных данных в строку
                }
            } catch (IOException e) {
                Log.i("InputBluetooth", "Записать данные не удалось");
                break;
            }
        }
    }

    // метод отправки данных на устройство через bluetooth
    public void dataOutputChar(char dataChar) {
        try {
            outputStream.write(dataChar);
        } catch (IOException e) {
            Log.i("OutputBluetooth", "Данные отправить не удалось");
        }
    }

    // метод отправки данных на устройство через bluetooth
    public void dataOutputArrayByte(byte[] dataArrayByte) {
        try {
            outputStream.write(dataArrayByte);
        } catch (IOException e) {
            Log.i("OutputBluetooth", "Данные отправить не удалось");
        }
    }

    // метод задания в меню для кнопки bluetooth картинки
    private void setBluetoothCheck() {
        // проверка включения bluetooth
        if (bluetoothAdapter.isEnabled()) { // если bluetooth включен, то
            bluetoothCheck.setText(R.string.bluetooth_off); // определение иконки включенного bluetooth
        } else { // иначе
            bluetoothCheck.setText(R.string.bluetooth_on); // определение иконки выключенного bluetooth
        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) { // условие включения bluetooth
                        setBluetoothCheck(); // актуализация картинки для кнопки
                    }
                }
            }
    );
}