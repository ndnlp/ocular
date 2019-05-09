of = open('test.fst', 'w')
d = {}
p = {}
with open('test.arpa', 'r') as f:
    for line in f.readlines():
        line = line.strip()
        if len(line) < 1 or not (line[0].isdigit() or line[0] == '-'):
            continue
        lst = line.split('\t')
        d[lst[1]] = lst[0]
of.close()

of.write('(<s> '
for key in d:
    k_lst = key.split()
    if len(k_lst) > 1:
        u = ' '.join(k_lst[:-1])
        w = k_lst[-1]
        i = 1
        while ' '.join(k_lst[1:]) not in d:
            i += 1
        v = ' '.join(k_lst[1:])
        p = d[key]
        of.write('(' + u + ' (' + v + ' "' + w + '" ' + p + '))\n')
        
     

    
