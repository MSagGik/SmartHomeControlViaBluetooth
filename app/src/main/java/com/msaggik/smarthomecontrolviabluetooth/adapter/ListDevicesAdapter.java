package com.msaggik.smarthomecontrolviabluetooth.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.msaggik.smarthomecontrolviabluetooth.R;
import com.msaggik.smarthomecontrolviabluetooth.entity.BluetoothSmartDevice;

import java.util.List;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesAdapter.ViewHolder> {

    // поля адаптера
    private Context context; // поле для контекста
    private Activity activity; // поле для активности
    private List<BluetoothSmartDevice> devices; // список для объектов устройств
    private int checkedDevice; // позиция в списке выбранного устройства
    private SharedPreferences preferences; // настройки приложения

    // конструктор
    public ListDevicesAdapter(Context context, Activity activity, List<BluetoothSmartDevice> devices) {
        this.context = context;
        this.activity = activity;
        this.devices = devices;
        preferences = context.getSharedPreferences("device_mac", Context.MODE_PRIVATE);
    }

    // метод onCreateViewHolder() возвращает объект ViewHolder(), который будет хранить данные по одному объекту DeviceEntity
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // трансформация layout-файла во View-элемент
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth, parent, false);
        return new ViewHolder(view);
    }

    // метод onBindViewHolder() выполняет привязку объекта ViewHolder к объекту Notebook по определенной позиции
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.deviceName.setText(devices.get(position).getName());
        holder.deviceMac.setText(devices.get(position).getMacAddress());
        // выбранное устройство из настроек
        if (preferences.getString("keyMac", "Устройство не выбранно").equals(devices.get(position).getMacAddress())) { // проверка выбранного значения с ранее записанным
            holder.deviceCheck.setChecked(true);
            devices.get(position).setChecked(true); // корректировка флага выбранного устройства
            checkedDevice = position; // фиксация позиции в списке устройства
        } else {
            holder.deviceCheck.setChecked(false);
        }

        // обработка нажатия на контейнер item_bluetooth.xml
        holder.deviceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devices.get(checkedDevice).setChecked(false); // корректировка флага прошлого выбранного устройства
                devices.get(position).setChecked(true); // корректировка флага нового выбранного устройства
                checkedDevice = position; // фиксация позиции в списке нового устройства
                notifyDataSetChanged(); // обновление списка устройств
                holder.deviceCheck.setChecked(true); // установка выбранного чек-бокса
                // перезапись настроек новым MAC адресом
                SharedPreferences.Editor editor = preferences.edit(); // открытие настроек для записи
                editor.putString("keyMac", devices.get(position).getMacAddress()); // запись выбранного MAC адреса
                editor.apply(); // сохранение настроек
            }
        });
    }

    // метод getItemCount() возвращает количество объектов в списке
    @Override
    public int getItemCount() {
        return devices.size();
    }

    // созданный статический класс ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder{
        // поля представления
        TextView deviceName, deviceMac;
        CheckBox deviceCheck;
        ConstraintLayout deviceItem;

        // конструктор класса ViewHolder с помощью которого мы связываем поля и представление item_bluetooth.xml
        ViewHolder(@NonNull View view) {
            super(view);
            deviceName = view.findViewById(R.id.name_device);
            deviceMac = view.findViewById(R.id.mac_device);
            deviceCheck = view.findViewById(R.id.check_device);
            deviceItem = view.findViewById(R.id.item_device);
        }
    }
}