#!/bin/bash
ssh $1@$2 'rm -rf dstat_for_server'
scp -r dstat_for_server $1@$2:~/
