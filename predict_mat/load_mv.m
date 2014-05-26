function [mv] = load_mv(header_path, monitor_path, trans_count_path, avg_latency_path, percentile_latency_path, groupingStrategy)
overallTime = tic;

mv = struct();

mv.prclat = load(percentile_latency_path);
[monitor avglat IndividualCounts dM] = load_stats(monitor_path, trans_count_path, avg_latency_path, 0, 10, true);
if nargin > 5
    [monitor avglat IndividualCounts dM] = applyGroupingPolicy(groupingStrategy, monitor, avglat, IndividualCounts, dM);
end

run(header_path);
%signature_clean = strrep(signature, '-', '_');
%run([data_dir '/' signature_clean '_header']);
%if the above header does not exist, create one using the following
%command: 

mv.cpu_usr = monitor(:,header.metadata.cpu_usr);     % here we ignore hyperthreading, i.e. we take all CPUs from dstat data.
mv.cpu_sys = monitor(:,header.metadata.cpu_sys);
mv.cpu_idl = monitor(:,header.metadata.cpu_idl);
mv.numOfTransType = size(IndividualCounts,2);
mv.clientTransLatency = avglat;
mv.numberOfObservations = size(monitor, 1);

mv.CoreVariance = var(mv.cpu_usr,0,2);
[mv.AvgCpuUser mv.AvgCpuSys mv.AvgCpuIdle mv.AvgCpuWai mv.AvgCpuHiq mv.AvgCpuSiq] = CpuAggregate2(monitor(:,:), header);


mv.osAsynchronousIO = monitor(:,header.columns.aio);
mv.osNumberOfContextSwitches = monitor(:,header.columns.csw);
mv.osNumberOfSectorReads = monitor(:,header.columns.dsk_read)./1024./1024; %MB
mv.osNumberOfSectorWrites = monitor(:,header.columns.dsk_writ)./1024./1024; %MB
mv.osAllocatedFileHandlers = monitor(:,header.columns.filesystem_files);
mv.osAllocatedINodes = monitor(:,header.columns.filesystem_inodes);
mv.osCountOfInterruptsServicedSinceBootTime = monitor(:,header.columns.int);
fn = fieldnames(header.metadata.interrupts);
mv.osNumberOfInterrupt = length(fn);
for i=1:length(fn)
    eval(['mv.osInterruptCount' num2str(i) '= monitor(:,header.metadata.interrupts.' fn{i} ');']);
end
mv.osNumberOfReadsIssued = monitor(:,header.columns.io_read)./1024./1024; %MB
mv.osNumberOfWritesCompleted = monitor(:,header.columns.io_writ)./1024./1024; %MB
mv.osNumberOfSwapInSinceLastBoot = monitor(:,header.columns.paging_in);
mv.osNumberOfSwapOutSinceLastBoot = monitor(:,header.columns.paging_out);
mv.osNumberOfProcessesCreated = monitor(:,header.columns.procs_new);
mv.osNumberOfProcessesCurrentlyRunning = monitor(:,header.columns.procs_run);
mv.osDiskUtilization = monitor(:,header.columns.util);
mv.osFreeSwapSpace = monitor(:,header.columns.swap_free);
mv.osUsedSwapSpace = monitor(:,header.columns.swap_used);
mv.osNumberOfAllocatedPage = monitor(:,header.columns.virtual_alloc);
mv.osNumberOfFreePages = monitor(:,header.columns.virtual_free);
mv.osNumberOfMajorPageFaults = monitor(:,header.columns.virtual_majpf);
mv.osNumberOfMinorPageFaults = monitor(:,header.columns.virtual_minpf);
mv.osNetworkSendKB=monitor(:,header.metadata.net_send) ./1024;
mv.osNetworkRecvKB=monitor(:,header.metadata.net_recv)./1024;

mv.clientTotalSubmittedTrans=sum(IndividualCounts(:,:), 2);
mv.clientIndividualSubmittedTrans=IndividualCounts;


