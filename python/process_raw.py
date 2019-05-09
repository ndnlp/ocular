'''
from __future__ import division
from bs4 import BeautifulSoup
import math
import nltk
from nltk.util import ngrams
from os import walk
from collections import Counter
import re
'''
def parse_transcript(transcript_filename):
    print(transcript_filename)
    #f = transcript_filename.split('/')
    #t = f[:2] + ['raw_clean'] + f[3:]
    #of = open('/'.join(t), 'w')
    of = open('vol2_clean.txt', 'w')
    with open(transcript_filename) as transcript:
        '''
        soup = BeautifulSoup(transcript, 'html.parser')
        raw = ' '.join([str(text) for text in soup.stripped_strings])
        for sent in nltk.sent_tokenize(raw):            
        '''
        lines = transcript.readlines()
        for sent in lines:
            sent = sent.strip()
            sent = "".join(i for i in sent if ord(i)>31 and ord(i)<127)
            sent = sent.split()
            sent = " ".join(i for i in sent if '/' not in i)
            '''
            sent = re.sub("[\(\[].*?[\)\]]", "", sent)            
            sent = sent.strip()
            
            if len(sent) == 0 or len(sent) < 3 and sent[0].isdigit():
                print(sent)
                continue
            '''
            of.write(sent + '\n')            
    of.close()

parse_transcript('vol2.txt')
'''
f_list = []

path ="../raw_org/raw"
for (dirpath, dirnames, filenames) in walk(path):
    f_list.extend([dirpath + '/' + fi for fi in filenames])

for f in f_list:
    parse_transcript(f)
'''

