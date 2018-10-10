package com.jimengtec;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothGatt bluetoothGatt;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mSocket;
    BluetoothDevice mDevice;
    OutputStream    mOutputStream;
    InputStream     mInputStream;
    Thread          workerThread;
    byte[]          readBuffer;
    int             readBufferPosition;
    //int             counter;
    volatile    boolean stopworker;
    TextView    textViewMessage;
    TextView    textViewDynamometerDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonConect = findViewById(R.id.buttonConect);
        Button buttonZero   = findViewById(R.id.buttonZero);
        textViewMessage = findViewById(R.id.textViewMessage);
        textViewDynamometerDisplay = findViewById(R.id.textViewDynamometerDisplay);


        //Connect Button
        buttonConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    findBT();
                    openBT();
            }
        });

    }

    void findBT(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            textViewMessage.setText("本机未发现蓝牙设备！");
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent  enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth,0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        textViewMessage.setText("设备未配置。");
        if(pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices){
                if (device.getName().equals("JDY-16")){   //JDY-30  WXCLJ-7
                    mDevice = device;
                    textViewMessage.setText("配置列表中发现设备.");
                    break;
                }
            }
        }
    }

    void openBT() {
//选择bluetoothDevice后配置回调函数
        bluetoothGatt=mDevice.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {//状态变为 已连接
 //                   Log.e(TAG, "成功建立连接");
                    textViewMessage.setText("成功建立连接.");

                }
                gatt.discoverServices();//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                if (newState == BluetoothGatt.STATE_DISCONNECTED) { //状态变为 未连接
                    Toast.makeText(MainActivity.this, "连接断开", Toast.LENGTH_LONG).show();
                }
                return;
            }
            public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                //用此函数接收数据
                super.onServicesDiscovered(gatt, status);
                String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";//已知服务
                String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";//已知特征
                bluetoothGattService = bluetoothGatt.getService(UUID.fromString(service_UUID));//通过UUID找到服务
                bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID));//找到服务后在通过UUID找到特征
                if (bluetoothGattCharacteristic != null) {
                    gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);//启用onCharacteristicChanged(），用于接收数据
                    //Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "发现服务失败", Toast.LENGTH_LONG).show();
                    return;
                }

            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                textViewMessage.setText("gg " + characteristic.getValue()[0]);
            }

    });
    }

}
