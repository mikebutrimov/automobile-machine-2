// demo: CAN-BUS Shield, send data
#include <mcp_can.h>
#include <SPI.h>

// the cs pin of the version after v1.1 is default to D9
// v0.9b and v1.0 is default D10
const int SPI_CS_PIN = 10;
const int HEARTBEAT_SIZE = 6;
boolean startup = false;
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
};


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





void dispatcher(){
  for (int i = 0; i< HEARTBEAT_SIZE; i++){
    if ( ((int)millis() - heartbeat[i].delayTime - heartbeat[i].putInTime) >= 0){
      sendCmd(heartbeat[i]);
      heartbeat[i].putInTime = millis(); 
    }
  }
}

void setup()
{
    Serial.begin(115200);

    while (CAN_OK != CAN.begin(CAN_125KBPS))              // init can bus : baudrate = 500k
    {
        Serial.println("CAN BUS Shield init fail");
        Serial.println(" Init CAN BUS Shield again");
        delay(100);
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
      startup = true;
    }

  dispatcher();
  
}

/*********************************************************************************************************
  END FILE
*********************************************************************************************************/
