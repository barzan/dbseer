#!/bin/bash

echo dropping tables
cat ~/tables/droptables.sql | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc
echo "(re) creating tables"
cat ~/tables/createtables.sql | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc
echo creating constraints
cat ~/tables/constraints.sql | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc

#now insertion begins!
echo inserting cputable data
cat ~/tables/cputable.ctl | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc
echo inserting iotable data
cat ~/tables/iotable.ctl | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc
echo inserting iotableSmallrow data
cat ~/tables/iotableSmallrow.ctl | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc
echo inserting locktable data
cat ~/tables/locktable.ctl | mysql -u root -h 127.0.0.1 -P 3400 -D tpcc


