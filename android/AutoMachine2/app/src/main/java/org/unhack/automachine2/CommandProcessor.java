package org.unhack.automachine2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by unhack on 10/5/16.
 */

public class CommandProcessor {
    private ArrayList<Command> commands = new ArrayList<>();
    private Context mContext;

    public CommandProcessor(Context context){
        this.mContext = context;
        //init command storage
        //for 0x21f address we need byte[3] command payload
        byte[] payload = new byte[3];
        //Play/Pause
        payload[0] = (byte)2;
        commands.add(new Command(0x21f,payload,
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.TOGGLE_PLAY_PAUSE).setPackage(PowerampAPI.PACKAGE_NAME),
                mContext));

        //Forward. Has all three intents
        payload = new byte[3];
        payload[0] = (byte)128;
        commands.add(new Command(0x21f,payload,
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT).setPackage(PowerampAPI.PACKAGE_NAME),
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.BEGIN_FAST_FORWARD).setPackage(PowerampAPI.PACKAGE_NAME),
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.END_FAST_FORWARD).setPackage(PowerampAPI.PACKAGE_NAME),
                mContext));

        //backward
        payload = new byte[3];
        payload[0] = (byte)64;
        commands.add(new Command(0x21f,payload,
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS).setPackage(PowerampAPI.PACKAGE_NAME),
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.BEGIN_REWIND).setPackage(PowerampAPI.PACKAGE_NAME),
                new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.END_REWIND).setPackage(PowerampAPI.PACKAGE_NAME),
                mContext));

    }


    //very very rude idea
    //hashmap is better, but i must thinkout uniqe keys to commands
    public void fireCommand(int address, byte[] payload){

        for (Command cmd: this.commands){
            if (cmd.getAddress() == address && Arrays.equals(cmd.getPayload(), payload)){
                //here we are
                cmd.fire(true);
            }
        }
    }

    public void houseKeeping(){
        //fire up all commands with false  key to reset states if needed
        for (Command cmd: this.commands){
            cmd.fire(false);
        }
    }
}
