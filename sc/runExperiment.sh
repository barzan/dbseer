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
CLIENT=vise5
MONITOR=~/rs-sysmon2
CLIENT_BENCH_DIR=/home/alekh/bench
SERVER_BENCH_DIR=~/bench
MATLAB_DIR=/home/barzan/bin

MYSQL_DATA=~/mysql/data
MYSQL_DATA_ORIG=~/mysql/tpcc_mysql-32orig
MYSQL_BASE=~/mysql
MYSQL_OPTIONS="--innodb-buffer-pool-size=8G
       --innodb_log_file_size=1800M
       --innodb_flush_method=O_DIRECT
       --port=3400
       --transaction_isolation=serializable
       --max_connections=300
       --skip-grant-tables
       --query_cache_size=0" 
# this last line was just to disable the query caching of mysql to reduce the unncesserary logging

#PG_DATA=~/postgres/data
#PG_DATA_ORIG=~/postgres/tpcc_postgres-32orig
#PG_BASE=~/postgres

#mkdir -p /tmp/ram
#sudo mount -t tmpfs -o size=4096M tmpfs /tmp/ram/

cd ~
echo "deleting the contents of $EXPRDIR"
rm -f $EXPRDIR/* || echo "some content of $EXPRDIR could not be deleted"
rm -rf $EXPRDIR/processed/ || echo "there is no $EXPRDIR/processed to be deleted"
mkdir -p $EXPRDIR/processed

echo "killing the current mysql ..."
killall mysqld_alekh || echo "no mysqld to kill"
echo "waiting..."
sleep 20
#echo "killing the current postgres ..."
#killall postgres || echo "no postgres to kill"
#echo "waiting..."
#sleep 20

echo "restoring the original TPCC warehouse..."
rm -rf $MYSQL_DATA
cp -ar $MYSQL_DATA_ORIG $MYSQL_DATA
rm -f $MYSQL_DATA/ib_logfile*
#rm -rf $PG_DATA
#cp -ar $PG_DATA_ORIG $PG_DATA

echo "starting MySQL server..."
$MYSQL_BASE/bin/mysqld_alekh --no-defaults --basedir=$MYSQL_BASE --socket=$MYSQL_BASE/mysql.sock \
       --datadir=$MYSQL_DATA --pid-file=$MYSQL_BASE/mysql.pid --tmpdir=$MYSQL_DATA $MYSQL_OPTIONS &
sleep 150
#echo "starting PosgreSQL server..."
#$PG_BASE/bin/postgres -D $PG_DATA &
#sleep 50
echo "database started"

#echo "inserting synthetic tables... [CHECK !!]"
#~/sc/importData.sh

echo "killing previous instances of dstat..."
killall python || echo "no python to kill!"
rm -f $MONITOR/log_exp_1.csv || echo "no initial dstat logs to delete"

echo "deleting the current logs on the client"
rsh $CLIENT "rm -rf ~/log/*" | echo "no existing logs on the client."

echo "warming up the DB..."
rsh $CLIENT "cd $CLIENT_BENCH_DIR; mysql --user=root --host=$SERVERIP oltpbench < run/sql/warm.sql > /dev/null"
#rsh $CLIENT "cd $CLIENT_BENCH_DIR; psql --username=root --host=$SERVERIP --port=5400 tpcc -f run/sql/warm.sql > /dev/null"

#for cfname in `ls $PROFILE`
cat $PROFILE | while read profile_line
do 
	cfname=`echo $profile_line | sed -e 's/ /-/g' | sed -e 's/,/_/g'`
	echo "configuring the client..."
	rsh $CLIENT "cd ~/bench/; ./setconf ~/log $profile_line"
	CMD="cd ~/bench/; ./oltpbenchmark -b tpcc -c ~/log/conf.xml --execute=true -s 1 -o ~/log/trans-$cfname 2> ~/log/err-$cfname"

	echo "syncing the client with the server"
	rsh $CLIENT "/bin/sync"

	echo "starting dstat monitor"
	source $MONITOR/setenv
	DSTAT_HOMEDIR=$MONITOR
	$MONITOR/monitor.sh & 
	
	echo "launching the benchmark on the client: $CMD"
	#~/sc/affinity.sh &
	rsh $CLIENT $CMD

	echo "cleaning up"
	killall python || echo "no python to kill!"
	#mv $MONITOR/log_exp_1.csv $EXPRDIR/monitor-$cfname
	tail -n +6 $MONITOR/log_exp_1.csv | cut -d',' -f1,3- >  $EXPRDIR/monitor-$cfname
	rsh $CLIENT "killall -9 java" || echo "no java on the client to kill"

	echo "copying the output files from the client..."
	scp $CLIENT:~/log/trans-$cfname.raw $EXPRDIR/trans-$cfname
	scp $CLIENT:~/log/err-$cfname $EXPRDIR 

	echo "processing the client file + aligning them with the server file!"
	export LC_ALL="en_US.utf8"
	echo "addpath('$SERVER_BENCH_DIR/matlab'); [counts latencies monitor] = fast_deverticalize_align('$EXPRDIR/', '$EXPRDIR/processed/', 'trans-$cfname','monitor-$cfname', 1, 9);" | $MATLAB_DIR/matlab -nodisplay 
	mv $EXPRDIR/monitor-$cfname $EXPRDIR/processed/monitor-$cfname

done

echo "Experiments done." 	

# c=0; for i in `ps -C mysqld -m -o tid`; do echo $i $c; taskset -c -p $c $i; let c=c+1; let c=c%16; done
 
