package org.unhack.automachine2;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.unhack.automachine2.Msg.controlMessage;
import com.maxmpz.poweramp.player.PowerampAPI;

import static org.unhack.automachine2.MainActivity.INTENT_FILTER_CONNECTEDTHREAD_READY;

/**
 * Created by unhack on 10/1/16.
 */

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final static int PLEN = 33;
    private final static int SOFLEN = 35;
    private Context mContext;
    private boolean running = true;
    private boolean housekeeping = true;
    private boolean read = true;
    private final CommandProcessor cmdProcessor;
    private controlMessage message;
    public ConnectedThread(BluetoothSocket socket, Context context) {
        mContext = context;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.cmdProcessor = new CommandProcessor(mContext);

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        Log.d("CONNECTED Thread", "In Run");
        //send broadcast that we are ready
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        //start housekeeping thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (housekeeping){
                    cmdProcessor.houseKeeping();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // Keep listening to the InputStream until an exception occurs
        while (running) {
            try {
                // Read from the InputStream
                byte[] control = new byte[1];
                int zero_count = 0;
                int read_count = 0;
                //init counts with 0 value
                //if read_count is larger than zero count - we read non-continious zeros and must start again.
                //when we read more than 34 zeros, we start waiting for PLEN const (PACKET LEN)
                //on PLEN event  dtop reading buffer
                zero_count = 0;
                read_count = 0;
                int messageLen = 0;
                read = true;
                while (read) {
                    mmInStream.read(control, 0, 1);
                    int byte_readed = (int) (control[0]);
                    //Log.d("LOOP VARS", "BYTE: " + String.valueOf(byte_readed) + " ZEROS: "
                    //        + String.valueOf(zero_count) + " READS: " + String.valueOf(read_count));
                    read_count++;
                    if (byte_readed == 0) {
                        zero_count++;
                    }
                    if (zero_count > PLEN) {
                        //Log.d("LOOP PLEN", "PLEN SEARCH");
                        //search for PLEN
                        if (byte_readed != 0) {
                            //Log.d("LOOP PLEN", "PLEN EVENT");
                            messageLen = byte_readed;
                            read = false;
                        }
                    }
                    if (read_count - zero_count > 0) {
                        zero_count = 0;
                        read_count = 0;
                    }
                }
                while (mmInStream.available() < messageLen) {
                    //wait for pizDATA
                }
                bytes = mmInStream.read(buffer, 0, messageLen);
                byte[] mPayload = new byte[bytes];
                for (int i = 0; i < bytes; i++) {
                    mPayload[i] = buffer[i];
                    //Log.d("BUFFER", String.valueOf((int) mPayload[i]));
                }
                try {
                    message = controlMessage.parseFrom(mPayload);
                    Log.d("PROTOBUF", message.toString());
                    int can_address = message.getCanAddress();
                    /*String string_payload = String.valueOf(can_address);
                    for (int i = 0; i < message.getCanPayload(0).size(); i++) {
                        string_payload = string_payload + " " + String.valueOf(message.getCanPayload(0).byteAt(i));
                    }
                    string_payload = string_payload + "\n";
                    //Log.d("String Paylod", string_payload);
                    Intent ioTextInten = new Intent(MainActivity.INTENT_FILTER);
                    ioTextInten.putExtra("payload", string_payload);*/
                    //push command to command processor
                    cmdProcessor.fireCommand(can_address,message.getCanPayload(0).toByteArray());
                    //mContext.sendBroadcast(ioTextInten);

                } catch (InvalidProtocolBufferException e) {
                    //e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public  void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    public  void writeMessage(controlMessage message)  {

        byte buffer[] =  message.toByteArray();
        int messageSize = buffer.length;
        Log.d("writeMessage", "Payload: " + Arrays.toString(message.getCanPayload(0).toByteArray()) );

        byte[] sof_buffer = new byte[SOFLEN];
        for (int i = 0; i< SOFLEN-2; i++){
            sof_buffer[i] = 0;
        }
        sof_buffer[SOFLEN-1] = (byte) messageSize;

        try {
            mmOutStream.write(sof_buffer);
            mmOutStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    public void halt(){
        Log.d("CONNECTED","Halting connection thread");
        cancel();
        this.running = false;
        this.housekeeping = false;

    }

}