of = open('test.norm', 'w')
with open('001r_9.norm', 'r') as f:
    for line in f.readlines():
        l = line.strip()
        new_l = ' '.join(list(l.lower()))
        of.write(new_l + '\n')
of.close()
