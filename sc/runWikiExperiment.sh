#!/bin/bash

set -o errexit
set -o nounset

if [ $# -ne 2 ]
then
  #echo "Usage: `basename $0` exprOutDir configFile winTimeInSecs TPSlimit"
  echo "Usage: `basename $0` exprOutDir profilesDir"
  exit 1 
fi

EXPRDIR=$1
PROFILE=$2
#WINDOW=$3
#TPSlimit=$4
SERVERIP=vise4
CLIENT=vise
MONITOR=~/diagnostictool/rs-sysmon/
MYSQL_DATA=~/tpcc_mysql
MYSQL_DATA_ORIG=~/tpcc_mysql-32orig
MYSQL_BASE=~/mysql-5.5.7-rc-linux2.6-x86_64
MYSQL_OPTIONS="--innodb-buffer-pool-size=8G
       --innodb_log_file_size=1800M
       --innodb_flush_method=O_DIRECT
       --port=3400
       --transaction_isolation=serializable
       --max_connections=300
       --skip-grant-tables
       --query_cache_size=0"
# this last line was just to disable the query caching of mysql to reduce the unncesserary logging
CLIENT_BENCH_DIR=/home/barzan/bench-carlo
SERVER_BENCH_DIR=~/bench-bm

cd ~
echo "deleting the contents of $EXPRDIR"
rm -f $EXPRDIR/* || echo "some content of $EXPRDIR could not be deleted"
rm -rf $EXPRDIR/processed/ || echo "there is no $EXPRDIR/processed to be deleted"
mkdir -p $EXPRDIR/processed

#echo "killing the current mysql ..."
#killall mysqld || echo "no mysqld to kill"
#echo "waiting..."
#sleep 20

#echo "restoring the original TPCC warehouse..."
#rm -rf $MYSQL_DATA
#cp -ar $MYSQL_DATA_ORIG $MYSQL_DATA
#rm -f $MYSQL_DATA/ib_logfile*

#echo "starting MySQL server..."
#$MYSQL_BASE/bin/mysqld --no-defaults --basedir=$MYSQL_BASE --socket=mysql.sock \
#       --datadir=$MYSQL_DATA --pid-file=mysql.pid --tmpdir=$MYSQL_DATA $MYSQL_OPTIONS &

#sleep 80

#echo "inserting synthetic tables..."
#~/sc/importData.sh

echo "killing previous instances of dstat..."
killall python || echo "no python to kill!"
rm -f $MONITOR/log_exp_1.csv || echo "no initial dstat logs to delete"

echo "deleting the current logs on the client"
rsh $CLIENT "rm -rf ~/log/*" | echo "no existing logs on the client."

#echo "warming up the DB..."
#rsh $CLIENT "cd $CLIENT_BENCH_DIR; mysql --user=root --host=$SERVERIP --port=3400 tpcc < run/sql/warm.sql > /dev/null"

#for cfname in `ls -r $PROFILE`
for cfname in `ls $PROFILE`
do 
	echo "time is now: "
	date
	scp $PROFILE/$cfname $CLIENT:~/log/rates-$cfname
#	CMD="cd $CLIENT_BENCH_DIR; java -XX:+HeapDumpOnOutOfMemoryError -Xmx8g -cp \`run/classpath.sh\` com.oltpbenchmark.DBWorkload -b wikipedia -c /home/barzan/log/rates-$cfname -o /home/barzan/log/trans-$cfname 2>&1 > ~/log/err-$cfname"
	CMD="cd $CLIENT_BENCH_DIR; ./oltpbenchmark -b wikipedia -c  /home/barzan/log/rates-$cfname --execute -o /home/barzan/log/trans-$cfname 2>&1 > ~/log/err-$cfname"

	echo "syncing the client with the server"
	rsh $CLIENT "/usr/bin/sync"

	echo "launching the benchmark on the client: $CMD"

	source $MONITOR/setenv
	DSTAT_HOMEDIR=$MONITOR
	$MONITOR/monitor.sh & 
	
	~/sc/affinity.sh &
	rsh $CLIENT $CMD

	killall python || echo "no python to kill!"
	mv $MONITOR/log_exp_1.csv $EXPRDIR/monitor-$cfname

	rsh $CLIENT "killall -9 java" || echo "no java on the client to kill"

	echo "copying the output files from the client..."
	scp $CLIENT:~/log/trans-$cfname $EXPRDIR
	scp $CLIENT:~/log/err-$cfname $EXPRDIR 

	echo "processing the client file + aligning them with the server file!"
	echo "addpath('$SERVER_BENCH_DIR/matlab'); [counts latencies monitor] = fast_deverticalize_align('$EXPRDIR/', '$EXPRDIR/processed/', 'trans-$cfname','monitor-$cfname', 1, 15);" | matlab -nodisplay 
	mv $EXPRDIR/monitor-$cfname $EXPRDIR/processed/monitor-$cfname
	
	lx=`wc -l /tmp/newOnes`
	echo "$lx threads pinpointed"
	
done

echo "Experiments done." 	

# c=0; for i in `ps -C mysqld -m -o tid`; do echo $i $c; taskset -c -p $c $i; let c=c+1; let c=c%16; done
 
18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
