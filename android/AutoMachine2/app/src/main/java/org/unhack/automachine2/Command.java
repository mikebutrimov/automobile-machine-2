package org.unhack.automachine2;

import android.content.Context;
import android.content.Intent;

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

    public Command(int addr, byte[] pld, Intent shortPressedIntent, Context context){
        this.address  = addr;
        this.payload = pld;
        this.shortPressedIntent = shortPressedIntent;
        this.mContext = context;
    }

    public Command(int addr, byte[] pld, Intent shortPressedIntent, Intent longPressedIntent,
                   Intent longReleasedIntent, Context context){
        this.address  = addr;
        this.payload = pld;
        this.shortPressedIntent = shortPressedIntent;
        if (longPressedIntent!= null) this.longPressedIntent = longPressedIntent;
        if (longReleasedIntent!= null) this.longReleasedIntent  = longReleasedIntent;
        this.mContext = context;
    }


    public int getAddress(){
        return this.address;
    }
    public byte[] getPayload(){
        return this.payload;
    }

    public synchronized void fire(boolean isReal){
        if (isReal) this.count++;
        //released and first fire
        if (status == RELEASED && isReal) {
            //first time firinng
            //fire up timer
            this.timer = System.currentTimeMillis();
            this.status = SHORT_PRESS;
            mContext.startService(shortPressedIntent);
            return;
        }
        if (status == SHORT_PRESS){
            if (System.currentTimeMillis() - timer < 110 && isReal) {
                if (this.count <= 2) {
                    //Not long press
                    //do Nothing
                } else {
                    //Long press
                    if (longPressedIntent != null) mContext.startService(longPressedIntent);
                    this.status = LONG_PRESS;
                    this.timer = System.currentTimeMillis();
                }
            }
            else {
                this.status = RELEASED;
                this.count = 0;
            }
        }
        if (status == LONG_PRESS){
            if (System.currentTimeMillis() - timer < 110 && isReal){
                //Do nothing, still oin long press
            }
            else {
                this.status = RELEASED;
                this.count = 0;
                if (longReleasedIntent != null) mContext.startService(longReleasedIntent);
            }
        }
    }
}


