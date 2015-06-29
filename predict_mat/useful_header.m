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

cpu_usr=1;
cpu_sys=cpu_usr+1;
cpu_total=cpu_sys+1;
cpu_wai=cpu_total+1;
cpu_hiq=cpu_wai+1;
cpu_siq=cpu_hiq+1;
memory_used=cpu_siq+1;
memory_buff=memory_used+1;
memory_cach=memory_buff+1;
memory_free=memory_cach+1;
net_recv=memory_free+1;
net_send=net_recv+1;
dsk_read=net_send+1;
dsk_writ=dsk_read+1;
io_read=dsk_writ+1;
io_writ=io_read+1;
swap_used=io_writ+1;
swap_free=swap_used+1;
paging_in=swap_free+1;
paging_out=paging_in+1;
virtual_majpf=paging_out+1;
virtual_minpf=virtual_majpf+1;
virtual_alloc=virtual_minpf+1;
virtual_free=virtual_alloc+1;
filesystem_files=virtual_free+1;
filesystem_inodes=filesystem_files+1;
interupts_33=filesystem_inodes+1;
interupts_79=interupts_33+1;
interupts_80=interupts_79+1;
interupts_81=interupts_80+1;
interupts_82=interupts_81+1;
interupts_83=interupts_82+1;
interupts_84=interupts_83+1;
interupts_85=interupts_84+1;
interupts_86=interupts_85+1;
system_int=interupts_86+1;
system_csw=system_int+1;
procs_run=system_csw+1;
procs_blk=procs_run+1;
procs_new=procs_blk+1;
sda_util=procs_new+1;
Bytes_received=sda_util+1;
Bytes_sent=Bytes_received+1;
Com_commit=Bytes_sent+1;
Com_delete=Com_commit+1;
Com_insert=Com_delete+1;
Com_rollback=Com_insert+1;
Com_select=Com_rollback+1;
Com_update=Com_select+1;
Created_tmp_tables=Com_update+1;
Handler_commit=Created_tmp_tables+1;
Handler_delete=Handler_commit+1;
Handler_read_key=Handler_delete+1;
Handler_read_next=Handler_read_key+1;
Handler_read_prev=Handler_read_next+1;
Handler_rollback=Handler_read_prev+1;
Handler_update=Handler_rollback+1;
Handler_write=Handler_update+1;
Innodb_buffer_pool_pages_data=Handler_write+1;
Innodb_buffer_pool_pages_dirty=Innodb_buffer_pool_pages_data+1;
Innodb_buffer_pool_pages_flushed=Innodb_buffer_pool_pages_dirty+1;
Innodb_buffer_pool_pages_free=Innodb_buffer_pool_pages_flushed+1;
Innodb_buffer_pool_pages_misc=Innodb_buffer_pool_pages_free+1;
Innodb_buffer_pool_read_requests=Innodb_buffer_pool_pages_misc+1;
Innodb_buffer_pool_reads=Innodb_buffer_pool_read_requests+1;
Innodb_buffer_pool_write_requests=Innodb_buffer_pool_reads+1;
Innodb_data_fsyncs=Innodb_buffer_pool_write_requests+1;
Innodb_data_pending_reads=Innodb_data_fsyncs+1;
Innodb_data_read=Innodb_data_pending_reads+1;
Innodb_data_reads=Innodb_data_read+1;
Innodb_data_writes=Innodb_data_reads+1;
Innodb_data_written=Innodb_data_writes+1;
Innodb_dblwr_pages_written=Innodb_data_written+1;
Innodb_dblwr_writes=Innodb_dblwr_pages_written+1;
Innodb_log_write_requests=Innodb_dblwr_writes+1;
Innodb_log_writes=Innodb_log_write_requests+1;
Innodb_os_log_fsyncs=Innodb_log_writes+1;
Innodb_os_log_written=Innodb_os_log_fsyncs+1;
Innodb_pages_created=Innodb_os_log_written+1;
Innodb_pages_read=Innodb_pages_created+1;
Innodb_pages_written=Innodb_pages_read+1;
Innodb_row_lock_current_waits=Innodb_pages_written+1;
Innodb_row_lock_time=Innodb_row_lock_current_waits+1;
Innodb_row_lock_time_avg=Innodb_row_lock_time+1;
Innodb_row_lock_time_max=Innodb_row_lock_time_avg+1;
Innodb_row_lock_waits=Innodb_row_lock_time_max+1;
Innodb_rows_deleted=Innodb_row_lock_waits+1;
Innodb_rows_inserted=Innodb_rows_deleted+1;
Innodb_rows_read=Innodb_rows_inserted+1;
Innodb_rows_updated=Innodb_rows_read+1;
Open_tables=Innodb_rows_updated+1;
Opened_tables=Open_tables+1;
Queries=Opened_tables+1;
Questions=Queries+1;
Select_range=Questions+1;
Slow_queries=Select_range+1;
Table_locks_immediate=Slow_queries+1;
Threads_running=Table_locks_immediate+1;
Uptime=Threads_running+1;
Uptime_since_flush_status=Uptime+1;
