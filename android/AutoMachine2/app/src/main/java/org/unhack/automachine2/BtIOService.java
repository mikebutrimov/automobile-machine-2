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
    public AMTrack mAMCurrentTrack = new AMTrack(this);
    private Intent mTrackIntent;
    private Bundle mCurrentTrack;
    private static int currentTrackPosition = 0;
    private static byte [] pause_payload = {0,2,0};
    private static byte [] play_payload = {0,11,0};
    private static Intent cmdPauseIntentOn = Utils.genereateVhclCmd(805, pause_payload, true, 500, false,"");
    private static Intent cmdPlayIntentOn = Utils.genereateVhclCmd(805, play_payload, true, 500, false,"");
    private static Intent cmdPauseIntentOff = Utils.genereateVhclCmd(805, pause_payload, true, 500, true,"");
    private static Intent cmdPlayIntentOff = Utils.genereateVhclCmd(805, play_payload, true, 500, true,"");
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
            String mutator = intent.getStringExtra("mutator");
            VehicleCommand mVehicleCommand = new VehicleCommand(repeat,interval,message);
            mVehicleCommand.setMutator(mutator);
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
            registerReceiver(mTrackPosReceiver, new IntentFilter(PowerampAPI.ACTION_TRACK_POS_SYNC));
            registerReceiver(mTrackStatusReceiver, new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED));
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

    private BroadcastReceiver mTrackPosReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra("pos", 0);
            mAMCurrentTrack.setPosition(pos);
        }
    };

    private BroadcastReceiver mTrackStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("paused", true)) {
                mAMCurrentTrack.pause();
            }
            else {
                mAMCurrentTrack.play();
            }
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
        try {
            unregisterReceiver(mCommandReceiver);
            unregisterReceiver(mConnectedThreadIsReady);
            unregisterReceiver(mTrackReceiver);
            unregisterReceiver(mTrackPosReceiver);
            unregisterReceiver(mTrackStatusReceiver);
        }
        catch (Exception e){
            Log.d("BTIOSErvice","suppress exception on exit while unregistering recievers ");
        }
        super.onDestroy();
    }

    //user functions
    private void processTrackIntent() {
        mCurrentTrack = null;
        if(mTrackIntent != null) {
            mCurrentTrack = mTrackIntent.getBundleExtra(PowerampAPI.TRACK);
            if(mCurrentTrack != null) {
                int position = mCurrentTrack.getInt(PowerampAPI.Track.POS_IN_LIST);
                currentTrackPosition = position;
                mAMCurrentTrack.setPositionInList(currentTrackPosition);

                byte[] can_40000 = {4,0,0,0,(byte) position};
                Intent dn40000 = Utils.genereateVhclCmd(0xa4,can_40000,false,1000,false,"");
                sendBroadcast(dn40000);



                int duration = mCurrentTrack.getInt(PowerampAPI.Track.DURATION);
                String artist = mCurrentTrack.getString(PowerampAPI.Track.ARTIST);
                String album = mCurrentTrack.getString(PowerampAPI.Track.ALBUM);
                String title = mCurrentTrack.getString(PowerampAPI.Track.TITLE);
                Log.d("POWERAMP!", " " + position + " " + artist + " " + album + " " + title);
                //HARDCODED
                //PRIBITO GVOZDIAMY (tm)
                byte[] track_info_as_array = new byte[40];

                if (title.length() >= 20) {
                    title = title.substring(0, 20);
                }
                if (artist.length() >= 20) {
                    artist = artist.substring(0, 20);
                }
                for (int i = 0; i < 20; i++) {
                    try {
                        track_info_as_array[i] = (byte) artist.charAt(i);
                    } catch (StringIndexOutOfBoundsException e) {
                        track_info_as_array[i] = 0;
                    }
                    try {
                        track_info_as_array[20 + i] = (byte) title.charAt(i);
                    } catch (StringIndexOutOfBoundsException e) {
                        track_info_as_array[20 + i] = 0;
                    }
                }

                int can_address = 0xa4;
                //rude fisrt string formatting
                byte msg[] = {16, 44, 32, 0, (byte) 136, (byte) position, (byte) artist.charAt(0), (byte) artist.charAt(1)};
                Intent cmdUpIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                cmdUpIntent.putExtra("address", can_address);
                cmdUpIntent.putExtra("repeat", false);
                cmdUpIntent.putExtra("interval", 0);
                ArrayList payload = new ArrayList();
                payload.add(msg);
                cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                sendBroadcast(cmdUpIntent);

                byte _09f_control[] = {48,0,10};
                Intent _9fintent = Utils.genereateVhclCmd(0x9f,_09f_control,false,1000,false,"");
                sendBroadcast(_9fintent);




                    byte[] prefix = {33, 34, 35, 36, 37, 38};
                    int j = 2;
                    for (int i = 0; i < 6; i++) {
                        if (i > 4) {
                            msg = new byte[4];
                            msg[0] = prefix[i];
                            for (int k = 1; k < 4; k++) {
                                msg[k] = track_info_as_array[j];
                                j++;
                            }
                        } else {
                            msg = new byte[8];
                            msg[0] = prefix[i];
                            for (int k = 1; k < 8; k++) {
                                msg[k] = track_info_as_array[j];
                                j++;
                            }
                        }


                        cmdUpIntent.putExtra("address", can_address);
                        cmdUpIntent.putExtra("repeat", false);
                        cmdUpIntent.putExtra("interval", 0);
                        payload = new ArrayList();
                        payload.add(msg);
                        cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                        sendBroadcast(cmdUpIntent);
                    }
                }

        }
    }
}
