#!/bin/bash

set -o errexit
set -o nounset


if [ $# -ne 4 ]
then
  echo "Usage: `basename $0` numOfTranTypes configFile rawDir proccessedDir"
  exit 1 
fi

numOfTranTypes=$1
configFile=$2
rawDir=$3
proccessedDir=$4 

rm -f $proccessedDir/*.ready $proccessedDir/*.raw $proccessedDir/*.dat $proccessedDir/*.dev or echo nothing to delete

echo "$numOfTranTypes columns"

for i in `sed 's/ /-/g' $configFile` 
do
echo "processing ..." $i

sed "s/XYZ/$i/g" ~/sc/temp-new.sql | sed "s-ABC-$rawDir-g" | sed "s-UVW-$proccessedDir-g"  
sed "s/XYZ/$i/g" ~/sc/temp-new.sql | sed "s-ABC-$rawDir-g" | sed "s-UVW-$proccessedDir-g" | mysql -h 127.0.0.1 -u sina 

~/sc/deverticalize.py $proccessedDir/coefs-$i.raw $proccessedDir/coefs-$i.dev $numOfTranTypes

#align
echo "addpath('~/sc/'); alignnew($numOfTranTypes, '$proccessedDir/coefs-$i.dev','$rawDir/monitor-$i.csv','$proccessedDir/coefs-$i.ready','$proccessedDir/monitor-$i.ready');" | matlab -nodisplay 

#echo "addpath('~/sc/'); msample($columns, 'coefs$i.ready','val$i.ready');" | matlab -nodisplay

#sed "s/XYZ/$i/g" ~/sc/disk-msample.m | matlab -nodisplay 

echo 

#cat coefs$i.ready >> coefs.ready
#cat val$i.ready >> val.ready

done

#echo "addpath('~/sc/'); msampleSimple($columns, 'coefs.ready','val.ready');" | matlab -nodisplay





18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
