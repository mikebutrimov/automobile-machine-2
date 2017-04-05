package org.unhack.automachine2;

import android.util.Log;

import com.google.protobuf.ByteString;

import org.unhack.automachine2.Msg.controlMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by unhack on 10/4/16.
 */
interface iMutator {
    Msg.controlMessage runCommand(Msg.controlMessage msg);
}

public class Utils {
    public static HashMap<String,iMutator> mutators = new HashMap();

    public static Msg.controlMessage track_position_mutator(Msg.controlMessage msg){
        //unsigned char cd_time[6] = {1, 255, 255, 0, 0,  0};
        //pos in list /total time in min / total time in sec / pos min / pos sec
        byte[] can_payload = msg.getCanPayload(0).toByteArray();
        int min = can_payload[3];
        int sec = can_payload[4];
        int totaltime = min*60+sec;
        totaltime = totaltime+1;
        min = totaltime / 60;
        sec = totaltime % 60;
        can_payload[3] = (byte) min;
        can_payload[4] = (byte) sec;
        ArrayList<byte[]> payload = new ArrayList<>();
        payload.add(can_payload);
        msg = createMessage(msg.getCanAddress(), payload);
        return msg;
    }


    public static HashMap<String,iMutator> getMutators() {

        mutators.put("trackPosition", new iMutator() {
            @Override
            public Msg.controlMessage runCommand(Msg.controlMessage msg) {
                return track_position_mutator(msg);
            }
        });

        return mutators;
    }

    public static Msg.controlMessage createMessage(int can_address, ArrayList<byte[]> payload){
        int payload_length = payload.size();
        if (payload_length == 0 || can_address == 0){
            //Log.d("Utils.create", "Zero message size || 0 as can address");
            return null;
        }
        controlMessage.Builder message = controlMessage.newBuilder();
        message.setCanAddress(can_address);
        //Log.d("Utils", "payload size: " +String.valueOf(payload_length));
        //Log.d("Utils", "PAYLOAD [0]" + payload.get(0).toString());
        for (int i = 0; i< payload_length; i++){
            byte[] ololo = payload.get(i);
            message.addCanPayload(ByteString.copyFrom(ololo));
        }
        //og.d("Utils.create" , "Can payload count : " + String.valueOf(message.getCanPayloadCount()));
        Log.d("Utils.create", "Message size " + String.valueOf(message.build().getSerializedSize()));
        return message.build();
    }
}
