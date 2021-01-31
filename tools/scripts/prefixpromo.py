#!/usr/bin/python3
import os, sys

# prefixes scan files, ie 001.jpg -> XY1.jpg.
# very rudimentary. may ruin your files, use at your own risk.
# use it inside the directory. first argument is the prefix, eg 'XY'
# author: axpendix@hotmail.com

def walk_file(f, prefix):
  if '.png' in f or '.jpg' in f:
    fp=f.split('/')[-1].split('.')[0]
    fe=f.split('/')[-1].split('.')[1]
    try:
      f2=f[:f.rfind('/')+1]+prefix+str(int(fp))+'.'+fe
      if f2 != f:
        os.system("mv '{}' '{}'".format(f,f2))
    except ValueError:
      print(fp)
      pass
  pass

def walk(d, prefix):
  #print('walk({})'.format(d))
  if os.path.isdir(d):
    [walk(d+os.sep+f, prefix) for f in os.listdir(d)]
  else:
    walk_file(d, prefix)

walk('.',sys.argv[1])
