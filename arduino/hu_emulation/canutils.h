#ifndef CANUTILS_H
#define CANUTILS_H
#include <Arduino.h>

struct CAN_COMMAND {
  short address;
  short bytes;
  int putInTime;
  int delayTime;
  short payload[8];
};

void sendCmd(CAN_COMMAND cmd);
#endif
