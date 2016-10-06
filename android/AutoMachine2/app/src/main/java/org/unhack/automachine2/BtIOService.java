package org.unhack.automachine2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    public VehicleControlThread mVehicleControlThread;
    public BtIOService() {
    }


    public BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int address = intent.getIntExtra("address",0);
            boolean repeat = intent.getBooleanExtra("repeat", false);
            boolean delete = intent.getBooleanExtra("delete",false);
            int interval = intent.getIntExtra("interval",0);
            ArrayList payload = intent.getParcelableArrayListExtra("payload");
            Msg.controlMessage message = Utils.createMessage(address,payload);
            Log.d("RECEIVER", "MESSAGE: " + message.toString());
            VehicleCommand mVehicleCommand = new VehicleCommand(repeat,interval,message);
            Log.d("BOOLEAN", "Boolean extra: " + String.valueOf(delete));
            if (delete) {
                mVehicleControlThread.removeCommand(mVehicleCommand);
            }
            else {
                mVehicleControlThread.putCommand(mVehicleCommand);
            }
        }
    };

    public BroadcastReceiver mConnectedThreadIsReady = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTEDRECEIVER", "IN RECEIVE");
            Log.d("THREADNULL", "Thread toString" + connect.getConnectedThread().toString());
            mVehicleControlThread = new VehicleControlThread(getApplicationContext(),connect.getConnectedThread());
            mVehicleControlThread.start();
        }
    };



    @Override
    public void onCreate() {
        registerReceiver(mCommandReceiver,new IntentFilter(MainActivity.INTENT_FILTER_INPUT_COMMAND));
        registerReceiver(mConnectedThreadIsReady, new IntentFilter(MainActivity.INTENT_FILTER_CONNECTEDTHREAD_READY));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("BT DEVICE", currentBtDevice + " is Selected");
        if (!currentBtDevice.isEmpty()) {
            try {
                mBtDevice = getBtDeviceFromName(currentBtDevice);
                mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                connect = new ConnectThread(mBtDevice, mUUID, mBluetoothAdapter, getApplicationContext());
                connect.start();

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
            connect.getConnectedThread().writeMessage(msg);
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
            connect.getConnectedThread().halt();
            mVehicleControlThread.halt();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        unregisterReceiver(mCommandReceiver);
        unregisterReceiver(mConnectedThreadIsReady);
        super.onDestroy();
    }

}
