#!/bin/bash

set -o errexit
set -o nounset

if [ $# -ne 3 ]
then
  echo "Usage: `basename $0` mem-file profile-dir output-directory" 
  exit 1 
fi

MEM_FILE=$1
PROFILE_DIR=$2
OUT_DIR=$3

rm -f $OUT_DIR/out.*

for m in `cat $MEM_FILE`
do
	echo "killing the current barzan_mysqld ..."
	killall barzan_mysqld || echo "no barzan_mysqld to kill"
	echo "waiting..."
	sleep 100

	echo "starting barzan_mysqld with memory $m"	
	~/sc/startMySQL.sh $m
	sleep 160

	echo "creating directory $OUT_DIR/$m"	
	mkdir -p $OUT_DIR/$m
	rm -rf $OUT_DIR/$m
	echo "starting debug-runExperiment.sh with $m memory"
	~/sc/debug-runExperiment.sh $OUT_DIR/$m $PROFILE_DIR 2>&1 > $OUT_DIR/out.$m

done

echo "Memory experiment done!"


18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
