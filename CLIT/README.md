C.L.I.T

Can LIstener / Transmitter

Special tool, used to generate arduino-compitable code.
As input it takes raw dump of CAN bus, in format: 
```
99          131     1       16      0       0       0       32
|           |       |       |       |       |       |       |
timestamp   PID     P       A       Y       L       O       A       D (up to 8 bytes)
(ms)
```

It converts such input to code like this:
```c++
void emulated() {
  cmd = {0x131,6,0,0,{1,16,0,0,0,32}};
  sendCmd(cmd);
  delay (40);
  cmd = {0x165,4,0,0,{8,198,0,32}};
  sendCmd(cmd);
  delay (59);
```

where cmd is CAN_COMMAND struct:

```c++
struct CAN_COMMAND {
  short address;
  short bytes;
  int putInTime;
  int delayTime;
  short payload[8];
};
```


and sendCmd is a function:
```c++

void sendCmd(CAN_COMMAND cmd){
  int b_count = cmd.bytes;
  byte * buffer = new byte[b_count];
  //copy useful bytes from command to buffer to send it in CAN
  for (int i = 0; i< b_count; i++){
    buffer[i] = cmd.payload[i];
  }
  CAN.sendMsgBuf(cmd.address, 0, b_count,buffer);
  delete[] buffer;
}

```

