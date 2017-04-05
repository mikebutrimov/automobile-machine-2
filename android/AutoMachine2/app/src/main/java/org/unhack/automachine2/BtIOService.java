package org.unhack.automachine2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static org.unhack.automachine2.MainActivity.currentBtDevice;

public class BtIOService extends Service {
    public BluetoothDevice mBtDevice;
    public UUID mUUID = null;
    public BluetoothAdapter mBluetoothAdapter;
    public ConnectThread connect;
    public VehicleControlThread mVehicleControlThread;
    private Intent mTrackIntent;
    private Bundle mCurrentTrack;
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
            registerReceiver(mTrackReceiver,new IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED));
        }
    };

    private BroadcastReceiver mTrackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mTrackIntent = intent;
            processTrackIntent();
            Log.w(TAG, "mTrackReceiver " + intent);
        }
    };



    @Override
    public void onCreate() {
        registerReceiver(mCommandReceiver,new IntentFilter(MainActivity.INTENT_FILTER_INPUT_COMMAND));
        registerReceiver(mConnectedThreadIsReady, new IntentFilter(MainActivity.INTENT_FILTER_CONNECTEDTHREAD_READY));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("BT DEVICE", currentBtDevice + " is Selected");
        if (currentBtDevice == null){
            this.onDestroy();
        }
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
        unregisterReceiver(mTrackReceiver);
        super.onDestroy();
    }

    //user functions
    private void processTrackIntent() {
        mCurrentTrack = null;
        if(mTrackIntent != null) {
            mCurrentTrack = mTrackIntent.getBundleExtra(PowerampAPI.TRACK);
            if(mCurrentTrack != null) {
                int position = mCurrentTrack.getInt(PowerampAPI.Track.POS_IN_LIST);
                int duration = mCurrentTrack.getInt(PowerampAPI.Track.DURATION);
                String artist = mCurrentTrack.getString(PowerampAPI.Track.ARTIST);
                String album = mCurrentTrack.getString(PowerampAPI.Track.ALBUM);
                String title = mCurrentTrack.getString(PowerampAPI.Track.TITLE);
                Log.d("POWERAMP!", " " +position + " " + artist + " " + album + " " + title);
                //HARDCODED
                //PRIBITO GVOZDIAMY (tm)
                String track_info = artist + /*" " + album +*/ " " + title;

                if (track_info.length() >= 40){
                    track_info = track_info.substring(0,40);
                }
                else {
                    track_info = track_info.substring(0, track_info.length());
                }

                byte[] track_info_as_array = new byte[40];

                //fill track info array
                for (int i = 0; i< track_info.length(); i++){
                    track_info_as_array[i] = (byte)(int) track_info.charAt(i);
                }

                int can_address = 0xa4;
                byte msg[] = {16,  44,  32,  0, 88,  19,  32,  32};
                //msg[3] = (byte) position;
                //msg[6] = track_info_as_array[0];
                //msg[7] = track_info_as_array[1];
                Intent cmdUpIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                cmdUpIntent.putExtra("address", can_address);
                cmdUpIntent.putExtra("repeat",false);
                cmdUpIntent.putExtra("interval",0);
                ArrayList payload = new ArrayList();
                payload.add(msg);
                cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                sendBroadcast(cmdUpIntent);


                byte[] prefix = {33,34,35,36,37,38};
                int j = 2;
                for (int i = 0; i< 6; i++){
                    if (i > 4){
                        msg = new byte[4];
                        msg[0] = prefix[i];
                        for (int k = 1; k< 4; k++){
                            msg[k] =  track_info_as_array[j];
                            j++;
                        }
                    }
                    else {
                        msg = new byte[8];
                        msg[0] = prefix[i];
                        for (int k = 1; k< 8; k++){
                            msg[k] =  track_info_as_array[j];
                            j++;
                        }
                    }



                    cmdUpIntent.putExtra("address", can_address);
                    cmdUpIntent.putExtra("repeat",false);
                    cmdUpIntent.putExtra("interval",0);
                    payload = new ArrayList();
                    payload.add(msg);
                    cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                    sendBroadcast(cmdUpIntent);
                }










                /*
                byte msg1[] = {16,  44,  32,  0, 88,  19,  32,  32};
                byte msg2[] = {33,  80, 48, 119, 110, 100, 32, 32};
                byte msg3[] = {34,  66, 89, 32, 32, 85, 78, 72};
                byte msg4[] = {35,  65, 67, 75, 32, 77,  97,  110};
                byte msg5[] = {36,  117, 32,  67,  104, 97,  111, 32};
                byte msg6[] = {37,  45,  32,  82,  117, 109, 98,  97};
                byte msg7[] = {38,  32,  68,  101};

                ///
                Intent cmdUpIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                cmdUpIntent.putExtra("address", can_address);
                cmdUpIntent.putExtra("repeat",false);
                cmdUpIntent.putExtra("interval",0);
                ArrayList payload = new ArrayList();
                payload.add(msg1);
                payload.add(msg2);
                payload.add(msg3);
                payload.add(msg4);


                cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                sendBroadcast(cmdUpIntent);

                cmdUpIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                cmdUpIntent.putExtra("address", can_address);
                cmdUpIntent.putExtra("repeat",false);
                cmdUpIntent.putExtra("interval",0);
                payload = new ArrayList();
                payload.add(msg5);
                payload.add(msg6);
                payload.add(msg7);
                cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                sendBroadcast(cmdUpIntent);
                */



            }
        }
    }
}
