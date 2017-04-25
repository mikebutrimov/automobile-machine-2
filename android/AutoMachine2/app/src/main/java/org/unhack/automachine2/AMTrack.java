package org.unhack.automachine2;

import android.content.Context;
import android.content.Intent;

/**
 * Created by unhack on 4/25/17.
 */

public class AMTrack {

    private boolean isPlaying;
    private int position;
    private int positionInList = 1;
    private Context context;
    private static byte [] pause_payload = {0,2,0};
    private static byte [] play_payload = {0,11,0};
    private static Intent cmdPauseIntentOn = Utils.genereateVhclCmd(805, pause_payload, true, 500, false,"");
    private static Intent cmdPlayIntentOn = Utils.genereateVhclCmd(805, play_payload, true, 500, false,"");
    private static Intent cmdPauseIntentOff = Utils.genereateVhclCmd(805, pause_payload, true, 500, true,"");
    private static Intent cmdPlayIntentOff = Utils.genereateVhclCmd(805, play_payload, true, 500, true,"");


    public AMTrack(Context context) {
        this.context = context;
    }

    public void play(){
        this.context.sendBroadcast(cmdPauseIntentOff);
        this.context.sendBroadcast(cmdPlayIntentOn);
        this.isPlaying = true;
    }
    public void pause() {
        this.context.sendBroadcast(cmdPlayIntentOff);
        this.context.sendBroadcast(cmdPauseIntentOn);
        byte[] can_payload = {1, (byte) 255, (byte) 255, 0, 0, 0};
        can_payload[0] = (byte) this.positionInList;
        Intent cmdIntentPosOff = Utils.genereateVhclCmd(933, can_payload, true, 1000, true, "trackPosition");
        this.context.sendBroadcast(cmdIntentPosOff);


        this.isPlaying = false;
    }

    public boolean isPlaying(){
        return this.isPlaying;
    }

    public void setPosition(int pos){
        this.position = pos;
        int min = this.position / 60;
        int sec = this.position % 60;
        byte[] can_payload = {1, (byte) 255, (byte) 255, 0, 0, 0};
        can_payload[3] = (byte) min;
        can_payload[4] = (byte) sec;
        can_payload[0] = (byte) this.positionInList;
        if (this.isPlaying()){
            Intent cmdIntentPosOff = Utils.genereateVhclCmd(933, can_payload, true, 1000, true, "trackPosition");
            Intent cmdIntentPosOn = Utils.genereateVhclCmd(933, can_payload, true, 1000, false, "trackPosition");
            this.context.sendBroadcast(cmdIntentPosOff);
            this.context.sendBroadcast(cmdIntentPosOn);
        }

    }
    public int getPosition(){
        return this.position;
    }

    public void setPositionInList(int posInList){
        this.positionInList = posInList;
        this.setPosition(0);
    }
}
