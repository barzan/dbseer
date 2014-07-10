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
MATLAB_DIR=/home/alekh/MATLAB/R2012a/bin

#MYSQL_DATA=~/mysql/data
#MYSQL_DATA_ORIG=~/mysql/tpcc_mysql-32orig
#MYSQL_BASE=~/mysql
#MYSQL_OPTIONS="--innodb-buffer-pool-size=8G
#       --innodb_log_file_size=1800M
#       --innodb_flush_method=O_DIRECT
#       --port=3400
#       --transaction_isolation=serializable
#       --max_connections=300
#       --skip-grant-tables
#       --query_cache_size=0" 
# this last line was just to disable the query caching of mysql to reduce the unncesserary logging

PG_DATA=~/postgres/data
PG_DATA_ORIG=~/postgres/tpcc_postgres-32orig
PG_BASE=~/postgres

#mkdir -p /tmp/ram
#sudo mount -t tmpfs -o size=4096M tmpfs /tmp/ram/

cd ~
echo "deleting the contents of $EXPRDIR"
rm -f $EXPRDIR/* || echo "some content of $EXPRDIR could not be deleted"
rm -rf $EXPRDIR/processed/ || echo "there is no $EXPRDIR/processed to be deleted"
mkdir -p $EXPRDIR/processed

#echo "killing the current mysql ..."
#killall mysqld || echo "no mysqld to kill"
#echo "waiting..."
#sleep 20
echo "killing the current postgres ..."
killall postgres_alekh || echo "no postgres to kill"
echo "waiting..."
sleep 10

echo "restoring the original TPCC warehouse..."
#rm -rf $MYSQL_DATA
#cp -ar $MYSQL_DATA_ORIG $MYSQL_DATA
#rm -f $MYSQL_DATA/ib_logfile*
rm -rf $PG_DATA
cp -ar $PG_DATA_ORIG $PG_DATA

#echo "starting MySQL server..."
#$MYSQL_BASE/bin/mysqld --no-defaults --basedir=$MYSQL_BASE --socket=$MYSQL_BASE/mysql.sock \
#       --datadir=$MYSQL_DATA --pid-file=$MYSQL_BASE/mysql.pid --tmpdir=$MYSQL_DATA $MYSQL_OPTIONS &
#sleep 150
echo "starting PosgreSQL server..."
$PG_BASE/bin/postgres_alekh -D $PG_DATA &
sleep 20
echo "database started"

#echo "inserting synthetic tables... [CHECK !!]"
#~/sc/importData.sh

echo "killing previous instances of dstat..."
killall python || echo "no python to kill!"
rm -f $MONITOR/log_exp_1.csv || echo "no initial dstat logs to delete"

echo "deleting the current logs on the client"
rsh $CLIENT "rm -rf ~/log/*" | echo "no existing logs on the client."

echo "warming up the DB..."
#rsh $CLIENT "cd $CLIENT_BENCH_DIR; mysql --user=root --host=$SERVERIP oltpbench < run/sql/warm.sql > /dev/null"
rsh $CLIENT "export LC_ALL=\"en_US.utf8\"; cd $CLIENT_BENCH_DIR; psql --username=root --host=$SERVERIP --port=5400 tpcc -f run/sql/warm.sql > /dev/null"

#for cfname in `ls $PROFILE`
scp $PROFILE $CLIENT:$CLIENT_BENCH_DIR/.

#cat $PROFILE | while read profile_line
#do 
	#echo $profile_line
	#continue
	cfname="pgtpcc"
	echo "configuring the client..."
	CLIENT_PROFILE=`echo $PROFILE | sed 's/.*\///'`
	rsh -n $CLIENT "cd ~/bench/; ./setconf2 ~/log $cfname $CLIENT_BENCH_DIR/$CLIENT_PROFILE"
	CMD="cd ~/bench/; ./oltpbenchmark -b tpcc -c ~/log/conf-$cfname.xml --execute=true -s 1 -o ~/log/trans-$cfname 2> ~/log/err-$cfname"

	echo "syncing the client with the server"
	rsh -n $CLIENT "/bin/sync"

	echo "starting dstat monitor"
	source $MONITOR/setenv
	DSTAT_HOMEDIR=$MONITOR
	$MONITOR/monitor.sh & 
	sleep 10
	
	echo "launching the benchmark on the client: $CMD"
	#~/sc/affinity.sh &
	rsh -n $CLIENT $CMD

	echo "cleaning up"
	TIMESTAMP=`date +"%Y%m%d-%H:%M:%S"`
	killall python || echo "no python to kill!"
	mv $MONITOR/log_exp_1.csv $EXPRDIR/monitor-$cfname-$TIMESTAMP.orig
	tail -n +6 $EXPRDIR/monitor-$cfname-$TIMESTAMP.orig >> $EXPRDIR/monitor-$cfname-$TIMESTAMP
	#tail -n +6 $MONITOR/log_exp_1.csv | cut -d',' -f1,3- >  $EXPRDIR/monitor-$cfname
	rsh -n $CLIENT "killall -9 java" || echo "no java on the client to kill"

	echo "copying the output files from the client..."
	scp $CLIENT:~/log/trans-$cfname.raw $EXPRDIR/trans-$cfname-$TIMESTAMP
	scp $CLIENT:~/log/err-$cfname $EXPRDIR/err-$cfname-$TIMESTAMP
	#scp $EXPRDIR/monitor-$cfname $CLIENT:~/log/monitor-$cfname

	echo "processing the client file + aligning them with the server file!"
	export LC_ALL="en_US.utf8"
	echo "addpath('$SERVER_BENCH_DIR/matlab'); [counts latencies monitor] = fast_deverticalize_align('$EXPRDIR/', '$EXPRDIR/processed/', 'trans-$cfname-$TIMESTAMP','monitor-$cfname-$TIMESTAMP', 1, 9);" | $MATLAB_DIR/matlab -nodisplay 

	#echo "export LC_ALL=\"en_US.utf8\"; echo \"addpath('$CLIENT_BENCH_DIR/matlab'); [counts latencies monitor] = fast_deverticalize_align('/home/alekh/log/', '/home/alekh/log/', 'trans-$cfname','monitor-$cfname', 1, 9);\" | $MATLAB_DIR/matlab -nodisplay"
	#rsh -n $CLIENT "mv /home/alekh/log/trans-$cfname.raw /home/alekh/log/trans-$cfname"
	#rsh -n $CLIENT "export LC_ALL=\"en_US.utf8\"; echo \"addpath('$CLIENT_BENCH_DIR/matlab'); [counts latencies monitor] = fast_deverticalize_align('/home/alekh/log/', '/home/alekh/log/', 'trans-$cfname','monitor-$cfname', 1, 9);\" | $MATLAB_DIR/matlab -nodisplay"
	mv $EXPRDIR/monitor-$cfname $EXPRDIR/processed/monitor-$cfname-$TIMESTAMP
	mv $EXPRDIR/monitor-$cfname.orig $EXPRDIR/processed/monitor-$cfname-$TIMESTAMP.orig
	mv $EXPRDIR/trans-$cfname-$TIMESTAMP $EXPRDIR/processed/trans-$cfname-$TIMESTAMP
	mv $EXPRDIR/err-$cfname-$TIMESTAMP $EXPRDIR/processed/err-$cfname-$TIMESTAMP
	echo "finished one experiment"
#done

echo "Experiments done." 	

# c=0; for i in `ps -C mysqld -m -o tid`; do echo $i $c; taskset -c -p $c $i; let c=c+1; let c=c%16; done
 
18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
