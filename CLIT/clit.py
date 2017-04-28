#!/usr/bin/python
import sys

#check sys.argv length. we need at least one argument
if len(sys.argv) < 2:
    print ("Usage: %s filename"%(sys.argv[0]))
    print sys.argv
    exit() 
    


input_file = sys.argv[1]
output_file = input_file + ".out.c"

with open (input_file, 'r') as infile:
    raw_file_list = infile.readlines()
    with open (output_file, 'w') as outfile:
        outfile.write('void emulated() {\n')
        last_read_line = None
        for line in raw_file_list:
            buf = line.strip().split("\t")
            if last_read_line is not None:
                delay = int(buf[0]) - int(last_read_line[0])
            else:
                delay = 0    
            if delay != 0:
                outfile.write('delay ('+str(delay)+');\n')
            outfile.write('CAN_COMMAND cmd = {'+
                                buf[1] +
                                ','+
                                str(len(buf)-2)+
                                ',0,0,{'+
                                ','.join(buf[2:])+
                                '}};\n')
            outfile.write('sendCmd(cmd);\n')
            last_read_line = buf

        outfile.write('}')


