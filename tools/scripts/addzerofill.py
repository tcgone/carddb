#!/usr/bin/python3

# adds zerofill to scan files, ie 1.jpg -> 001.jpg.
# very rudimentary. may ruin your files, use at your own risk.
# use it inside the directory. 
# author: axpendix@hotmail.com

import os, re
rep = re.compile('\\D')

def walk_file(f):
  if '.png' in f or '.jpg' in f:
    fp=f.split('/')[-1].split('.')[0]
    fp=rep.sub("", fp)
    fe=f.split('/')[-1].split('.')[1]
    try:
      f2=f[:f.rfind('/')+1]+'{0:03d}'.format(int(fp))+'.'+fe
      if f2 != f:
        os.system("mv '{}' '{}'".format(f,f2))
    except ValueError:
      print(fp)
      pass
  pass

def walk(d):
  #print('walk({})'.format(d))
  if os.path.isdir(d):
    [walk(d+os.sep+f) for f in os.listdir(d)]
  else:
    walk_file(d)

walk('.')
