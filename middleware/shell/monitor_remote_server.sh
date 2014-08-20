#!/bin/bash
ssh $1@$2 'cd dstat_for_server; rm -f log*; rm -f monitor.pid; chmod 755 dstat; /bin/bash monitor.sh'
