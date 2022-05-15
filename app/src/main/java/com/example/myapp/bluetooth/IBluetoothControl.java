package com.example.myapp.bluetooth;

import java.io.IOException;
import java.util.List;

class BluetoothDevicePair {
    private String name; // 蓝牙名称
    private String mac; // 蓝牙mac地址

    /**
     * 构造函数
     *
     * @param name 蓝牙设备名称
     * @param mac  蓝牙设备MAC地址
     */
    public BluetoothDevicePair(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    /**
     * 构造函数
     *
     * @param bluetoothDevice
     */
    public BluetoothDevicePair(BluetoothDevicePair bluetoothDevice) {
        this.name = bluetoothDevice.name;
        this.mac = bluetoothDevice.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

}

public interface IBluetoothControl {
    String SerialPortServiceClass_UUID = "{00001101-0000-1000-8000-00805F9B34FB}" ;
    /**
     * 向已连接的蓝牙设备写入数据流
     *
     * @param bytes 要写入的数据流
     * @return 写入是否成功
     */
    boolean write(byte[] bytes) throws IOException;

    /**
     * 向已连接的蓝牙设备写入字符串
     *
     * @param string 要写入的字符串
     * @return 写入是否成功
     */
    boolean write(String string) throws IOException;

    byte[] readBytes() throws IOException;
    
    /**
     * 开启蓝牙
     *
     * @return 是否成功
     */
    boolean startBluetooth();

    /**
     * 扫描附近的蓝牙设备
     *
     * @return 扫描到的蓝牙列表
     */
    List<BluetoothDevicePair> getDeviceList();

    /**
     * 根据mac地址连接蓝牙
     *
     * @param mac    蓝牙mac地址
     * @param passwd 连接密码
     * @return 链接是否成功
     */
    boolean connect(String mac, String passwd);

    /**
     * 断开当前连接
     *
     * @return 断开是否成功
     */
    boolean disconnect();

    void setCallback(Callback runnable);
}
