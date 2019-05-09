import string

i = 1
with open('vocab', 'w') as f:
    for s in string.ascii_letters + string.punctuation:
        f.write(str(i) + ' ' + s + ' 0\n') 
        i += 1
