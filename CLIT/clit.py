#!/usr/bin/python
import sys

#check sys.argv length. we need at least one argument
if len(sys.argv) < 2:
    print ("Usage: %s filename"%(sys.argv[0]))
    print sys.argv
    exit() 
    


input_file = sys.argv[1]
if (len(sys.argv) == 3):
    output_file = sys.argv[2] + ".c"
    output_header = sys.argv[2] + ".h"
else:
    output_file = "emulated.c"
    output_header = "emulated.h"


with open (output_header, 'w') as outheader:
	outheader.write('struct CAN_COMMAND {\n\
        short address;\n\
        short bytes;\n\
        int putInTime;\n\
        int delayTime;\n\
        short payload[8];\n\
        };\n\
    void emulated();\n')




with open (input_file, 'r') as infile:
    raw_file_list = infile.readlines()
    with open (output_file, 'w') as outfile:
        outfile.write('#include "'+output_header+'"\n')
        outfile.write('void emulated() {\n')
        outfile.write('CAN_COMMAND cmd;\n')
        last_read_line = None
        for line in raw_file_list:
            buf = line.strip().split("\t")
            if last_read_line is not None:
                delay = int(buf[0]) - int(last_read_line[0])
            else:
                delay = 0    
            if delay != 0:
                outfile.write('delay ('+str(delay)+');\n')
            outfile.write('cmd = {0x'+
                                buf[1] +
                                ','+
                                str(len(buf)-2)+
                                ',0,0,{'+
                                ','.join(buf[2:])+
                                '}};\n')
            outfile.write('sendCmd(cmd);\n')
            last_read_line = buf

        outfile.write('}')


