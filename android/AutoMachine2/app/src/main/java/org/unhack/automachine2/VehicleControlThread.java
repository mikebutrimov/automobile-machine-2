package org.unhack.automachine2;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by unhack on 10/6/16.
 */

public class VehicleControlThread extends Thread {
    private Context mContext;
    private ConnectedThread mConnectedThread;
    private ArrayList<VehicleCommand> commandQueue = new ArrayList<>();
    public boolean running = true;
    public VehicleControlThread(Context context, ConnectedThread thread){
        this.mContext = context;
        this.mConnectedThread = thread;
    }

    public void putCommand(VehicleCommand cmd){
        Log.d("QUEUE", "PUT COMMAND");
        commandQueue.add(cmd);
    }

    public void removeCommand(VehicleCommand cmd){
        for (Iterator<VehicleCommand> iterator = commandQueue.iterator(); iterator.hasNext();){
            VehicleCommand command = iterator.next();
            if (cmd.getMessage().equals(command.getMessage())){
                iterator.remove();
            }
        }
    }

    public synchronized void removeFiredCommands(){
        //remove non-repeated and fired commands
        for (Iterator<VehicleCommand> iterator = commandQueue.iterator(); iterator.hasNext();){
            VehicleCommand cmd = iterator.next();
            if (!cmd.isRepeated() && cmd.isFired()){
                iterator.remove();
            }
        }
    }

    public synchronized void processQueue() {
        if (mConnectedThread != null) {
            for (VehicleCommand cmd : this.commandQueue) {
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
            try {
                sleep(100); // why not?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void halt(){
        Log.d("QUEUE", "STOPPING queue daemon");
        this.running = false;
    }

}
