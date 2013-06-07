#!/bin/bash

EXPECTED_ARGS=2
E_BADARGS=65

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` configDir dataDir"
  exit $E_BADARGS
fi

cnt=1
for i in `ls $1`
do
  ln -s $2/coefs-$i coefs$cnt.log
  ln -s $2/cpu-$i val$cnt.log
  cut -f 1,2,7,8 -d , val$cnt.log | tail -n +8 >  val$cnt.cut
  let "cnt += 1"
done