%Init
if strcmpi(header.dbms, 'mysql')
    mv.dbmsChangedRows = sum(dM(:,[header.columns.Innodb_rows_deleted header.columns.Innodb_rows_updated header.columns.Innodb_rows_inserted]),2);
    mv.dbmsCumChangedRows=cumsum(mv.dbmsChangedRows);
    mv.dbmsCumFlushedPages = monitor(:, header.columns.Innodb_buffer_pool_pages_flushed);
    mv.dbmsFlushedPages = DoSmooth(dM(:, header.columns.Innodb_buffer_pool_pages_flushed), 10);
    %mv.dbmsFlushedPages = dM(:, header.columns.Innodb_buffer_pool_pages_flushed);
    mv.dbmsCurrentDirtyPages = monitor(:, header.columns.Innodb_buffer_pool_pages_dirty);
    mv.dbmsDirtyPages = dM(:, header.columns.Innodb_buffer_pool_pages_dirty);
    mv.dbmsDataPages = monitor(:, header.columns.Innodb_buffer_pool_pages_data);
    mv.dbmsFreePages = monitor(:, header.columns.Innodb_buffer_pool_pages_free);
    mv.dbmsTotalPages = monitor(:, header.columns.Innodb_buffer_pool_pages_total); 
    mv.dbmsThreadsRunning = monitor(:, header.columns.Threads_running); 
    mv.dbmsTotalWritesMB=dM(:,header.columns.Innodb_data_written)./1024./1024; %MB
    mv.dbmsLogWritesMB=dM(:,header.columns.Innodb_os_log_written)./1024./1024; %MB
    mv.dbmsNumberOfPhysicalLogWrites=dM(:,header.columns.Innodb_log_writes);
    mv.dbmsNumberOfDataReads=dM(:,header.columns.Innodb_data_reads);
    mv.dbmsNumberOfDataWrites=dM(:,header.columns.Innodb_data_writes);
    
    mv.dbmsNumberOfLogWriteRequests=dM(:,header.columns.Innodb_log_write_requests);
    mv.dbmsNumberOfFysncLogWrites=dM(:,header.columns.Innodb_os_log_fsyncs);
    mv.dbmsNumberOfPendingLogWrites=dM(:,header.columns.Innodb_os_log_pending_writes);
    mv.dbmsNumberOfPendingLogFsyncs=dM(:,header.columns.Innodb_os_log_pending_fsyncs);

    mv.dbmsNumberOfNextRowReadRequests=dM(:,header.columns.Handler_read_rnd_next);
    mv.dbmsNumberOfRowInsertRequests=dM(:,header.columns.Handler_write);
    mv.dbmsNumberOfFirstEntryReadRequests=dM(:,header.columns.Handler_read_first);
    mv.dbmsNumberOfKeyBasedReadRequests=dM(:,header.columns.Handler_read_key);
    mv.dbmsNumberOfNextKeyBasedReadRequests=dM(:,header.columns.Handler_read_next);
    mv.dbmsNumberOfPrevKeyBasedReadRequests=dM(:,header.columns.Handler_read_prev);
    mv.dbmsNumberOfRowReadRequests=dM(:,header.columns.Handler_read_rnd);
    
    mv.dbmsPageWritesMB=dM(:,header.columns.Innodb_pages_written).*2.*16./1024; % to account for double write buffering
    mv.dbmsDoublePageWritesMB=dM(:,header.columns.Innodb_dblwr_pages_written).*2.*16./1024; % to account for double write buffering
    mv.dbmsDoubleWritesOperations=dM(:,header.columns.Innodb_dblwr_writes);

    mv.dbmsNumberOfPendingWrites=dM(:,header.columns.Innodb_data_pending_writes);
    mv.dbmsNumberOfPendingReads=dM(:,header.columns.Innodb_data_pending_reads);
    
    mv.dbmsBufferPoolWrites = dM(:,header.columns.Innodb_buffer_pool_write_requests);
    mv.dbmsRandomReadAheads = dM(:,header.columns.Innodb_buffer_pool_read_ahead_rnd);
    mv.dbmsSequentialReadAheads = dM(:,header.columns.Innodb_buffer_pool_read_ahead_seq);
    mv.dbmsNumberOfLogicalReadRequests = dM(:,header.columns.Innodb_buffer_pool_read_requests);
    mv.dbmsNumberOfLogicalReadsFromDisk = dM(:,header.columns.Innodb_buffer_pool_reads);
    mv.dbmsNumberOfWaitsForFlush = dM(:,header.columns.Innodb_buffer_pool_wait_free);

    % stats on how many and what type of SQL statements were run
    mv.dbmsCommittedCommands=dM(:,header.columns.Com_commit);
    mv.dbmsRolledbackCommands=dM(:,header.columns.Com_rollback);
    mv.dbmsRollbackHandler=dM(:,header.columns.Handler_rollback);
    
    % latency stats
    mv.measuredCPU=monitor(:,header.columns.mysqld_cpu)+monitor(:,header.columns.mysqld_children_cpu);
    mv.measuredWritesMB=monitor(:,header.columns.mysqld_bytes_written) / 1024 / 1024;
    mv.measuredReadsMB=monitor(:,header.columns.mysqld_bytes_read) / 1024 / 1024;

    lock_smoothing = 1;
    mv.dbmsCurrentLockWaits=DoSmooth(monitor(:,header.columns.Innodb_row_lock_current_waits), lock_smoothing);
    mv.dbmsLockWaits=DoSmooth(dM(:,header.columns.Innodb_row_lock_waits), lock_smoothing);
    mv.dbmsLockWaitTime=DoSmooth(dM(:,header.columns.Innodb_row_lock_time), lock_smoothing) / 1000; % to turn it into seconds!
    %mv.dbmsCurrentLockWaits=monitor(:,header.columns.Innodb_row_lock_current_waits);
    %mv.dbmsLockWaits=dM(:,header.columns.Innodb_row_lock_waits);
    %mv.dbmsLockWaitTime=dM(:,header.columns.Innodb_row_lock_time) / 1000; % to turn it into seconds!
    if exist('OCTAVE_VERSION')
        save('dbmsCurrentLockWaits_Octave.mat', '-v6', '-struct', 'mv', 'dbmsCurrentLockWaits');
        save('dbmsLockWaits_Octave.mat', '-v6', '-struct', 'mv', 'dbmsLockWaits');
        save('dbmsLockWaitTime_Octave.mat', '-v6', '-struct', 'mv', 'dbmsLockWaitTime');
    else
        save('dbmsCurrentLockWaits_Matlab', '-v6', '-struct', 'mv', 'dbmsCurrentLockWaits');
        save('dbmsLockWaits_Matlab', '-v6', '-struct', 'mv', 'dbmsLockWaits');
        save('dbmsLockWaitTime_Matlab', '-v6', '-struct', 'mv', 'dbmsLockWaitTime');
    end

    mv.dbmsReadRequests = dM(:, header.columns.Innodb_buffer_pool_read_requests);
    mv.dbmsReads = dM(:, header.columns.Innodb_buffer_pool_reads);
    mv.dbmsPhysicalReadsMB = dM(:,[header.columns.Innodb_data_read])./1024./1024;

