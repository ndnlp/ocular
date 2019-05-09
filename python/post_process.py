#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
reload(sys)
sys.setdefaultencoding('utf8')

fname = 'ocular_exe/0225_2_output/all_transcriptions/186r_iter-3_transcription.txt'
fout = open('0225_2.post', 'w')
with open(fname, 'r') as fin:
    for line in fin.readlines():
        line = line.replace(u"\uA75D", "rum")
        line = line.replace(u"\u204A", "et")
        line = line.replace(u"\u0180", "resbyte")
        line = line.replace(u"\uA76F", "con")
        line = line.replace(u"\uA749", "lis")
        line = line.replace(u"\uA753", "pro")
        line = line.replace(u"\uA770", "us")
        line = line.replace("b;", "bus")
        line = line.replace("q;", "que")
        line = line.replace(u"\uA751", "per")
        line = line.replace(u"\uA757", "que")
        fout.write(line)
fout.close()
