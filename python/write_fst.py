import string

with open('abbrev.fst', 'w') as f:
    f.write('A\n')
    for a in string.ascii_letters:
        f.write('(S (0 "' + a + '" "' + a + '"))\n')
        f.write('(0 (0 "' + a + '" "' + a + '"))\n')
        for i in range(14):
            f.write('(' + str(i) + ' (' + str(i+1) + ' "' + a + '" *e*))\n')
            f.write('(' + str(i+1) + ' (0 "' + a + '" "' + a + '"))\n')
            f.write('(' + str(i+1) + ' (A "</s>" "</s>"))\n')
        f.write('(0 (A "</s>" "</s"))\n')

        
