package org.unhack.automachine2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public int REQUEST_ENABLE_BT = 1;
    public static final String INTENT_FILTER_UPDATE_UI = "org.unhack.automachine2.BROADCAST_FILTER_UPDATE_UI";
    public static final String INTENT_FILTER_INPUT_COMMAND  = "org.unhack.automachine2,BROADCAST_FILTER_INPUT_COMMAND";
    public static final String INTENT_FILTER_CONNECTEDTHREAD_READY = "org.unhack.automachine2,BROADCAST_FILTER_CONNECTEDTHREAD_ISREADY";
    public Spinner mBtDevSpinner;
    public static String currentBtDevice = null;
    public TextView ioTextView;


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String payload = intent.getStringExtra("payload");
            ioTextView.setText("");
            ioTextView.setText(payload);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ioTextView = (TextView) findViewById(R.id.ioText);
        registerReceiver(myReceiver, new IntentFilter(INTENT_FILTER_UPDATE_UI));
        //set onclick listeners
        Button ok_button = (Button) findViewById(R.id.button_ok);
        ok_button.setOnClickListener(mOnClickListener);
        Button exit_button = (Button) findViewById(R.id.button_exit);
        exit_button.setOnClickListener(mOnClickListener);
        Button left_button = (Button) findViewById(R.id.button_left);
        left_button.setOnClickListener(mOnClickListener);
        Button right_button = (Button) findViewById(R.id.button_right);
        right_button.setOnClickListener(mOnClickListener);
        Button up_button = (Button) findViewById(R.id.button_up);
        up_button.setOnClickListener(mOnClickListener);
        Button down_button = (Button) findViewById(R.id.button_down);
        down_button.setOnClickListener(mOnClickListener);
        Button menu_button = (Button) findViewById(R.id.button_menu);
        menu_button.setOnClickListener(mOnClickListener);
        Button dark_button = (Button) findViewById(R.id.button_dark);
        dark_button.setOnClickListener(mOnClickListener);
        Button hu_button = (Button) findViewById(R.id.button_hu);
        hu_button.setOnClickListener(mOnClickListener);
        Button huoff_button = (Button) findViewById(R.id.button_huoff);
        huoff_button.setOnClickListener(mOnClickListener);

        //get bt adaptors or check if there are any
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this,"There is no BT on your system. Sorry",Toast.LENGTH_LONG).show();
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                mBtDevSpinner = (Spinner) findViewById(R.id.spinner_bt_devices);

                final HashMap<String,BluetoothDevice> mBtHashMap = new HashMap<>();

                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    mBtHashMap.put(device.getName(),device);
                }
                final ArrayAdapter<String> mBtAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,new ArrayList<String>(mBtHashMap.keySet()));

                mBtDevSpinner.setAdapter(mBtAdapter);
                mBtDevSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        //Toast.makeText(getApplicationContext(), mBtAdapter.getItem(i) + " is selected", Toast.LENGTH_SHORT).show();
                        currentBtDevice = mBtAdapter.getItem(i);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        }

    }

    public void startConn(View v){
        Switch mSwIo = (Switch) findViewById(R.id.sw_io);
        Intent startBtService = new Intent(getApplicationContext(),BtIOService.class);
        if (mSwIo.isChecked()){
            Toast.makeText(getApplicationContext(),"starting service with "+currentBtDevice, Toast.LENGTH_SHORT).show();
            startService(startBtService);
        }
        else {
            stopService(startBtService);
        }
    }

    //onclick listener
    //PRIBITO GVOZDAMY(tm)
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int can_address;
            byte[] pld;
            ArrayList payload;
            switch (v.getId()) {
                case R.id.button_ok:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,64,0,0,0};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_exit:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,16,0,0,0};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_menu:
                    can_address = 0x3e5;
                    pld = new byte[] {64,0,0,0,0,0};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_dark:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,4,0,0,0};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_left:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,0,0,0,1};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_right:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,0,0,0,4};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_down:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,0,0,0,64};
                    sendCommand(can_address,pld);
                    break;
                case R.id.button_up:
                    can_address = 0x3e5;
                    pld = new byte[] {0,0,0,0,0,16};
                    Intent cmdUpIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                    cmdUpIntent.putExtra("address", can_address);
                    cmdUpIntent.putExtra("repeat",false);
                    cmdUpIntent.putExtra("interval",0);
                    payload = new ArrayList();
                    payload.add(pld);
                    cmdUpIntent.putParcelableArrayListExtra("payload", payload);
                    sendBroadcast(cmdUpIntent);
                    //sendCommand(can_address,pld);
                    break;
                case R.id.button_hu:
                    can_address = 0x165;
                    pld = new byte[] {(byte)200,(byte)192,32,0};
                    Intent cmdHUonIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                    cmdHUonIntent.putExtra("address", can_address);
                    cmdHUonIntent.putExtra("repeat",true);
                    cmdHUonIntent.putExtra("interval",90);
                    payload = new ArrayList();
                    payload.add(pld);
                    cmdHUonIntent.putParcelableArrayListExtra("payload", payload);
                    sendBroadcast(cmdHUonIntent);
                    //sendCommand(can_address,pld);
                    break;
                case R.id.button_huoff:
                    can_address = 0x165;
                    pld = new byte[] {(byte)200,(byte)192,32,0};
                    Intent cmdHUoffIntent = new Intent(MainActivity.INTENT_FILTER_INPUT_COMMAND);
                    cmdHUoffIntent.putExtra("address", can_address);
                    cmdHUoffIntent.putExtra("repeat",true);
                    cmdHUoffIntent.putExtra("interval",100);
                    cmdHUoffIntent.putExtra("delete", true);
                    payload = new ArrayList();
                    payload.add(pld);
                    cmdHUoffIntent.putParcelableArrayListExtra("payload", payload);
                    sendBroadcast(cmdHUoffIntent);
                    //sendCommand(can_address,pld);
                    break;
            }
        }
    };

    public  void sendCommand(int can_address, byte[] pld){
        ArrayList payload = new ArrayList();
        payload.add(0,pld);
        Intent cmdIntent = new Intent(this, BtIOService.class);
        cmdIntent.putExtra("canaddress", can_address);
        cmdIntent.putExtra("payload", payload);
        startService(cmdIntent);
    }



    @Override
    public void onDestroy(){
        this.unregisterReceiver(myReceiver);
        super.onDestroy();
    }
}
