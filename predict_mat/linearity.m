filtered_header = '"Tran1","Tran2","Tran3","Tran4","Tran5","Latency1","Latency2","Latency3","Latency4","Latency5","cpu_usr","cpu_sys","cpu_total","cpu_wai","cpu_hiq","cpu_siq","memory_used","memory_buff","memory_cach","memory_free","net_recv","net_send","dsk_read","dsk_writ","io_read","io_writ","swap_used","swap_free","paging_in","paging_out","virtual_majpf","virtual_minpf","virtual_alloc","virtual_free","filesystem_files","filesystem_inodes","interupts_33","interupts_79","interupts_80","interupts_81","interupts_82","interupts_83","interupts_84","interupts_85","interupts_86","system_int","system_csw","procs_run","procs_blk","procs_new","sda_util","Bytes_received","Bytes_sent","Com_commit","Com_delete","Com_insert","Com_rollback","Com_select","Com_update","Created_tmp_tables","Handler_commit","Handler_delete","Handler_read_key","Handler_read_next","Handler_read_prev","Handler_rollback","Handler_update","Handler_write","Innodb_buffer_pool_pages_data","Innodb_buffer_pool_pages_dirty","Innodb_buffer_pool_pages_flushed","Innodb_buffer_pool_pages_free","Innodb_buffer_pool_pages_misc","Innodb_buffer_pool_read_requests","Innodb_buffer_pool_reads","Innodb_buffer_pool_write_requests","Innodb_data_fsyncs","Innodb_data_pending_reads","Innodb_data_read","Innodb_data_reads","Innodb_data_writes","Innodb_data_written","Innodb_dblwr_pages_written","Innodb_dblwr_writes","Innodb_log_write_requests","Innodb_log_writes","Innodb_os_log_fsyncs","Innodb_os_log_written","Innodb_pages_created","Innodb_pages_read","Innodb_pages_written","Innodb_row_lock_current_waits","Innodb_row_lock_time","Innodb_row_lock_time_avg","Innodb_row_lock_time_max","Innodb_row_lock_waits","Innodb_rows_deleted","Innodb_rows_inserted","Innodb_rows_read","Innodb_rows_updated","Open_tables","Opened_tables","Queries","Questions","Select_range","Slow_queries","Table_locks_immediate","Threads_running","Uptime","Uptime_since_flush_status"';
% = '"cpu_usr","cpu_sys","cpu_total","cpu_wai","cpu_hiq","cpu_siq","memory_used","memory_buff","memory_cach","memory_free","net_recv","net_send","dsk_read","dsk_writ","io_read","io_writ","swap_used","swap_free","paging_in","paging_out","virtual_majpf","virtual_minpf","virtual_alloc","virtual_free","filesystem_files","filesystem_inodes","interupts_33","interupts_79","interupts_80","interupts_81","interupts_82","interupts_83","interupts_84","interupts_85","interupts_86","system_int","system_csw","procs_run","procs_blk","procs_new","sda_util","Bytes_received","Bytes_sent","Com_commit","Com_delete","Com_insert","Com_rollback","Com_select","Com_update","Created_tmp_tables","Handler_commit","Handler_delete","Handler_read_key","Handler_read_next","Handler_read_prev","Handler_rollback","Handler_update","Handler_write","Innodb_buffer_pool_pages_data","Innodb_buffer_pool_pages_dirty","Innodb_buffer_pool_pages_flushed","Innodb_buffer_pool_pages_free","Innodb_buffer_pool_pages_misc","Innodb_buffer_pool_read_requests","Innodb_buffer_pool_reads","Innodb_buffer_pool_write_requests","Innodb_data_fsyncs","Innodb_data_pending_reads","Innodb_data_read","Innodb_data_reads","Innodb_data_writes","Innodb_data_written","Innodb_dblwr_pages_written","Innodb_dblwr_writes","Innodb_log_write_requests","Innodb_log_writes","Innodb_os_log_fsyncs","Innodb_os_log_written","Innodb_pages_created","Innodb_pages_read","Innodb_pages_written","Innodb_row_lock_current_waits","Innodb_row_lock_time","Innodb_row_lock_time_avg","Innodb_row_lock_time_max","Innodb_row_lock_waits","Innodb_rows_deleted","Innodb_rows_inserted","Innodb_rows_read","Innodb_rows_updated","Open_tables","Opened_tables","Queries","Questions","Select_range","Slow_queries","Table_locks_immediate","Threads_running","Uptime","Uptime_since_flush_status"';

