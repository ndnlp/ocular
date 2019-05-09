import kenlm
import math

model = kenlm.LanguageModel('five.arpa')
fname = 'test.norm'
s = 0
n = 0
with open(fname, 'r') as f:
    for l in f.readlines():
        l = l.strip()
        s += model.score(l)
        n += len(l)
ce = - s/n
print(math.pow(10, ce))
