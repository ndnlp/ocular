#!/usr/bin/env python
# -*- coding: utf-8 -*-

from os import walk
import sys
reload(sys)
sys.setdefaultencoding('utf8')

f_list = ['vol2_clean.txt']
'''
path ="tesserae"
for (dirpath, dirnames, filenames) in walk(path):
    f_list.extend([dirpath + '/' + fi for fi in filenames])
'''
rum = 0
et = 0
pbr = 0
con = 0
lis = 0
pro = 0
us = 0
bus = 0
per = 0
que = 0

for fname in f_list:
    fout = 'vol2_proc.txt'
    #fout = 'tess_proc/' + fname.split('/')[1]
    g = open(fout, 'w')
    with open(fname, 'r') as f:
        for line in f.readlines():
            line.replace("j", "i")
            line.replace("v", "u")
            line.replace("J", "I")
            line.replace("U", "V")
            if 'rum ' in line:
                if rum % 2 == 0:
                    line = line.replace("rum ", u"\uA75D" + " ")
                rum += 1
            if ' et ' in line:
                if et % 2 == 0:
                    line = line.replace(" et ", " " + u"\u204A" + " ")
                et += 1
            if 'presbyter' in line:
                if pbr % 2 == 0:
                    line = line.replace("presbyter", "p" + u"\u0180" + "r")
                pbr += 1
            if 'con' in line:
                if con % 2 == 0:
                    line = line.replace("con", u"\uA76F")
                con += 1
            if 'lis' in line:
                if lis % 2 == 0:
                    line = line.replace("lis", u"\uA749")
                lis += 1
            if 'pro' in line:
                if pro % 2 == 0:
                    line = line.replace("pro", u"\uA753")
                pro += 1
            if 'us ' in line:
                if us % 2 == 0:
                    line = line.replace("us ", u"\uA770" + " ")
                us += 1
            if 'bus' in line:
                if bus % 2 == 0:
                    line = line.replace("bus", "b;")
                bus += 1
            if 'per' in line:
                if per % 2 == 0:
                    line = line.replace("per", u"\uA751")
                per += 1
            if 'que' in line:
                if que % 3 == 0:
                    line = line.replace("que", "q;")                    
                elif que % 3 == 1:
                    line = line.replace("que", u"\uA757")
                que += 1
            g.write(line)
    g.close()
    