%i stands for isolated workload
%b stands for binary workload
%r stands for limited rate
%M stands for aggregated monitoring stats
%C stands for count of transactions
%L stands for latencies
%B Big: B=[C L M]

[iC1 iL1 iM1] = loadAligned('/Users/sina/expr5/processed', '7200-60-10-10-10-2000');
iB1 = [iC1 iL1 iM1];
[iC3 iL3 iM3] = loadAligned('/Users/sina/expr5/processed', '7200-10-10-60-10-2000'); 
iB3 = [iC3 iL3 iM3];
[iC4 iL4 iM4] = loadAligned('/Users/sina/expr5/processed', '7200-10-10-10-60-2000');
iB4 = [iC4 iL4 iM4];
[bC12 bL12 bM12] = loadAligned('/Users/sina/expr5/processed', '7200-50-50--0--0-2000');
bB12 = [bC12 bL12 bM12];
[bC14 bL14 bM14] = loadAligned('/Users/sina/expr5/processed', '7200-50--0--0-50-2000');
bB14 = [bC14 bL14 bM14];
[bC15 bL15 bM15] = loadAligned('/Users/sina/expr5/processed', '7200-50--0--0--0-2000'); 
bB15 = [bC15 bL15 bM15];
[bC24 bL24 bM24] = loadAligned('/Users/sina/expr5/processed', '7200--0-50--0-50-2000');
bB24 = [bC24 bL24 bM24];
[bC45 bL45 bM45] = loadAligned('/Users/sina/expr5/processed', '7200--0--0--0-50-2000');
bB45 = [bC45 bL45 bM45];
[rC2 rL2 rM2] = loadAligned('/Users/sina/expr5/processed', '7200-20-20-20-20-2000');
rB2 = [rC2 rL2 rM2];
[rC3 rL3 rM3] = loadAligned('/Users/sina/expr5/processed', '7200-20-20-20-20-3000'); 
rB3 = [rC3 rL3 rM3];
[rC4 rL4 rM4] = loadAligned('/Users/sina/expr5/processed', '7200-20-20-20-20-4000'); 
rB4 = [rC4 rL4 rM4];
[rC5 rL5 rM5] = loadAligned('/Users/sina/expr5/processed', '7200-20-20-20-20-5000'); 
rB5 = [rC5 rL5 rM5];
[rC6 rL6 rM6] = loadAligned('/Users/sina/expr5/processed', '7200-20-20-20-20-6000'); 
rB6 = [rC6 rL6 rM6];

Call = [iC1; iC3; iC4; bC12; bC14; bC15; bC24; bC45; rC2; rC3; rC4; rC5; rC6];
Lall = [iL1; iL3; iL4; bL12; bL14; bL15; bL24; bL45; rL2; rL3; rL4; rL5; rL6];
Mall = [iM1; iM3; iM4; bM12; bM14; bM15; bM24; bM45; rM2; rM3; rM4; rM5; rM6];
Ball = [Call Lall Mall];

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ball.csv'));
dlmwrite('/Users/sina/expr5/weka/Ball.csv',Ball, 'delimiter',',','-append');        

