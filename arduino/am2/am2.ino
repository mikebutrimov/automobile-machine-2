#define GPIO2_PREFER_SPEED 1
#include "arduino2.h"
#include <mcp_can.h>
#include <pb_encode.h>
#include <pb_decode.h>
#include "msg.pb.h"
#include "ainet.h"


//interrupt service routine variables
int counts = 0;
const int bytes = 11;
byte byte_val = 0;
byte vals[bytes*8];
byte byte_vals[bytes];
bool ack_buffer[8];
int readyForNext = 1;
const int BITVAL_THRESHOLD_HIGH = 6;
const int SOF_THRESHOLD = 15;
const int BRAKE = 10000;

//control message frame
byte sop[35];
int messageLen = 0;
const byte PLEN = 33;  //minimum packet length
const byte SOPLEN = 35;  //start of packet length

//CAN variables
const int HEARTBEAT_SIZE = 2;

struct CAN_COMMAND {
  short address;
  short bytes;
  int putInTime;
  int delayTime;
  short payload[8];
};

CAN_COMMAND heartbeat[HEARTBEAT_SIZE] = {
  {997,6,0,500,{0,0,0,0,0,0}}, //buttons zeroing press
  {357,4,0,100,{200,192,32,32}}, //turn display on as in mp3 mode
};

MCP_CAN CAN(10); //Can bus shield on spi pin 10

int vol_index = 15; // default volume index to restore

void isr_read_msg() {
  //here we start with RISING on SOF but must check it
  while (digitalRead2(AINETIN) == HIGH  && counts < BRAKE) {
    counts++;
  }
  if (counts<SOF_THRESHOLD) {
    //we got an ack of someone's packet. ignore it
    delayMicroseconds(192);
    return;
  }
  //SOF ends with LOW reading. wait for HIGH again and count it
  for (int i = 0; i< bytes*8; i++) {
    counts = 0;
    while (digitalRead2(AINETIN) == LOW && counts < BRAKE) {
      counts++;
    }
    counts = 0;
    while (digitalRead2(AINETIN) == HIGH && counts < BRAKE ) {
      counts++;
    }
    if (counts < BITVAL_THRESHOLD_HIGH) {
      vals[i] = 1;
    }
    else {
      vals[i] = 0;
    }
    byte_val = (byte_val<<1)|vals[i];
    if (i % 8 ==7) {
      byte_vals[i/8] = byte_val;
      byte_val = 0;
    }
  }

  if (byte_vals[0] == 0x02) {
    //we must count 40 micros from the front of last impulse
    //so it depends on value of last bit
    if (vals[(bytes*8)-1] == 0) {
      delayMicroseconds(23);
    }
    else {
      delayMicroseconds(31);
    }
    //send ack
    fastSend(ack_buffer,8,1);
    //start init seq. by setting ainetAck to true
    //this means that we get request from processor to headunit (from 0x40 to 0x02)
    if (byte_vals[1] == 0x40 && byte_vals[2] == 0x90 && byte_vals[3] == 0x67) {
      ainetAck = true;
    }
  }

  //some commented out code to output last captured packet
  for (int i = 0; i< bytes; i++) {
    Serial.print(byte_vals[i],HEX);
    Serial.print(" ");
  }
  Serial.println();
}


//Volume control
void volUp() {
  if ((vol_index+1) < 36) {
    vol_index = vol_index + 1;
    ainet_commands[7][3] = vol[vol_index];
    crc(ainet_commands[7]);
    sendAiNetCommand(ainet_commands[7],11);
    byte buf[1];
    buf[0]= (byte) vol_index;
    if (vol_index < 32) {
      byte status = CAN.sendMsgBuf(0x1a5, 0, 1, buf);
      buf[0] = (byte) (224+vol_index);
      CAN.sendMsgBuf(0x1a5, 0, 1, buf);
    }
  }
}

void volDown() {
  if ((vol_index-1) >= 0) {
    vol_index = vol_index - 1;
    ainet_commands[7][3] = vol[vol_index];
    crc(ainet_commands[7]);
    sendAiNetCommand(ainet_commands[7],11);
    byte buf[1];
    buf[0]= (byte) vol_index;
    if (vol_index < 32) {
      byte status = CAN.sendMsgBuf(0x1a5, 0, 1, buf);
      buf[0] = (byte)(224+vol_index);
      CAN.sendMsgBuf(0x1a5, 0, 1, buf);
    }
  }
}

void vUpVdown(unsigned char *can_buf) {
  if (can_buf[0] == 4) {
    volDown();
  }
  if (can_buf[0] == 8) {
    volUp();
  }
}
//End of volume control



