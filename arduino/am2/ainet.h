const int AINET_COMMANDS_NUM = 20;


uint8_t ainet_commands[AINET_COMMANDS_NUM][11]={
{0x40,0x02,0x50,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x90}, // 0 H701 detection
{0x40,0x02,0x90,0x67,0x40,0x22,0x00,0x00,0x00,0x00,0xda}, //init, 1st command // not necessary ?
{0x40,0x02,0xe2,0x64,0x00,0x00,0x00,0x00,0x00,0x00,0x2e}, //init, 2nd command //enable presets
{0x40,0x02,0x90,0x67,0x25,0x00,0x00,0x00,0x00,0x00,0xf4}, //init, 3rd command
{0x40,0x02,0x90,0x67,0x5b,0x00,0x00,0x00,0x00,0x00,0x1b}, //init, 4th command
{0x40,0x50,0x90,0x67,0x41,0x23,0x00,0x00,0x00,0x00,0x5a}, //init, 5th command
{0x40,0x02,0xa0,0x70,0x00,0x00,0x00,0x00,0x00,0x00,0x1e}, // 6 init, 6th command -- enable the processor
{0x40,0x02,0xD2,0x99,0x00,0x00,0x00,0x00,0x00,0x00,0xD7}, // 7 volume 0
{0x40,0x02,0xd3,0x31,0x00,0x00,0x00,0x00,0x00,0x00,0x94}, //Balance L15
{0x40,0x02,0xd4,0x4f,0x00,0x00,0x00,0x00,0x00,0x00,0x1d}, //Fader F15
{0x40,0x50,0x04,0xD2,0x64,0x01,0x00,0x00,0x00,0x00,0x23}, //preset 1
{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00}, //user command to send
{0x40,0x02,0xd0,0x24,0x00,0x00,0x00,0x00,0x00,0x00,0xf8}, // 12 activate A1 input| (4th byte) 2a  - D1
{0x40,0x50,0x02,0xd1,0x73,0x00,0x00,0x00,0x00,0x00,0xaa}, //Sub. level +1
{0x40,0x50,0x02,0xd1,0x63,0x00,0x00,0x00,0x00,0x00,0xbe}, //Sub. level -1
{0x32,0x02,0x10,0x90,0x67,0x20,0x05,0x00,0x00,0x00,0x4a}, //Confirmation for CD Ch init
{0x32,0x02,0xa1,0x70,0x00,0x00,0x00,0x00,0x00,0x00,0xaa}, //CD Ch init #2
{0x32,0x50,0xd3,0x70,0x00,0x00,0x00,0x00,0x00,0x00,0x50}, //CD Ch play 
{0x32,0x50,0xd5,0x75,0x00,0x00,0x00,0x00,0x00,0x00,0x00}, //CD Ch disk/track control
{0x40,0x02,0xd6,0x70,0x00,0x00,0x00,0x00,0x00,0x00,0x29},// 19  unmute  
};

const byte VOL_LEN=36;
uint8_t vol[VOL_LEN]={0x99,0x78,0x68,0x60,0x55,0x50,0x48,0x46,0x44,0x42,
                      0x40,0x38,0x36,0x34,0x32,0x30,0x28,0x26,0x24,0x22,
                      0x20,0x18,0x16,0x14,0x12,0x10,0x09,0x08,0x07,0x06,
                      0x05,0x04,0x03,0x02,0x01,0x00};

