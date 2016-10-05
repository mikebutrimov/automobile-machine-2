package org.unhack.automachine2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.unhack.automachine2.MainActivity.currentBtDevice;

public class BtIOService extends Service {
    public BluetoothDevice mBtDevice;
    public UUID mUUID = null;
    public BluetoothAdapter mBluetoothAdapter;
    public ConnectThread connect;

    public BtIOService() {
    }
    @Override
    public void onCreate() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("BT DEVICE", currentBtDevice + " is Selected");
        if (!currentBtDevice.isEmpty()) {
            try {
                mBtDevice = getBtDeviceFromName(currentBtDevice);
                mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                connect = new ConnectThread(mBtDevice, mUUID, mBluetoothAdapter, getApplicationContext());
                connect.start();
                connect.getmConnectedThread();
            }
            catch (Exception e){
                e.printStackTrace();
                this.onDestroy();
            }
        }
        else {
            this.onDestroy();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList payload = intent.getParcelableArrayListExtra("payload");
        int can_address = intent.getIntExtra("canaddress",0);
        if (can_address != 0 && !payload.isEmpty()) {
            //Log.d("onStart", "PAYLOAD: " + payload.toString());
            Msg.controlMessage msg = Utils.createMessage(can_address, payload);
            connect.getmConnectedThread().writeMessage(msg);
        }
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    public BluetoothDevice getBtDeviceFromName(String devName){
        if (mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        final HashMap<String,BluetoothDevice> mBtHashMap = new HashMap<>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            mBtHashMap.put(device.getName(),device);
        }
        if (mBtHashMap.containsKey(devName)){
            return mBtHashMap.get(devName);
        }
        return null;
    }



    @Override
    public void onDestroy(){
        try{
            connect.getmConnectedThread().halt();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();

    }
}
