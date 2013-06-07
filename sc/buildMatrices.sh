#!/bin/bash

rm *.ready *.raw *.dat *.dev

columns=5

if [ $# -eq 1 ]
then
  columns=$1
fi
 
echo "$columns columns"

for ((i=1;i<=5;++i))
do
echo "processing ..." $i
#./numberrows.py val$i.log val$i.log.num

sed "s/XYZ/$i/g" /home/barzan/scripts/temp.sql | sed "s-UVW-$PWD-g" | mysql -u root -h 127.0.0.1 -P 3400

/home/barzan/scripts/deverticalize.py coefs$i.raw coefs$i.dev $columns

#align
#sed "s/XYZ/$i/g" /home/barzan/scripts/align.m | matlab -nodisplay 
echo "addpath('/home/barzan/scripts/'); align($columns, 'coefs$i.dev','val$i.cut','coefs$i.ready','val$i.ready');" | matlab -nodisplay 

#sed "s/XYZ/$i/g" /home/barzan/scripts/msample.m | matlab -nodisplay 
echo "addpath('/home/barzan/scripts/'); msample($columns, 'coefs$i.ready','val$i.ready');" | matlab -nodisplay

sed "s/XYZ/$i/g" /home/barzan/scripts/disk-msample.m | matlab -nodisplay 

echo 

cat coefs$i.ready >> coefs.ready
cat val$i.ready >> val.ready

done

#sed "s/XYZ/$i/g" /home/barzan/scripts/msample.m | matlab -nodisplay
echo "addpath('/home/barzan/scripts/'); msampleSimple($columns, 'coefs.ready','val.ready');" | matlab -nodisplay





