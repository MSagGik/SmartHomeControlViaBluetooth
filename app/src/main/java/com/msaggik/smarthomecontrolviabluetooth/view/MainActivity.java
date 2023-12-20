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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.msaggik.smarthomecontrolviabluetooth.R;
import com.msaggik.smarthomecontrolviabluetooth.utility.PermissionApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements Runnable{

    // поля
    private TextView climate;
    private SharedPreferences sharedPreferences; // настройки
    private MenuItem bluetoothCheck; // кнопка включения bluetooth
    private BluetoothAdapter bluetoothAdapter; // адаптер для bluetooth
    private Thread threadConnect; // поток соединения с устройством
    private BluetoothDevice device; // устройство
    private BluetoothSocket socket; // сокет для связи с устройством
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB"; // номер UUID последовательного порта Bluetooth
    private InputStream inputStream; // входящие данные
    private OutputStream outputStream; // исходящие данные
    private Handler handler; // обработчик очереди сообщений
    private String message = "Получение данных климат контроля ..."; // входные данные в виде строки

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // включение светлой темы при тесте
        setContentView(R.layout.activity_main);

        // привязка разметки
        climate = findViewById(R.id.climate);
        Switch[] lighting = new Switch[]{findViewById(R.id.lighting_1), findViewById(R.id.lighting_2), findViewById(R.id.lighting_3), findViewById(R.id.lighting_4),
                findViewById(R.id.lighting_5), findViewById(R.id.lighting_6), findViewById(R.id.lighting_7), findViewById(R.id.lighting_8)};

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // инициализация стандартного bluetoothAdapter
        sharedPreferences = this.getSharedPreferences("device_mac", Context.MODE_PRIVATE); // полученние данных сохраннённого MAC-адреса устройства из настроек
        Toast.makeText(this, "В настройках устройство " + sharedPreferences.getString("keyMac", "отсутствует"), Toast.LENGTH_SHORT).show();

        new PermissionApp(getApplicationContext(), this); // регистрация и выдача разрешений

        handler = new Handler(); // обработчик очереди сообщений



        // обработка нажатия Switch
        for (Switch switchLighting: lighting) {
            switchLighting.setOnCheckedChangeListener(listener);
        }
    }

    // создание слушателя (отправка команд на контроллер через bluetooth)
    private final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.lighting_1:
                    if (b) {
                        dataOutput('1');
                    } else {
                        dataOutput('a');
                    }
                    break;
                case R.id.lighting_2:
                    if (b) {
                        dataOutput('2');
                    } else {
                        dataOutput('b');
                    }
                    break;
                case R.id.lighting_3:
                    if (b) {
                        dataOutput('3');
                    } else {
                        dataOutput('c');
                    }
                    break;
                case R.id.lighting_4:
                    if (b) {
                        dataOutput('4');
                    } else {
                        dataOutput('d');
                    }
                    break;
                case R.id.lighting_5:
                    if (b) {
                        dataOutput('5');
                    } else {
                        dataOutput('e');
                    }
                    break;
                case R.id.lighting_6:
                    if (b) {
                        dataOutput('6');
                    } else {
                        dataOutput('f');
                    }
                    break;
                case R.id.lighting_7:
                    if (b) {
                        dataOutput('7');
                    } else {
                        dataOutput('g');
                    }
                    break;
                case R.id.lighting_8:
                    if (b) {
                        dataOutput('8');
                    } else {
                        dataOutput('h');
                    }
                    break;
            }
        }
    };

    // метод создания меню в активности
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic_menu, menu); // привязка разметки меню к активности
        bluetoothCheck = menu.findItem(R.id.bluetooth_check); // привязка разметки кнопки

        // задание картинки для кнопки bluetooth
        setBluetoothCheck();

        return super.onCreateOptionsMenu(menu);
    }

    // слушатель нажатия кнопок в меню
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.bluetooth_check: // обработка нажатия на кнопку bluetooth
                if (!bluetoothAdapter.isEnabled()) { // проверка выключенного bluetooth
                    // включение bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activityResultLauncher.launch(intent);
                } else {
                    // проверка наличия разрешения на включение bluetooth
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                    bluetoothAdapter.disable(); // выключение bluetooth
                    bluetoothCheck.setIcon(R.drawable.bluetooth_off); // определение иконки выключенного bluetooth
                }
                break;
            case R.id.bluetooth_list: // обработка нажатия на кнопку меню
                if (bluetoothAdapter.isEnabled()) { // поиск устройств в случае включённого bluetooth
                    Intent intent = new Intent(MainActivity.this, DeviceSelectionActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Bluetooth выключен", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bluetooth_connect: // обработка нажатия на кнопку меню
                if (threadConnect == null) { // если потока соединения ещё не создано, то
                    connectDevice(); // соединение с устройством
                    Toast.makeText(this, "Умное устройство подключено", Toast.LENGTH_SHORT).show();
                } else { // иначе
                    connectDevice(); // обрыв соединения
                    threadConnect.isInterrupted(); // прерывание потока соединения
                    threadConnect = null; // обнуление потока соединения
                    Toast.makeText(this, "Умное устройство отключено", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
            Log.i("ConnectBluetooth", "Успешное подключение");
            inputStream = socket.getInputStream();  // считывание входящих данных
            outputStream = socket.getOutputStream(); // отправка данных на устройство

            handler.postDelayed(dataUpdate, 0); // запуск потока с нулевой задержкой для обновления UI
            dataInput(); // считывание данных с устройства
        } catch (IOException e) {
            Log.i("ConnectBluetooth", "Соединение не удалось");
            try {
                handler.removeCallbacks(dataUpdate); // удаление из очереди данного потока
                socket.close(); // приостановление соединения
                Log.i("ConnectBluetooth", "Успешное отключение");
            } catch (IOException x) {
                Log.i("ConnectBluetooth", "Не удалось отключиться");
            }
        }
    }

    // создание нового потока для обновления данных в UI
    private Runnable dataUpdate = new Runnable() {
        @Override
        public void run() {
            climate.setText(message);
            handler.postDelayed(this, 0); // запуск потока с нулевой задержкой
        }
    };

    // метод получения данных с устройства через bluetooth
    public void dataInput() {
        // контейнер для входных данных
        byte[] bytes = new byte[1_000]; // инициализация контейнера для данных
        // считывание данных
        while (true) {
            try {
                int size = inputStream.read(bytes); // запись входных данных в контейнер
                message = new String(bytes, 0, size); // преобразование входных данных в строку
                Log.i("InputBluetooth", message);
            } catch (IOException e) {
                Log.i("InputBluetooth", "Записать данные не удалось");
                break;
            }
        }
    }
    // метод отправки данных на устройство через bluetooth
    public void dataOutput(char dataString) {
        try {
            outputStream.write(dataString);
        } catch (IOException e) {
            Log.i("OutputBluetooth", "Данные отправить не удалось");
        }
    }

    // метод задания в меню для кнопки bluetooth картинки
    private void setBluetoothCheck() {
        // проверка включения bluetooth
        if (bluetoothAdapter.isEnabled()) { // если bluetooth включен, то
            bluetoothCheck.setIcon(R.drawable.bluetooth_on); // определение иконки включенного bluetooth
        } else { // иначе
            bluetoothCheck.setIcon(R.drawable.bluetooth_off); // определение иконки выключенного bluetooth
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