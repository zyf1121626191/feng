package com.example.myapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class BluetoothControl implements IBluetoothControl {
    public String TAG = "BluetoothControl-DEBUG" ;
    private BluetoothAdapter adapter;
    BluetoothDevice bluetoothDevice ;
    BluetoothSocket socket ;
    OutputStream ostream ;
    InputStream istream ;
    Thread rxLooper ;
    static final String LOCK = "lock" ;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;
    /// final String MACBOOK_PRO = "3C:22:FB:98:04:E9" ;
    final String HC05 = "4C:75:25:CD:DE:1E";
    /// final String LENOVO = "28:3A:4D:15:4C:08" ;
    /// final String BBB = "98:D3:32:11:0D:E1" ;
    public BluetoothControl() throws IOException {
        adapter = BluetoothAdapter.getDefaultAdapter() ;
        bluetoothDevice = adapter.getRemoteDevice(HC05);
//        uuid = bluetoothDevice.getUuids()[0].getUuid();
        socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid) ;
        istream = socket.getInputStream() ;
        ostream = socket.getOutputStream();
        socket.connect();
    }
    /**
     * 向已连接的蓝牙设备写入数据流
     *
     * @param bytes 要写入的数据流
     * @return 写入是否成功
     */
    @Override
    public boolean write(byte[] bytes) throws IOException {
        ostream.write(bytes);
        return true;
    }

    /**
     * 向已连接的蓝牙设备写入字符串
     *
     * @param string 要写入的字符串
     * @return 写入是否成功
     */
    @Override
    public boolean write(String string) throws IOException {
        ostream.write(string.getBytes(StandardCharsets.US_ASCII));
        return true;
    }

    @Override
    public byte[] readBytes() throws IOException {
        byte[] ret = null ;
        if(istream == null) return ret ;
        int length = istream.available() ;
        ret = new byte[length] ;
        // Log.d(TAG, "len == " + len);
        return istream.read(ret) == 0? null : ret ;
    }

    /**
     * 开启蓝牙
     *
     * @return 是否成功
     */
    @Override
    public boolean startBluetooth() {
        return false;
    }

    /**
     * 扫描附近的蓝牙设备
     *
     * @return 扫描到的蓝牙列表
     */
    @Override
    public List<BluetoothDevicePair> getDeviceList() {
        return null;
    }

    /**
     * 根据mac地址连接蓝牙
     *
     * @param mac    蓝牙mac地址
     * @param passwd 连接密码
     * @return 链接是否成功
     */
    @Override
    public boolean connect(String mac, String passwd) {
        return false;
    }

    /**
     * 断开当前连接
     *
     * @return 断开是否成功
     */
    @Override
    public boolean disconnect() {
        return false;
    }



    @Override
    public void setCallback(final Callback callback) {
        if(rxLooper != null) {
            rxLooper.interrupt();
        }
        rxLooper = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while(true) {
                    if(Thread.currentThread().isInterrupted()) {
                        return ;
                    }
                    if(callback != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Log.d("CallBack-DEBUG", "Rx!");
                                callback.onRecieve(istream);
                            }
                        }).start();
                    }
                }
            }
        }) ;
        rxLooper.start();
    }
}
