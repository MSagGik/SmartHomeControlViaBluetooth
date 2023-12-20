package com.msaggik.smarthomecontrolviabluetooth.entity;

public class BluetoothSmartDevice {

    // поля
    private String name; // название устройства
    private String macAddress; // MAC адрес устройства
    private boolean checked; // поле выбранного устройства для подключения

    // геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
