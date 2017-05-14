#include "emulated.h"
#include "canutils.h"
CAN_COMMAND cmd;
void emulated() {
  cmd = {0x325,3,0,0,{0,11,0}};
  sendCmd(cmd);
  delay (229);
  cmd = {0x365,5,0,0,{10,255,255,1,0}};
  sendCmd(cmd);
  delay (57);
  cmd = {0xA4,8,0,0,{16,44,32,0,136,7,58,58}};
  sendCmd(cmd);
  delay (2);
  cmd = {0x9F,3,0,0,{48,0,10}};
  sendCmd(cmd);
  delay (10);
  cmd = {0xA4,8,0,0,{33,46,77,80,51,84,83,82}};
  sendCmd(cmd);
  delay (2);
  cmd = {0x3A5,6,0,0,{7,255,255,0,128,128}};
  sendCmd(cmd);
  delay (9);
  cmd = {0xA4,8,0,0,{34,77,73,77,73,90,89,88}};
  sendCmd(cmd);
  delay (11);
  cmd = {0xA4,8,0,0,{35,0,0,0,0,0,0,0}};
  sendCmd(cmd);
  delay (1);
  cmd = {0x165,4,0,0,{200,198,32,32}};
  sendCmd(cmd);
  delay (9);
  cmd = {0xA4,8,0,0,{36,0,0,0,0,0,0,0}};
  sendCmd(cmd);
  delay (12);
  cmd = {0xA4,8,0,0,{37,0,0,0,0,0,0,0}};
  sendCmd(cmd);
  delay (11);
  cmd = {0xA4,4,0,0,{38,0,0,0}};
  sendCmd(cmd);
}
