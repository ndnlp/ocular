from __future__ import division
from bs4 import BeautifulSoup
import math
import nltk
from nltk.util import ngrams
from os import walk
from collections import Counter
import re 


def parse_transcript(transcript_filename):
	with open(transcript_filename) as transcript:
		soup = BeautifulSoup(transcript, 'html.parser')
		raw = ' '.join([str(text) for text in soup.stripped_strings])
		for sent in nltk.sent_tokenize(raw):
			if len(sent) < 10:
				continue
			sent = re.sub("[\(\[].*?[\)\]]", "", sent)
			sent = sent.rstrip()
			print(' '.join([c for c in sent.lower()]))
'''
f_list = []

path ="raw_clean/christian_div/bible"
for (dirpath, dirnames, filenames) in walk(path):
	f_list.extend([dirpath + '/' + fi for fi in filenames])
'''
f_list = ["raw_clean/christian_div/voragine.raw"]
for f in f_list:
	parse_transcript(f)

