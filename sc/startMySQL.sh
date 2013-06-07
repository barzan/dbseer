#!/bin/bash

set -o errexit
set -o nounset

if [ $# -ne 1 ]
then
  BFSIZE=8G
else
  BFSIZE=$1
fi

echo "Buffer pool size will be $BFSIZE"

MYSQL_DATA=~/tpcc_mysql
MYSQL_DATA_ORIG=~/tpcc_mysql-32orig
MYSQL_BASE=~/mysql-5.5.7-rc-linux2.6-x86_64
MYSQL_OPTIONS="--innodb-buffer-pool-size=$BFSIZE
       --innodb_log_file_size=1800M
       --innodb_flush_method=O_DIRECT
       --port=3400
       --transaction_isolation=serializable
       --max_connections=300
       --skip-grant-tables
       --query_cache_size=0" 
# this last line was just to disable the query caching of mysql to reduce the unncesserary logging


$MYSQL_BASE/bin/barzan_mysqld --no-defaults --basedir=$MYSQL_BASE --socket=mysql.sock \
       --datadir=$MYSQL_DATA --pid-file=mysql.pid --tmpdir=$MYSQL_DATA $MYSQL_OPTIONS &
