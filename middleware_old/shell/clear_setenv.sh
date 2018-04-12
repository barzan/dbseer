#!/bin/bash
sed -i 's/mysql_user='$mMYSQL_USER'/mysql_user=/' dstat_for_server/setenv
sed -i 's/mysql_pass='$mMYSQL_PASS'/mysql_pass=/' dstat_for_server/setenv
sed -i 's/mysql_host='$mMYSQL_HOST'/mysql_host=/' dstat_for_server/setenv
sed -i 's/mysql_port='$mMYSQL_PORT'/mysql_port=/' dstat_for_server/setenv