//Read and write BT and CAN sections
void readOrder() {
  if (!Serial1.available()) return;
  //Read and find Start Of PAcket (SOP)
  int zero_count = 0;
  int read_count = 0;
  bool continue_read = true;
  while (continue_read) {
    char byte_readed = Serial1.read();
    if (byte_readed != -1) {
      read_count++;
      if (byte_readed == 0) {
        zero_count++;
      }
      if (zero_count > PLEN) {
        if (byte_readed != 0) {
          messageLen = byte_readed;
          continue_read = false;
        }
      }
      if (read_count - zero_count > 0) {
        read_count = 0;
        zero_count = 0;
      }
    }
    else {
    }
  }

  byte * proto_buf_message = new byte[messageLen];
  int received = 0;
  continue_read = true;
  while (continue_read) {
    int byte_readed = Serial1.read();
    if (byte_readed != -1) {
      proto_buf_message[received] = byte_readed;
      ++received;
    }
    else {}
    if (received >= messageLen) {
      continue_read = false;
    }
  }

  if (received == messageLen) {
    //go ahead
    controlMessage message = controlMessage_init_zero;
    pb_istream_t stream =
      pb_istream_from_buffer(proto_buf_message, messageLen);
    bool status;
    status = pb_decode(&stream, controlMessage_fields, &message);
    if (!status) {
      Serial.println("Error decoding message");
      delete[] proto_buf_message;
      return;
    }
    else {
      for (int i = 0; i< message.can_payload_count; i++) {
        for (int j = 0; j<message.can_payload[i].size ; j++) {
          Serial.print (message.can_payload[i].bytes[j], DEC);
          Serial.print (" ");
        }
        Serial.println();
      }
      //retransmitt message to can
      int canId = message.can_address;
      //security if to avoid writing garbage in can bus
      if (canId == 0x165 || canId == 0x3e5 || canId == 0x21f || canId == 0xa4 || canId == 933 || canId == 805) {
        for (int i = 0; i< message.can_payload_count; i++) {
          byte status = CAN.sendMsgBuf(canId,0,message.can_payload[i].size,message.can_payload[i].bytes);
          //cool down can bus shield
          delay(1);
          //^^ yes, it looks stupid but it works
        }
      }
    }
  }
  else {
    Serial.println("wrong packet length");
  }
  delete[] proto_buf_message;
}

void readCan() {
  //read data from CAN Bus
  unsigned char len = 0;
  unsigned char can_buf[8];
  int canId;
  if(CAN_MSGAVAIL == CAN.checkReceive()) {
    CAN.readMsgBuf(&len, can_buf);
    canId = (int) CAN.getCanId();
    if (canId == 0x21f && len != 0) {
      //process volum up and down buttons
      vUpVdown(can_buf);
      uint8_t buffer[64];
      size_t message_length;
      bool status;
      pb_ostream_t stream = pb_ostream_from_buffer(buffer, sizeof(buffer));
      controlMessage message = controlMessage_init_zero;
      message.can_address = canId;
      message.can_payload[0].size = len;
      for (int i = 0; i < len; i++) {
        message.can_payload[0].bytes[i] = can_buf[i];
      }
      message.can_payload_count = 1;
      status = pb_encode(&stream, controlMessage_fields, &message);
      sop[SOPLEN-1] = stream.bytes_written;
      Serial1.write(sop,SOPLEN);
      Serial1.write(buffer,stream.bytes_written);
    }
  }
}
//End of read and write BT and CAN Sections

//some can tools
void sendCmd(CAN_COMMAND cmd) {
  int b_count = cmd.bytes;
  byte * buffer = new byte[b_count];
  //copy useful bytes from command to buffer to send it in CAN
  for (int i = 0; i< b_count; i++) {
    buffer[i] = cmd.payload[i];
  }
  byte status = CAN.sendMsgBuf(cmd.address, 0, b_count,buffer);
  delete[] buffer;
}

void dispatcher() {
  for (int i = 0; i< HEARTBEAT_SIZE; i++) {
    if ( ((int)millis() - heartbeat[i].delayTime - heartbeat[i].putInTime) >= 0) {
      sendCmd(heartbeat[i]);
      heartbeat[i].putInTime = millis();
    }
  }
}

void batch_send(CAN_COMMAND * cmds, int len) {
  for (int i = 0; i< len; i++) {
    sendCmd(cmds[i]);
  }
}
//End of some can batch tools


void setup() {
  //init ainet pins
  pinMode2(AINETIN, INPUT);
  pinMode2(AINETOUT, OUTPUT);
  pinMode2(7, OUTPUT);
  //prepare uranus;
  attachInterrupt(digitalPinToInterrupt(AINETIN), isr_read_msg, RISING);
  Serial.begin(115200);
  Serial1.begin(9600);
  //generate sop
  for (int i = 0; i < SOPLEN-1; i++) {
    sop[i] = 0;
  }
  sop[SOPLEN-1] = PLEN;
  //generate ainet ack buffer
  byte b = 0x02;
  for (j=0; j<8; j++) {
    type=(b & (1 << (7-j))) >> (7-j);
    if (type==0) {
      ack_buffer[j] = 0;
    }
    else {
      ack_buffer[j] = 1;
    }
  }

START_INIT:

  if(CAN_OK == CAN.begin(CAN_125KBPS))
  {
    Serial.println("CAN BUS Shield init ok!");
  }
  else
  {
    Serial.println("CAN BUS Shield init fail");
    Serial.println("Init CAN BUS Shield again");
    delay(100);
    goto START_INIT;
  }

}

void loop() {
  init_ainet_processor();
  dispatcher();
  readOrder();
  readCan();
}
