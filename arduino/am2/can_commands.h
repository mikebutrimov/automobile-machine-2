const int HEARTBEAT_SIZE = 8;

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
  //{805,3,0,500,{0,11,0}},
  {869,5,0,500,{254,255,255,0,0}},
};
