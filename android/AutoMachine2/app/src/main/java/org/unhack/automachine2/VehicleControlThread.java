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
            if (cmd.getMessage().equals(command.getMessage()) || cmd.getMessage().getCanAddress() == command.getMessage().getCanAddress()){
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
                if (cmd.isRepeated()) {
                    if ((cmd.getInterval() + cmd.getLastExecutionTime()) < System.currentTimeMillis()) {
                        Msg.controlMessage buf_msg =  cmd.getMessage();
                        String mutator = cmd.getMutator();
                        if (mutator != null){
                            buf_msg = Utils.getMutators().get(mutator).runCommand(buf_msg);
                            cmd.setMessage(buf_msg);
                        }

                        mConnectedThread.writeMessage(cmd.getMessage());
                        cmd.setLastExecutionTime(System.currentTimeMillis());
                    }

                } else {
                    if (!cmd.isRepeated() && !cmd.isFired()) {
                        //exec!
                        mConnectedThread.writeMessage(cmd.getMessage());
                        cmd.fire();
                        //cmd.setLastExecutionTime(System.currentTimeMillis());
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
