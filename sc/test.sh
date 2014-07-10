#!/bin/bash

set -o errexit
set -o nounset

if [ $# -ne 4 ]
then
  echo "Usage: `basename $0` exprOutDir configFile winTimeInSecs TPSlimit"
  exit 1 
fi

EXPRDIR=$1
CONF=$2
WINDOW=$3
TPSlimit=$4
SERVERIP=128.30.76.250
CLIENT=vise
MONITOR=/home/barzan/rs-sysmon/

cd ~

echo "killing previous instances of dstat..."
killall python || echo "no python to kill!"
rm -f $MONITOR/log_exp_1.csv || echo "no initial dstat logs to delete"

for cfname in `sed 's/ /-/g' $CONF`
do 

cf=`echo -n $cfname | sed 's/-/ /g'`
echo "*************************** read $cf"

CMD="cd ~/bench/; java -cp ./build/classes:./lib/commons-lang-2.5.jar:./lib/mysql-connector-java-5.1.10-bin.jar -Dnwarehouses=32 -Dnterminals=160 -Ddriver=com.mysql.jdbc.Driver -Dconn=jdbc:mysql://$SERVERIP:3400/tpcc -Duser=root client.TPCCOverTime $WINDOW $cf $TPSlimit > ~/coefs-$cfname 2> ~/err-$cfname"

. $MONITOR/setenv
$MONITOR/monitor.sh & 

rsh $CLIENT "touch ~/coefs-$cfname"
rsh $CLIENT "touch ~/err-$cfname"

echo "launching the benchmark on the client: $CMD"
sleep 3

echo "copying the output files from the client..."
scp $CLIENT:~/coefs-$cfname $EXPRDIR
scp $CLIENT:~/err-$cfname $EXPRDIR 

killall aaaabbbbccc || echo "no python to kill!"
mv $MONITOR/log_exp_1.csv $EXPRDIR/monitor-$cfname

done

echo "Out of the loop here ........................."

killall iotop || echo "no iotop to kill"
echo "Experiments done." 	

 
18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
