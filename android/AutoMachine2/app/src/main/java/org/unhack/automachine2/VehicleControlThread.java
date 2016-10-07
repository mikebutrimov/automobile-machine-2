package org.unhack.automachine2;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by unhack on 10/6/16.
 */

public class VehicleControlThread extends Thread {
    public ConnectedThread mConnectedThread;
    private ArrayList<VehicleCommand> commandQueue = new ArrayList<>();
    public boolean running = true;
    public Context mContext;
    private boolean lock = false;
    public VehicleControlThread(Context context, ConnectedThread thread){
        this.mContext = context;
        this.mConnectedThread = thread;
    }

    private synchronized boolean lock(){
        boolean wasLocked = lock;
        this.lock = true;
        return wasLocked;
    }
    private synchronized void releaseLock(){
        this.lock = false;
    }

    public void putCommand(VehicleCommand cmd){
        while (lock()){
        }
        commandQueue.add(cmd);
        releaseLock();
    }

    public void removeCommand(VehicleCommand cmd){
        while (lock()){
        }
        for (Iterator<VehicleCommand> iterator = commandQueue.iterator(); iterator.hasNext();){
            VehicleCommand command = iterator.next();
            if (cmd.getMessage().equals(command.getMessage())){
                iterator.remove();
            }
        }
        releaseLock();
    }

    public  void removeFiredCommands(){
        while (lock()){
        }
        //remove non-repeated and fired commands
        for (Iterator<VehicleCommand> iterator = commandQueue.iterator(); iterator.hasNext();){
            VehicleCommand cmd = iterator.next();
            if (!cmd.isRepeated() && cmd.isFired()){
                iterator.remove();
            }
        }
        releaseLock();
    }

    public  void processQueue() {
        if (mConnectedThread != null) {
            while (lock()){
            }
            for (Iterator<VehicleCommand> iterator = commandQueue.iterator(); iterator.hasNext();){
                VehicleCommand cmd = iterator.next();
                if (!cmd.isRepeated() && !cmd.isFired()) {
                    Msg.controlMessage msg = cmd.getMessage();
                    mConnectedThread.writeMessage(msg);
                    cmd.fire();
                } else {
                    if ((cmd.getInterval() + cmd.getLastExecutionTime()) < System.currentTimeMillis()) {
                        //exec!
                        mConnectedThread.writeMessage(cmd.getMessage());
                        cmd.setLastExecutionTime(System.currentTimeMillis());
                    }
                }
            }
        releaseLock();
        }
        else {
            Log.d("QUEUE","Waiting for connected thread to be fired up ");
        }
    }

    public void run() {
        Log.d("QUEUE", "STARTED queue daemon");
        while (running) {
            processQueue();
            removeFiredCommands();
        }
    }

    public void halt(){
        Log.d("QUEUE", "STOPPING queue daemon");
        this.running = false;
    }

}
