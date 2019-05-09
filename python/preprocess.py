import re

#of = open('abbrev.csv', 'w')
n = 0
with open('abbrev.txt', 'r') as f:
    for line in f.readlines():
        l = line.strip()
        if '(' not in l:
            continue
        start = 0
        for i, c in enumerate(l):
            if c == '(':
                start = i
            if c == ')':
                leng = i-start-1
                if leng > n:
                    n = leng
print n
        #longs = l.replace('(', '').replace(')', '')
        #short = re.sub("[\(\[].*?[\)\]]", "", l)
        #of.write(longs + '+' + short + '\n')
#of.close()
