package com.jimengtec;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothGattService bluetoothGattService;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothGatt bluetoothGatt;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    private Handler handler = new Handler();
    private String str_receive;

    TextView    textViewMessage;
    TextView    textViewDynamometerDisplay;
    private final static String TAG = "+";


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
//###########################Find Device#######################
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
                            bluetoothDevice = device;
                            textViewMessage.setText("配置列表中发现设备.");
                            break;
                        }
                    }
                }
//###################Find Device ended###########################
                if (bluetoothGatt != null)
                    bluetoothGatt.disconnect();
                bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == BluetoothProfile.STATE_CONNECTED) {//状态变已连接
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewMessage.setText("成功建立连接");
                                    textViewMessage.setTextColor(Color.BLUE);
                                }
                            });
                            gatt.discoverServices();//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                        }
                        if (newState == BluetoothProfile.STATE_DISCONNECTED) { //状态变为未连接
                            Log.e(TAG, "连接断开");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewMessage.setText("连接断开");
                                    textViewMessage.setTextColor(Color.RED);
//dong                                    setContentView(view_main);
                                }
                            });
                            return;
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                        super.onServicesDiscovered(gatt, status);
                        if (status == gatt.GATT_SUCCESS) { // 发现服务成功
                            String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
                            String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
                            bluetoothGattService = gatt.getService(UUID.fromString(service_UUID));
                            bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID));
                            if (bluetoothGattCharacteristic != null) {
                                Log.e(TAG, "成功获取特征");
                                gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            } else {
                                Log.e(TAG, "获取特征失败");
                                if (gatt != null) gatt.disconnect();
                                textViewMessage.setText("获取特征失败");
                                textViewMessage.setTextColor(Color.RED);
                            }
                        } else {
                            Log.e(TAG, "发现服务失败");
                            if (gatt != null) gatt.disconnect();
                            textViewMessage.setText("发现服务失败");
                            textViewMessage.setTextColor(Color.RED);
                        }
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt
                                                                gatt, BluetoothGattCharacteristic characteristic) {
                        //用此函数#################################接收数据####################################
                        super.onCharacteristicChanged(gatt, characteristic);
                        byte[] bytesReceive = characteristic.getValue();
                        Log.e(TAG, "收到数据");
                        if (bytesReceive.length != 0) {

                                str_receive = new String(bytesReceive);
                                Log.e(TAG, str_receive);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewMessage.setText("你收到了："+str_receive);//追加字符串
 //                                   textViewMessage.append(str_receive);//追加字符串

                                }
                            });
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt
                                                              gatt, BluetoothGattCharacteristic characteristic, int status) {
                        Log.e(TAG, gatt.getDevice().getName() + " write successfully");
                    }
                });

            }


        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
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
                    bluetoothDevice = device;
                    textViewMessage.setText("配置列表中发现设备.");
                    break;
                }
            }
        }
    }

    void openBT() {
//选择bluetoothDevice后配置回调函数
        bluetoothGatt=bluetoothDevice.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {//状态变为 已连接
 //                   Log.e(TAG, "成功建立连接");
//                    textViewMessage.setText("成功建立连接.");
                    //连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            gatt.discoverServices());
                }

                if (newState == BluetoothGatt.STATE_DISCONNECTED) { //状态变为 未连接
                    Toast.makeText(MainActivity.this, "连接断开", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                //用此函数接收数据
                super.onServicesDiscovered(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "GATT_SUCCESS failure.");
                    return;
                }
                String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";//已知服务
                String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";//已知特征
                bluetoothGattService = bluetoothGatt.getService(UUID.fromString(service_UUID));//通过UUID找到服务
//                bluetoothGattService.addService(bluetoothGattService);
                bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID));//找到服务后在通过UUID找到特征
                if (bluetoothGattCharacteristic != null) {
                    bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);//启用onCharacteristicChanged(），用于接收数据
                    Log.i(TAG, "特征注册注册成功。");
                } else {
                    Log.i(TAG, "特征注册注册失败。");
                }

            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                byte[] messageBytes = characteristic.getValue();
                String messageString = null;
                try {
                    messageString = new String(messageBytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Unable to convert message bytes to string");
                }
                Log.d(TAG,"Received message: " + messageString);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.i(TAG, "有数据.");
            }

    });

    }

}
