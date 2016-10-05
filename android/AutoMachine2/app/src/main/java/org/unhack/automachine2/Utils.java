package org.unhack.automachine2;

import android.util.Log;

import com.google.protobuf.ByteString;

import org.unhack.automachine2.Msg.controlMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by unhack on 10/4/16.
 */

public class Utils {
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
