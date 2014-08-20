#!/bin/bash
ssh $1@$2 'cd dstat_for_server; pkill -15 -P $(cat monitor.pid)'
scp $1@$2:~/dstat_for_server/log* Transactions/