%Callez = [iC1; iC3; iC4; bC12;     bC15; bC24; bC45; rC2; rC3; rC4; rC5; rC6];
%Lallez = [iL1; iL3; iL4; bL12;     bL15; bL24; bL45; rL2; rL3; rL4; rL5; rL6];
%Mallez = [iM1; iM3; iM4; bM12;     bM15; bM24; bM45; rM2; rM3; rM4; rM5; rM6];
Callez = [iC1; iC3; iC4; bC12;     bC15; bC24; bC45; rC2];
Lallez = [iL1; iL3; iL4; bL12;     bL15; bL24; bL45; rL2];
Mallez = [iM1; iM3; iM4; bM12;     bM15; bM24; bM45; rM2];
Ballez = [Callez Lallez Mallez];

Callez1 = [iC1; iC3; iC4; bC12;     bC15; rC2];
Lallez1 = [iL1; iL3; iL4; bL12;     bL15; rL2];
Mallez1 = [iM1; iM3; iM4; bM12;     bM15; rM2];
Ballez1 = [Callez1 Lallez1 Mallez1];


system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ballez1.csv'));
dlmwrite('/Users/sina/expr5/weka/Ballez1.csv',Ballez1, 'delimiter',',','-append');        

Callr = [rC2; rC3; rC4; rC5; rC6];
Lallr = [rL2; rL3; rL4; rL5; rL6];
Mallr = [rM2; rM3; rM4; rM5; rM6];
Ballr = [Callr Lallr Mallr];

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ballr.csv'));
dlmwrite('/Users/sina/expr5/weka/Ballr.csv',Ballr, 'delimiter',',','-append');        

Call1 = [iC1; bC12; bC14; bC15];
Lall1 = [iL1; bL12; bL14; bL15];
Mall1 = [iM1; bM12; bM14; bM15];
Ball1 = [Call1 Lall1 Mall1];

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ball1.csv'));
dlmwrite('/Users/sina/expr5/weka/Ball1.csv',Ball1, 'delimiter',',','-append'); 

%Non nulls for latency1

Ball_notnull = removeNans(Ball, 6);
system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ball_notnull.csv'));
dlmwrite('/Users/sina/expr5/weka/Ball_notnull.csv',Ball_notnull, 'delimiter',',','-append');        

Ballez_notnull = removeNans(Ballez, 6);
system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ballez_notnull.csv'));
dlmwrite('/Users/sina/expr5/weka/Ballez_notnull.csv',Ballez_notnull, 'delimiter',',','-append');  

Ballr_notnull = removeNans(Ballr, 6);
system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/Ballr_notnull.csv'));
dlmwrite('/Users/sina/expr5/weka/Ballr_notnull.csv',Ballr_notnull, 'delimiter',',','-append');    

%%%Others!

