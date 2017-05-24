#ifndef AINET_H
#define AINET_H
#include "arduino2.h"

//declared in ainet.cpp
extern const int AINETIN;
extern const int AINETOUT;
extern bool fast_byte_buffer[];
extern uint8_t i,j,type;
extern const int AINET_COMMANDS_NUM;
extern const byte VOL_LEN;
extern uint8_t ainet_commands[][11];
extern uint8_t vol[];
extern bool ainetInit;
extern bool ainetAck;
//^^all this was declared in ainet.cpp

void crc(uint8_t *packet);
void fastSend(bool* packet, int packet_size, bool ack);
void sendAiNetCommand(byte * packet, int packet_size);
void init_ainet_processor();

#endif
