#include "arduino2.h"
const int AINETIN = 2;
const int AINETOUT = 13;
uint8_t i,j,type;
const int bytes = 11;
bool fast_byte_buffer[88];


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
      delayMicroseconds(5);
      digitalWrite2(AINETOUT, LOW);
      delayMicroseconds(18);
    }
  }
  interrupts();
}

void sendAiNetCommand(byte * packet, int packet_size){
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
  fastSend(fast_byte_buffer, packet_size*8, 0);
  interrupts();
}


