// demo: CAN-BUS Shield, send data
#include <mcp_can.h>
#include <SPI.h>

// the cs pin of the version after v1.1 is default to D9
// v0.9b and v1.0 is default D10
const int SPI_CS_PIN = 10;
const int HEARTBEAT_SIZE = 8;
boolean startup = false;
boolean track = false;
MCP_CAN CAN(SPI_CS_PIN);                                    // Set CS pin

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
  {805,3,0,500,{0,11,0}},
  {869,5,0,500,{20,50,43,0,0}},
};



CAN_COMMAND track_name[8] = {

{164, 8, 0, 0,{16,  44,  32,  0, 88,  19,  32,  32}},  
{164, 8, 0, 0,{33,  80, 48, 119, 110, 100, 32, 32}},
{164, 8, 0, 0,{34,  66, 89, 32, 32, 85, 78, 72}}, 
{164, 8, 0, 0,{35,  65, 67, 75, 32, 77,  97,  110}},
{164, 8, 0, 0,{36,  117, 32,  67,  104, 97,  111, 32}},  
{164, 8, 0, 0,{37,  45,  32,  82,  117, 109, 98,  97}},  
{164, 4, 0, 0,{38,  32,  68,  101}},
{933, 6, 0, 1000, {1, 255, 255, 2, 45,  0}},
};

CAN_COMMAND track_name2[8] = {

{164, 8, 0, 0,{16,  44,  32,  0, 88,  19,  32,  32}},  
{164, 8, 0, 0,{33,  81, 49, 120, 111, 101, 32, 32}},
{164, 8, 0, 0,{34,  67, 90, 32, 32, 86, 79, 73}}, 
{164, 8, 0, 0,{35,  65, 67, 75, 32, 77,  97,  110}},
{164, 8, 0, 0,{36,  117, 32,  67,  105, 98,  112, 32}},  
{164, 8, 0, 0,{37,  46,  32,  83,  118, 110, 98,  97}},  
{164, 4, 0, 0,{38,  32,  68,  101}},
{933, 6, 0, 1000, {1, 255, 255, 2, 45,  0}},
};


unsigned char cd_time[6] = {1, 255, 255, 2, 0,  0};


void sendCmd(CAN_COMMAND cmd){
  int b_count = cmd.bytes;
  byte * buffer = new byte[b_count];
  //copy useful bytes from command to buffer to send it in CAN
  for (int i = 0; i< b_count; i++){
    buffer[i] = cmd.payload[i];
  }
  CAN.sendMsgBuf(cmd.address, 0, b_count,buffer);
  Serial.print(millis());
  Serial.print("\t");
  Serial.println("cmd was sent");
  delete[] buffer;
}

int sec = 0;



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

void setup()
{
    Serial.begin(115200);

    while (CAN_OK != CAN.begin(CAN_125KBPS))              // init can bus : baudrate = 500k
    {
        Serial.println("CAN BUS Shield init fail");
        Serial.println(" Init CAN BUS Shield again");
    }
    Serial.println("CAN BUS Shield init ok!");
}

unsigned char stmp[8] = {0, 1, 2, 3, 4, 5, 6, 7};
void loop()
{
    if (!startup){
      unsigned char start_seq[2] = {0,0};
      //start up sequence
      for (int i = 0; i< 10; i++){
        CAN.sendMsgBuf(1056, 0, 2, start_seq);
        delay(10);
      }
      delay(500);
      Serial.println("before end of start-up seq");
      unsigned char start_seq2[8] = {32,16,9,8,80,2,32,34};
      CAN.sendMsgBuf(1504, 0, 8, start_seq2);
      
      startup = true;
    }

  dispatcher();



  if (sec == 60){
      sec = 0;
  }
    
    if (millis()%1000 == 0){
      sec++;
      cd_time[4] = char(sec);
      //Serial.println(sec);
      //CAN.sendMsgBuf(933, 0, 6, cd_time);
    }


      if ( !track){
    batch_send(track_name2,7);
    //batch_send(track_name2,8);
    Serial.println("track1");
    track = true;
  }
  
  
   
  
}

/*********************************************************************************************************
  END FILE
*********************************************************************************************************/
