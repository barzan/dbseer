#!/usr/bin/python

import sys

def deverticalize(ifname, ofname, numTypes):
#  print ifname, ofname, numTypes
  inf = open(ifname, 'r')
  outf = open(ofname, 'w')
  counts = []
  for i in range(numTypes):
    counts.append(0)  

  lastWinId = None
  for line in inf:
    (winid, tranType, tranCnt) = line[:-1].split(',');
    winid = int(winid) 
    tranType = int(tranType) 
    tranCnt  = int(tranCnt) 
    #print winid, tranType, tranCnt, 'End'
    if winid != lastWinId:
      if winid < lastWinId:
        print 'invalid format! the winids are not sorted'
        sys.exit()
      if lastWinId != None:
        x = [] 
        for i in counts: x.append(str(i))
        outf.write(','.join(x) +'\n')
        x = []
        for i in range(numTypes): x.append(str(0))
        for i in range(lastWinId+1,winid): outf.write(','.join(x) +'\n')
      for i in range(numTypes):
        counts[i] = 0
    counts[tranType-1] = tranCnt
    lastWinId = winid

  if lastWinId != None:
    x = []
    for i in counts: 
      x.append(str(i))
    outf.write(','.join(x))
    outf.write('\n')
  
  inf.close()
  outf.close()


def main():
  if len(sys.argv) != 4:
    print 'Usage:', sys.argv[0], 'inputFile', 'outputFile', 'numOfTransTypes'
  else:
     deverticalize(sys.argv[1],sys.argv[2],int(sys.argv[3]))


#################
if __name__ == '__main__':
  main()
18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
