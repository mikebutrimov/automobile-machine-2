#define GPIO2_PREFER_SPEED 1
#include "arduino2.h"
#include <mcp_can.h>  
#include <pb_encode.h>
#include <pb_decode.h>
#include "msg.pb.h"
int counts = 0;
byte bits = 0;
const char BYTES = 8;
const int bytes = 11;
const byte FFBYTES[8] = {255,255,255,255,255,255,255,255};
long timer = 0;
byte sop[35];

long last = 0;

byte byte_val = 0;
byte vals[bytes*8];
byte byte_vals[bytes];
bool fast_byte_buffer[bytes*8];
bool ack_buffer[8];
int readyForNext = 1;
int messageLen = 0;
uint8_t i,j,type;
const byte PLEN = 33;
const byte SOPLEN = 35;
const int AINETIN = 2;
const int AINETOUT = 13;
const int BITVAL_THRESHOLD_HIGH = 6;
const int SOF_THRESHOLD = 15;
const int BRAKE = 10000;
MCP_CAN CAN(10); 
const int HEARTBEAT_SIZE = 9;

const byte VOL_LEN=36;
uint8_t vol[VOL_LEN]={0x99,0x78,0x68,0x60,0x55,0x50,0x48,0x46,0x44,0x42,
                      0x40,0x38,0x36,0x34,0x32,0x30,0x28,0x26,0x24,0x22,
                      0x20,0x18,0x16,0x14,0x12,0x10,0x09,0x08,0x07,0x06,
                      0x05,0x04,0x03,0x02,0x01,0x00};


struct CAN_COMMAND {
  short address;
  short bytes;
  int putInTime;
  int delayTime;
  short payload[8];
};

CAN_COMMAND heartbeat[HEARTBEAT_SIZE] = {
  {1312,8,0,1000,{1,0,0,0,0,0,0,0}}, 
  {305,6,0,100,{1,16,0,0,0,32}},
  {485,7,0,500,{63,63,63,63,72,0,3}},
  {741,4,0,1000,{0,0,0,0}},
  {997,6,0,1000,{0,0,0,0,0,0}},
  {357,4,0,100,{200,192,32,0}},
  {353,7,0,100,{160,3,6,1,0,1,0}},
  {805,3,0,500,{0,11,0}},
  {869,5,0,500,{20,50,43,0,0}},
};



void crc(uint8_t *packet) {
  uint8_t crc_reg=0xff,poly,i,j;
  uint8_t bit_point, *byte_point;
  for (i=0, byte_point=packet; i<10; ++i, ++byte_point) {
    for (j=0, bit_point=0x80 ; j<8; ++j, bit_point>>=1) {
      if (bit_point & *byte_point) { // case for new bit =1
        if (crc_reg & 0x80) poly=1; // define the polynomial
        else poly=0x1c;
        crc_reg= ( (crc_reg << 1) | 1) ^ poly;
      } else { // case for new bit =0
        poly=0;
        if (crc_reg & 0x80) poly=0x1d;
        crc_reg= (crc_reg << 1) ^ poly;
      }
    }
  }
  packet[10]= ~crc_reg; // write use CRC
}

void fastSend(bool* packet, int packet_size, bool ack){
  noInterrupts();
  if (!ack){
    digitalWrite2(AINETOUT, HIGH);
    delayMicroseconds(32);
    digitalWrite2(AINETOUT, LOW);
    delayMicroseconds(16);
  }
  for (int i = 0; i< packet_size; i++){
    if (packet[i] == 0){
      digitalWrite2(AINETOUT, HIGH);
      delayMicroseconds(14);
      digitalWrite2(AINETOUT, LOW);
      delayMicroseconds(8);
    }
    else {
      digitalWrite2(AINETOUT, HIGH);
      delayMicroseconds(8);
      digitalWrite2(AINETOUT, LOW);
      delayMicroseconds(14);
    }
  }
  interrupts();
}

