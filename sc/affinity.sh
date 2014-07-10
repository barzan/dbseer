#!/bin/bash
ps -C barzan_mysqld -m -o tid h | grep -v - | sort > /tmp/beforeExp
for i in `cat /tmp/beforeExp`; do taskset -c -p 0 $i; done
sleep 1800
# we use only one core for the entire mysql threads!
ps -C barzan_mysqld -m -o tid h | grep -v - | sort > /tmp/afterExp
comm /tmp/beforeExp /tmp/afterExp -1 -3 > /tmp/newOnes
#the following line disables hyperthreading
c=0; for i in `cat /tmp/newOnes`; do let c=c+1; taskset -c -p $c $i; let c=c%7; done
# to use hyperthreading you should use the following line!
#c=0; for i in `cat /tmp/newOnes`; do let c=c+1; taskset -c -p $c $i; let c=c%15; done
18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