system(horzcat('echo ','"totalTrans",',filtered_header,' > /Users/sina/expr5/weka/bB24w_total.csv'));
dlmwrite('/Users/sina/expr5/weka/bB24w_total.csv',[sum(bC24')'  bB24], 'delimiter',',','-append');    

%
newFilVec = [cpu_total net_recv net_send dsk_read dsk_writ io_writ virtual_minpf system_int system_csw procs_run Com_commit Com_delete Com_insert Com_update Innodb_dblwr_pages_written Innodb_dblwr_writes Innodb_log_writes Innodb_os_log_fsyncs Innodb_pages_created Innodb_pages_written Innodb_row_lock_current_waits Innodb_row_lock_time Innodb_row_lock_time_avg Innodb_row_lock_time_max Innodb_row_lock_waits Innodb_rows_deleted Innodb_rows_inserted Innodb_rows_read Innodb_rows_updated];
newFilHed = '"Latency1","cpu_total","net_recv","net_send","dsk_read","dsk_writ","io_writ","virtual_minpf","system_int","system_csw","procs_run","Com_commit","Com_delete","Com_insert","Com_update","Innodb_dblwr_pages_written","Innodb_dblwr_writes","Innodb_log_writes","Innodb_os_log_fsyncs","Innodb_pages_created","Innodb_pages_written","Innodb_row_lock_current_waits","Innodb_row_lock_time","Innodb_row_lock_time_avg","Innodb_row_lock_time_max","Innodb_row_lock_waits","Innodb_rows_deleted","Innodb_rows_inserted","Innodb_rows_read","Innodb_rows_updated"';

system(horzcat('echo ', newFilHed ,' > /Users/sina/expr5/weka/iB3_30fil.csv'));
dlmwrite('/Users/sina/expr5/weka/iB3_30fil.csv',[iL3(:,1)  iM3(:,newFilVec)], 'delimiter',',','-append');    
system(horzcat('echo ', newFilHed ,' > /Users/sina/expr5/weka/iB1_30fil.csv'));
dlmwrite('/Users/sina/expr5/weka/iB1_30fil.csv',[iL1(:,1)  iM1(:,newFilVec)], 'delimiter',',','-append');    


system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/rB2.csv'));
dlmwrite('/Users/sina/expr5/weka/rB2.csv',rB2, 'delimiter',',','-append');    

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/bB14.csv'));
dlmwrite('/Users/sina/expr5/weka/bB14.csv',bB14, 'delimiter',',','-append');    

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/iB1.csv'));
dlmwrite('/Users/sina/expr5/weka/iB1.csv',iB1, 'delimiter',',','-append');    

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/iB3.csv'));
dlmwrite('/Users/sina/expr5/weka/iB3.csv',iB3, 'delimiter',',','-append');    

system(horzcat('echo ',filtered_header,' > /Users/sina/expr5/weka/iB4.csv'));
dlmwrite('/Users/sina/expr5/weka/iB4.csv', iB4, 'delimiter',',','-append');    

%%% Training M5 rules etc.
trainParams = m5pparams(true, 500, true, true, 15, 0.05);
[avgMSE, avgRMSE, avgRRMSE, avgR2, avgMAE, avgTime] = m5pcv([rC2 rM2], rL2(:,1), trainParams, ...
zeros(size(rM2,2)+size(rC2,2)), 3, false, false);

X = [rC2 rM2];
Y = rL2(:,1);
[model, time] = m5pbuild(X, Y, trainParams, zeros(size(X,2)), false);
YpredAll = m5ppredict(model, X);

X = rC2;
[model, time] = m5pbuild(X, Y, trainParams, zeros(size(X,2)), false);
YpredCounts = m5ppredict(model, X);

figure
subplot(2,2,1);
plot(YpredAll, 'b');
hold on;
plot(YpredCounts,'g');
plot(Y,'r');
l = legend('predicted latency (100 features)', 'predicted latency (trans counts only)', 'actual latency');
ylabel('Latency (sec)');
xlabel('different instances');
title('Latency Prediction');
grid;

subplot(2,2,2);
plot(YpredAll, 'b');
hold on;
plot(YpredCounts,'g');
plot(Y,'r');
l = legend('predicted latency (100 features)', 'predicted latency (trans counts only)', 'actual latency');
ylabel('Latency (sec)');
xlabel('different instances');
title('Latency Prediction (zoomed in)');
axis([0 20 0.1 0.2])
grid;

subplot(2,2,3);
plot(YpredAll, 'b');
hold on;
plot(YpredCounts,'g');
plot(Y,'r');
l = legend('predicted latency (100 features)', 'predicted latency (trans counts only)', 'actual latency');
ylabel('Latency (sec)');
xlabel('different instances');
title('Latency Prediction (zoomed in)');
axis([0 100 0.1 0.2])
grid;

subplot(2,2,4);
plot(YpredAll, 'b');
hold on;
plot(YpredCounts,'g');
plot(Y,'r');
l = legend('predicted latency (100 features)', 'predicted latency (trans counts only)', 'actual latency');
ylabel('Latency (sec)');
xlabel('different instances');
title('Latency Prediction (zoomed in)');
axis([600 700 0 1])
grid;


set(gcf,'Color','w');

%%%

avgSignal = [mean(iB1); mean(iB3); mean(iB4); mean(bB12); mean(bB14); mean(bB15); mean(bB24); mean(bB45); mean(rB2); mean(rB3); mean(rB4); mean(rB5); mean(rB6)];