void fastByteSend(byte * packet, int packet_size){
  noInterrupts();
  for (i=0;i<packet_size;i++) {
    for (j=0;j<8;j++) {
      type=(packet[i] & (1 << (7-j))) >> (7-j);
      if (type==0) {
        fast_byte_buffer[i*8+j] = 0;
      }
      else {
        fast_byte_buffer[i*8+j] = 1;
      }
    }
  }
  fastSend(fast_byte_buffer, packet_size*8,0);
  interrupts();
}

void volUp(){
  byte packet[11] = {0x40,0x02,0xD2,0x99,0x00,0x00,0x00,0x00,0x00,0x00,0xD7};
  fastByteSend(packet,11);
}


void isr_read_msg(){
  if (!readyForNext) return;
  noInterrupts();
  //here we start with RISING on SOF but must check it
  while (digitalRead2(AINETIN) == HIGH  && counts < BRAKE){
    counts++;
  }
  if (counts<SOF_THRESHOLD){//need to do some finetuning
    return;
  }
  //SOF ends with LOW reading. wait for HIGH again and count it
  for (int i = 0; i< bytes*8; i++){
    while (digitalRead2(AINETIN) == LOW){
    }
    counts = 0;
    while (digitalRead2(AINETIN) == HIGH && counts < BRAKE ){
      counts++;
    }
    if (counts < BITVAL_THRESHOLD_HIGH){
      vals[i] = 1;
    }
    else {
      vals[i] = 0;
    }
    byte_val = (byte_val<<1)|vals[i];
    if (i % 8 ==7){
      byte_vals[i/8] = byte_val;
      byte_val = 0;
    }
  }
  readyForNext = 0;
  interrupts();
  if (byte_vals[0] == 0x02){
    delayMicroseconds(16); 
    fastSend(ack_buffer,8,1);
  }
}

void readOrder(){
  if (!Serial1.available()) return;
  int zero_count = 0;
  int read_count = 0;
  bool continue_read = true;
  while (continue_read){
    char byte_readed = Serial1.read();
    if (byte_readed != -1){
      read_count++;
      if (byte_readed == 0){
        zero_count++;
        }
      if (zero_count > PLEN){
        if (byte_readed != 0){
          messageLen = byte_readed;
          continue_read = false;
        }
      }
      if (read_count - zero_count > 0){
        read_count = 0;
        zero_count = 0;
      }
    }
  else {
  }
}
  Serial.print("Message SIZE: ");
  Serial.println(messageLen, DEC);
  while (Serial1.available() < messageLen){
  }
  Serial.println("__________________");
  byte * proto_buf_message = new byte[messageLen];
  byte received = Serial1.readBytes(proto_buf_message, messageLen);
  if (received == messageLen){
    //go ahead
    controlMessage message = controlMessage_init_zero;
    pb_istream_t stream = 
      pb_istream_from_buffer(proto_buf_message, messageLen);
    bool status;
    status = pb_decode(&stream, controlMessage_fields, &message);
    if (!status){
      Serial.println("Error decoding message");
    }
    //Serial.println("Decoded message:");
    //Serial.print("Addr : ");
    //Serial.println(message.can_address, HEX);
    //Serial.print("Payload count: ");
    //Serial.println(message.can_payload_count, DEC);
    //for (int i = 0; i< message.can_payload_count; i++){
    //  Serial.print("Payload ");
    //  Serial.print(i, DEC);
    //  Serial.print(" : ");
    //  for (int j = 0; j<message.can_payload[i].size ; j++){
    //    Serial.print (message.can_payload[i].bytes[j], DEC);
    //    Serial.print (" ");
    //  }
    //  Serial.println();
    //  last = millis();
    //}

    //retransmitt message to can
    int canId = message.can_address;
    if (canId == 0x165 || canId == 0x3e5 || canId == 0x21f){
      //security if to avoid writing garbage in can bus
      for (int i = 0; i< message.can_payload_count; i++){
        CAN.sendMsgBuf(canId,0,message.can_payload[i].size,message.can_payload[i].bytes);
        Serial.println("Message to CAN was send");
      }
    }
  }
}

