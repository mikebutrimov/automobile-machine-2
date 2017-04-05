package org.unhack.automachine2;

import android.util.Log;
import android.util.StringBuilderPrinter;

/**
 * Created by unhack on 10/6/16.
 */

public class VehicleCommand {
    private boolean repeatness;
    private boolean isFired = false;
    private int interval;
    private Msg.controlMessage message;
    private long lastExecutionTime = 0;
    private String mutator;


    public VehicleCommand(Boolean repeatness, int interval, Msg.controlMessage message){
        this.repeatness = repeatness;
        this.interval = interval;
        this.message = message;
    }

    public boolean isRepeated(){
        return this.repeatness;
    }

    public int getInterval(){
        return this.interval;
    }

    public Msg.controlMessage getMessage(){
        return this.message;
    }
    public void fire(){
        this.isFired = true;
    }
    public boolean isFired(){
        return this.isFired;
    }
    public void setLastExecutionTime(long time){
        this.lastExecutionTime = time;
    }
    public long getLastExecutionTime(){
        return this.lastExecutionTime;
    }

    public String getMutator(){
        if (mutator.isEmpty())  return null;
        return this.mutator;
    }

    public void setMutator(String mutator){
        this.mutator = mutator;
    }
}
