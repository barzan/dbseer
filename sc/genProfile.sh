#!/bin/bash

win=2500
episode=5
tps=2000

#header
echo "580 10000 50 50 0 0 0 0 0 0 0 0 0" > t12-dist
echo "580 10000 50 0 50 0 0 0 0 0 0 0 0" > t13-dist
echo "580 10000 50 0 0 50 0 0 0 0 0 0 0" > t14-dist
echo "580 10000 50 0 0 0 50 0 0 0 0 0 0" > t15-dist
echo "580 10000 0 50 50 0 0 0 0 0 0 0 0" > t23-dist
echo "580 10000 0 50 0 50 0 0 0 0 0 0 0" > t24-dist
echo "580 10000 0 50 0 0 50 0 0 0 0 0 0" > t25-dist
echo "580 10000 0 0 50 50 0 0 0 0 0 0 0" > t34-dist
echo "580 10000 0 0 50 0 50 0 0 0 0 0 0" > t35-dist
echo "580 10000 0 0 0 50 50 0 0 0 0 0 0" > t45-dist

for i in `ls t*-dist`
do
echo "18 1 20 20 20 20 20 0 0 0 0 0 0" >> $i
done

share1=0
for ((i=0;i<=100;i=i+1))
do 
#share2=0$(echo "scale=9; 100-$share1" | bc)
share2=0$(echo "scale=9; 100-$i" | bc)
echo "$episode $tps $i $share2 0 0 0 0 0 0 0 0 0" >> t12-dist
echo "$episode $tps $i 0 $share2 0 0 0 0 0 0 0 0" >> t13-dist
echo "$episode $tps $i 0 0 $share2 0 0 0 0 0 0 0" >> t14-dist
echo "$episode $tps $i 0 0 0 $share2 0 0 0 0 0 0" >> t15-dist
echo "$episode $tps 0 $i $share2 0 0 0 0 0 0 0 0" >> t23-dist
echo "$episode $tps 0 $i 0 $share2 0 0 0 0 0 0 0" >> t24-dist
echo "$episode $tps 0 $i 0 0 $share2 0 0 0 0 0 0" >> t25-dist
echo "$episode $tps 0 0 $i $share2 0 0 0 0 0 0 0" >> t34-dist
echo "$episode $tps 0 0 $i 0 $share2 0 0 0 0 0 0" >> t35-dist
echo "$episode $tps 0 0 0 $i $share2 0 0 0 0 0 0" >> t45-dist
#share1=0$(echo "scale=9; $share1 + 100.00000000000/$win" | bc)
done

#18 1 20 20 20 20 20 0 0 0 0 0 0
#18 1 20 20 20 20 20 0 0 0 0 0 0
