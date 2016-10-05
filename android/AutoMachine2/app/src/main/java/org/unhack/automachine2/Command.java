package org.unhack.automachine2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by unhack on 10/5/16.
 */



public class Command {
    private static final int SHORT_PRESS = 541;
    private static final int LONG_PRESS = 916;
    private static final int RELEASED = 619;
    private int address;
    private byte[] payload;
    private int status = RELEASED;
    private long timer = 0;
    private Intent shortPressedIntent;
    private Intent longPressedIntent = null;
    private Intent longReleasedIntent = null;
    private Context mContext;
    private  int count = 0;
    private String string_payload;
    private static final int THRESHOLD = 110;

    public Command(int addr, byte[] pld, Intent shortPressedIntent, Context context){
        this.address  = addr;
        this.payload = pld;
        this.shortPressedIntent = shortPressedIntent;
        this.mContext = context;
        string_payload = String.valueOf(addr);
        for (int i = 0; i< pld.length; i ++){
            string_payload = string_payload + " " + String.valueOf((byte)pld[i]);
        }
    }

    public Command(int addr, byte[] pld, Intent shortPressedIntent, Intent longPressedIntent,
                   Intent longReleasedIntent, Context context){
        this.address  = addr;
        this.payload = pld;
        this.shortPressedIntent = shortPressedIntent;
        if (longPressedIntent!= null) this.longPressedIntent = longPressedIntent;
        if (longReleasedIntent!= null) this.longReleasedIntent  = longReleasedIntent;
        this.mContext = context;
        string_payload = String.valueOf(addr);
        for (int i = 0; i< pld.length; i ++){
            string_payload = string_payload + " " + String.valueOf((byte)pld[i]);
        }

    }


    public int getAddress(){
        return this.address;
    }
    public byte[] getPayload(){
        return this.payload;
    }

    public  void fire(boolean isReal){
        if (isReal) this.count++;
        //released and first fire
        if (status == RELEASED && isReal) {
            //first time firinng
            //fire up timer
            this.timer = System.currentTimeMillis();
            this.status = SHORT_PRESS;
            return;
        }
        if (status == SHORT_PRESS){
            if (System.currentTimeMillis() - timer < THRESHOLD && isReal) {
                if (this.count == 2) {
                    //Not long press
                    Log.d("SOME TAG", "DO NOTHING");
                } else {
                    //Long press
                    if (longPressedIntent != null) mContext.startService(longPressedIntent);
                    this.status = LONG_PRESS;
                    this.timer = System.currentTimeMillis();
                    Intent ioTextInten = new Intent(MainActivity.INTENT_FILTER);
                    ioTextInten.putExtra("payload", string_payload + " fired LONG \n");
                    mContext.sendBroadcast(ioTextInten);
                }
            }
            else {
                if (System.currentTimeMillis() - timer > THRESHOLD && isReal) {
                    //go out from SHORT state
                    mContext.startService(shortPressedIntent);
                    Intent ioTextInten = new Intent(MainActivity.INTENT_FILTER);
                    ioTextInten.putExtra("payload", string_payload + " fired SHORT \n");
                    mContext.sendBroadcast(ioTextInten);
                    this.status = RELEASED;
                    this.count = 0;
                }
            }
        }
        if (status == LONG_PRESS){
            if (System.currentTimeMillis() - timer < THRESHOLD && isReal){
                //Do nothing, still oin long press
            }
            else {

                this.status = RELEASED;
                this.count = 0;
                if (longReleasedIntent != null) mContext.startService(longReleasedIntent);
                Intent ioTextInten = new Intent(MainActivity.INTENT_FILTER);
                ioTextInten.putExtra("payload", string_payload + " released LONG \n");
                mContext.sendBroadcast(ioTextInten);
            }
        }
    }
}