elseif strcmpi(header.dbms, 'psql')
    mv.dbmsFlushedPages = DoSmooth(dM(:, header.columns.buffers_clean)+dM(:, header.columns.buffers_backend), 10);
    %mv.dbmsFlushedPages = dM(:, header.columns.buffers_clean)+dM(:, header.columns.buffers_backend);
    mv.dbmsLogWritesMB=dM(:,header.columns.buffers_checkpoint).*8192./1024./1024; %MB
    mv.dbmsPageWritesMB=(dM(:,header.columns.buffers_clean)+dM(:,header.columns.buffers_backend)).*2.*8./1024; % to account for double write buffering
    mv.dbmsCommittedCommands=dM(:,header.columns.xact_commit);
    mv.dbmsRolledbackCommands=dM(:,header.columns.xact_rollback);
    mv.measuredCPU=monitor(:,header.columns.postgres_cpu) + monitor(:,header.columns.postgres_children_cpu);
    mv.measuredWritesMB=monitor(:,header.columns.postgres_bytes_written) / 1024 / 1024;
    mv.measuredReadsMB=monitor(:,header.columns.postgres_bytes_read) / 1024 / 1024;
    mv.dbmsReadRequests = dM(:, header.columns.blks_read) + dM(:, header.columns.blks_hit);
    mv.dbmsReads = dM(:, header.columns.blks_read);
else
    error(['Sorry, we currently do not support the ' header.dbms ' DBMS']);
end

elapsed = toc(overallTime);
%fprintf(1,'load_modeling_variables time for %s=%f\n', signature, elapsed);

end

