% Copyright 2013 Barzan Mozafari
%
% Licensed under the Apache License, Version 2.0 (the "License");
% you may not use this file except in compliance with the License.
% You may obtain a copy of the License at
%
%     http://www.apache.org/licenses/LICENSE-2.0
%
% Unless required by applicable law or agreed to in writing, software
% distributed under the License is distributed on an "AS IS" BASIS,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
% See the License for the specific language governing permissions and
% limitations under the License.

function [diffedM] = diff_if_necessary(M)

header_aligned;

diffme = [Bytes_received Bytes_sent Com_commit Com_delete Com_insert Com_rollback Com_select Com_update Created_tmp_tables Handler_commit Handler_delete Handler_read_key Handler_read_next Handler_read_prev Handler_rollback Handler_update Handler_write Innodb_buffer_pool_pages_flushed Innodb_buffer_pool_read_requests Innodb_buffer_pool_reads Innodb_buffer_pool_write_requests Innodb_data_fsyncs Innodb_data_read Innodb_data_reads Innodb_data_writes Innodb_data_written Innodb_dblwr_pages_written Innodb_dblwr_writes Innodb_log_write_requests Innodb_log_writes Innodb_os_log_fsyncs Innodb_os_log_written Innodb_pages_created Innodb_pages_read Innodb_pages_written Innodb_row_lock_time Innodb_row_lock_time_max Innodb_row_lock_waits Innodb_rows_deleted Innodb_rows_inserted Innodb_rows_read Innodb_rows_updated Opened_tables Queries Questions Select_range Slow_queries Table_locks_immediate Uptime Uptime_since_flush_status];
d = diff(M);

diffedM = M(2:end,:);
diffedM(:,diffme)= d(:,diffme);

%size(nodiff)
%size(diffme)

end

