
#!/bin/sh
DATADIR=/home/barzan/tpcc_mysql
BASEDIR=/home/barzan/mysql-5.5.7-rc-linux2.6-x86_64

OPTIONS="--innodb-buffer-pool-size=8G
       --innodb_log_file_size=1800M
       --innodb_flush_method=O_DIRECT
       --port=3400
       --transaction_isolation=serializable
       --max_connections=300"

$BASEDIR/bin/mysqld --no-defaults --basedir=$BASEDIR --socket=mysql.sock \
       --datadir=$DATADIR --pid-file=mysql.pid --tmpdir=$DATADIR $OPTIONS 


#--skip-grant-tables



18 1 20 20 20 20 20 0 0 0 0 0 0
18 1 20 20 20 20 20 0 0 0 0 0 0
