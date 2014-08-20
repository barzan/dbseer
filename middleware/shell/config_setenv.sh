#!/bin/bash
sed -i 's/mysql_user=/mysql_user='$mMYSQL_USER'/' dstat_for_server/setenv
sed -i 's/mysql_pass=/mysql_pass='$mMYSQL_PASS'/' dstat_for_server/setenv
sed -i 's/mysql_host=/mysql_host='$mMYSQL_HOST'/' dstat_for_server/setenv
sed -i 's/mysql_port=/mysql_port='$mMYSQL_PORT'/' dstat_for_server/setenv