void readCan(){
  //read data from CAN Bus
  unsigned char len = 0;
  unsigned char can_buf[8]; 
  int canId;
  if(CAN_MSGAVAIL == CAN.checkReceive()){
    CAN.readMsgBuf(&len, can_buf);
    canId = (int) CAN.getCanId();
    if (canId == 0x21f && len != 0){
      uint8_t buffer[64];
      size_t message_length;
      bool status;
      pb_ostream_t stream = pb_ostream_from_buffer(buffer, sizeof(buffer));
      controlMessage message = controlMessage_init_zero;
      message.can_address = canId;
      message.can_payload[0].size = len;
      for (int i = 0; i < len; i++){
        message.can_payload[0].bytes[i] = can_buf[i];
      }
      message.can_payload_count = 1;
      status = pb_encode(&stream, controlMessage_fields, &message);
      sop[SOPLEN-1] = stream.bytes_written;

      Serial1.write(sop,SOPLEN);
      Serial1.write(buffer,stream.bytes_written);
      //Serial.print("Can address  ");
      //Serial.print(canId, DEC);
      //Serial.print("  Bytes written:  ");
      //Serial.println(stream.bytes_written, DEC);
    }
  }
}

void testReadCan(){
  //read data from CAN Bus
  unsigned char len = 3;
  unsigned char can_buf[3]= {64,0,0}; 
  int canId = 0x21f;
  if(true){
    if (canId == 0x21f && len != 0){
      uint8_t buffer[64];
      size_t message_length;
      bool status;
      pb_ostream_t stream = pb_ostream_from_buffer(buffer, sizeof(buffer));
      controlMessage message = controlMessage_init_zero;
      message.can_address = canId;
      message.can_payload[0].size = len;
      for (int i = 0; i < len; i++){
        message.can_payload[0].bytes[i] = can_buf[i];
      }
      message.can_payload_count = 1;
      status = pb_encode(&stream, controlMessage_fields, &message);
      sop[SOPLEN-1] = stream.bytes_written;

      Serial1.write(sop,SOPLEN);
      Serial1.write(buffer,stream.bytes_written);
      Serial.println("Test packet was send");
    }
  }
}

//some can heartbeat tools
void sendCmd(CAN_COMMAND cmd){
  int b_count = cmd.bytes;
  byte * buffer = new byte[b_count];
  //copy useful bytes from command to buffer to send it in CAN
  for (int i = 0; i< b_count; i++){
    buffer[i] = cmd.payload[i];
  }
  CAN.sendMsgBuf(cmd.address, 0, b_count,buffer);
  //Serial.print(millis());
  //Serial.print("\t");
  //Serial.println("cmd was sent");
  delete[] buffer;
}

void dispatcher(){
  for (int i = 0; i< HEARTBEAT_SIZE; i++){
    if ( ((int)millis() - heartbeat[i].delayTime - heartbeat[i].putInTime) >= 0){
      sendCmd(heartbeat[i]);
      heartbeat[i].putInTime = millis(); 
    }
  }
}

void batch_send(CAN_COMMAND * cmds, int len){
  for (int i = 0; i< len; i++){
    sendCmd(cmds[i]);
  }
}




 
void setup() {
  pinMode2(AINETIN, INPUT);
  pinMode2(AINETOUT, OUTPUT);
  //prepare uranus;
  //attachInterrupt(digitalPinToInterrupt(AINETIN), isr_read_msg, RISING);
  Serial.begin(115200);
  Serial1.begin(115200);
  //generate sop
  for (int i = 0; i < SOPLEN-1; i++){
    sop[i] = 0;
  }
  sop[SOPLEN-1] = PLEN;
  
  byte b = 0x02;
  for (j=0;j<8;j++) {
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
  // put your main code here, to run repeatedly:
 /* if (readyForNext == 0) {
    for (int i = 0; i< bytes; i++){
      Serial.print(byte_vals[i],HEX);
      Serial.print(" ");
    }
    Serial.println();
    readyForNext = 1;
  }*/
  readCan();
  readOrder(); 
  dispatcher();
}
