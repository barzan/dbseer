useful_header;
filtered_header = {'cpu_usr' 'cpu_sys' 'cpu_total' 'cpu_wai' 'cpu_hiq' 'cpu_siq' 'memory_used' 'memory_buff' 'memory_cach' 'memory_free' 'net_recv' 'net_send' 'dsk_read' 'dsk_writ' 'io_read' 'io_writ' 'swap_used' 'swap_free' 'paging_in' 'paging_out' 'virtual_majpf' 'virtual_minpf' 'virtual_alloc' 'virtual_free' 'filesystem_files' 'filesystem_inodes' 'interupts_33' 'interupts_79' 'interupts_80' 'interupts_81' 'interupts_82' 'interupts_83' 'interupts_84' 'interupts_85' 'interupts_86' 'system_int' 'system_csw' 'procs_run' 'procs_blk' 'procs_new' 'sda_util' 'Bytes_received' 'Bytes_sent' 'Com_commit' 'Com_delete' 'Com_insert' 'Com_rollback' 'Com_select' 'Com_update' 'Created_tmp_tables' 'Handler_commit' 'Handler_delete' 'Handler_read_key' 'Handler_read_next' 'Handler_read_prev' 'Handler_rollback' 'Handler_update' 'Handler_write' 'Innodb_buffer_pool_pages_data' 'Innodb_buffer_pool_pages_dirty' 'Innodb_buffer_pool_pages_flushed' 'Innodb_buffer_pool_pages_free' 'Innodb_buffer_pool_pages_misc' 'Innodb_buffer_pool_read_requests' 'Innodb_buffer_pool_reads' 'Innodb_buffer_pool_write_requests' 'Innodb_data_fsyncs' 'Innodb_data_pending_reads' 'Innodb_data_read' 'Innodb_data_reads' 'Innodb_data_writes' 'Innodb_data_written' 'Innodb_dblwr_pages_written' 'Innodb_dblwr_writes' 'Innodb_log_write_requests' 'Innodb_log_writes' 'Innodb_os_log_fsyncs' 'Innodb_os_log_written' 'Innodb_pages_created' 'Innodb_pages_read' 'Innodb_pages_written' 'Innodb_row_lock_current_waits' 'Innodb_row_lock_time' 'Innodb_row_lock_time_avg' 'Innodb_row_lock_time_max' 'Innodb_row_lock_waits' 'Innodb_rows_deleted' 'Innodb_rows_inserted' 'Innodb_rows_read' 'Innodb_rows_updated' 'Open_tables' 'Opened_tables' 'Queries' 'Questions' 'Select_range' 'Slow_queries' 'Table_locks_immediate' 'Threads_running' 'Uptime' 'Uptime_since_flush_status'};


s = alignEZ('7200-60-10-10-10-2000',0); %success
s = intersect(s, alignEZ('7200-10-60-10-10-2000',0)); %maybe
s = intersect(s, alignEZ('7200-10-10-60-10-2000',0)); %success
s = intersect(s, alignEZ('7200-10-10-10-60-2000',0)); %success
s = intersect(s, alignEZ('7200-10-10-10-10-2000',0)); %maybe
s = intersect(s, alignEZ('7200-50--0--0--0-2000',0)); %success
s = intersect(s, alignEZ('7200-50-50--0--0-2000',0)); %success
s = intersect(s, alignEZ('7200-50--0-50--0-2000',0)); %maybe
s = intersect(s, alignEZ('7200-50--0--0-50-2000',0)); %success
s = intersect(s, alignEZ('7200--0-50--0--0-2000',0)); %maybe
s = intersect(s, alignEZ('7200--0-50-50--0-2000',0)); %maybe
s = intersect(s, alignEZ('7200--0-50--0-50-2000',0)); %success
s = intersect(s, alignEZ('7200--0--0-50--0-2000',0)); %maybe
s = intersect(s, alignEZ('7200--0--0-50-50-2000',0)); %maybe
s = intersect(s, alignEZ('7200--0--0--0-50-2000',0)); %success
s = intersect(s, alignEZ('7200-20-20-20-20-100',0)); %give up
s = intersect(s, alignEZ('7200-20-20-20-20-500',0)); %give up
s = intersect(s, alignEZ('7200-20-20-20-20-1000',0)); %maybe
s = intersect(s, alignEZ('7200-20-20-20-20-1500',0)); %maybe
s = intersect(s, alignEZ('7200-20-20-20-20-2000',0)); % success
s = intersect(s, alignEZ('7200-20-20-20-20-3000',0)); %sucess
s = intersect(s, alignEZ('7200-20-20-20-20-4000',0)); %sucess
s = intersect(s, alignEZ('7200-20-20-20-20-5000',0)); %success
s = intersect(s, alignEZ('7200-20-20-20-20-6000',0)); %success

finalSugggestions = filtered_header(s);

finalSugggestions';