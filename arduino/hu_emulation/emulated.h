#ifndef EMULATED_H
#define EMULATED_H


struct CAN_COMMAND {
  short address;
  short bytes;
  int putInTime;
  int delayTime;
  short payload[8];
};

void emulated();

#endif
