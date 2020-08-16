#!/usr/bin/python
import sys

#check sys.argv length. we need at least one argument
if len(sys.argv) < 2:
    print ("Usage: %s filename"%(sys.argv[0]))
    print sys.argv
    exit() 
    

#lists with pids to include in heartbeat
pids = ['165','1E5','2E5','325','365','3A5','9F','A4']





input_file = sys.argv[1]
if (len(sys.argv) == 3):
    output_file = sys.argv[2] + ".cpp"
    output_header = sys.argv[2] + ".h"
else:
    output_file = "emulated.cpp"
    output_header = "emulated.h"


with open (output_header, 'w') as outheader:
    outheader.write('\n\
#ifndef EMULATED_H\n\
#define EMULATED_H\n\
void emulated();\n\
#endif\n')
	



with open (input_file, 'r') as infile:
    raw_file_list = infile.readlines()
    with open (output_file, 'w') as outfile:
        outfile.write('#include "'+output_header+'"\n')
        outfile.write('#include "canutils.h"\n\
CAN_COMMAND cmd;\n\
void emulated() {\n')

        last_read_line = None
        for line in raw_file_list:
            buf = line.strip().split("\t")
            if pids and buf[1] in pids:
                if last_read_line is not None:
                    delay = int(buf[0]) - int(last_read_line[0])
                else:
                    delay = 0    
                if delay != 0:
                    outfile.write('  delay ('+str(delay)+');\n')
                outfile.write('  cmd = {0x'+
                                buf[1] +
                                ','+
                                str(len(buf)-2)+
                                ',0,0,{'+
                                ','.join(buf[2:])+
                                '}};\n')
                outfile.write('  sendCmd(cmd);\n')
                last_read_line = buf

        outfile.write('}\n')


