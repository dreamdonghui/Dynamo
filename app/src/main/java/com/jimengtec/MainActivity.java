package com.jimengtec;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
               try
                {
                    findBT();
                    openBT();
                }
               catch (IOException  ex) { }
            }
        });

        // Send and close example. Not used at this moment.

        ////Send Button

        //sendButton.setOnClickListener(new View.OnClickListener()

        //{

        //public void onClick(View v)

        //{

        //try

        //{

        //sendData();

        //}

        //catch (IOException ex) { }

        //}

        //});

        //

        ////Close button

        //closeButton.setOnClickListener(new View.OnClickListener()

        //{

        //public void onClick(View v)

        //{

        //try

        //{

        //closeBT();

        //}

        //catch (IOException ex) { }

        //}

        //});

        //

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

    void openBT() throws IOException{
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //JDY-16 / WXCLJ-7 //       UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//JDY-30 0000ffe1-0000-1000-8000-00805f9b34fb
        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        try{
            mSocket.connect();
        }catch (IOException e){
            e.printStackTrace();
        }


        mOutputStream = mSocket.getOutputStream();
        mInputStream = mSocket.getInputStream();
        beginListenForDate();

        textViewMessage.setText("设备链路已建立。");
    }

    void beginListenForDate(){
        final Handler handler = new Handler();
        final byte delimiter = 10; //ASCII for new line;

        stopworker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()&& !stopworker){
                    try {
                        int bytesAvailable = mInputStream.available();
                        if (bytesAvailable>0){
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for (int i=0; i<bytesAvailable;i++){
                                byte b = packetBytes[i];
                                if (b == delimiter){
                                    byte[] encodeBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer,0,encodeBytes,0,encodeBytes.length);
                                    final String data = new String(encodeBytes,"US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                           textViewDynamometerDisplay.setText(data);
                                           textViewDynamometerDisplay.setText("配置列表77pou77连接。");

                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex){
                        stopworker = true;
                    }
                }
            }
        });
        workerThread.start();

    }
/***************************************************************
    void sendData() throws  IOException{
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }
    void closeBT() throws IOException{
        stopWorker = true;
        mOutputStream.close();
        mInputStream.close();
        mSocket.close();
        myLabel.setText("Bluetooth Closed");
    }
*************************************************************/
}
