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

function [mvGrouped mvUngrouped] = load_mv2(headers, monitors, avglats, prclats, IndividualCounts, dMs, groupingStrategy, tranTypes)
overallTime = tic;

num_dataset = size(headers,2);

mvGrouped = struct();
mvUngrouped = struct();

mvGrouped.headers = headers;
mvUngrouped.headers = headers;

%%% initialize
mvUngrouped.numOfTransType = [];
mvUngrouped.cpu_usr = [];
mvUngrouped.cpu_sys = [];
mvUngrouped.cpu_idl = [];
mvUngrouped.clientTransLatency = [];
mvUngrouped.numberOfObservations = [];
mvUngrouped.CoreVariance = [];
mvUngrouped.AvgCpuUser = [];
mvUngrouped.AvgCpuSys = [];
mvUngrouped.AvgCpuIdle = [];
mvUngrouped.AvgCpuWai = [];
mvUngrouped.AvgCpuHiq = [];
mvUngrouped.AvgCpuSiq = [];
mvUngrouped.osAsynchronousIO = [];
mvUngrouped.osNumberOfContextSwitches = [];
mvUngrouped.osNumberOfSectorReads = [];
mvUngrouped.osNumberOfSectorWrites = [];
mvUngrouped.osAllocatedINodes = [];
mvUngrouped.osAllocatedFileHandlers = [];
mvUngrouped.osCountOfInterruptsServicedSinceBootTime = [];
mvUngrouped.osNumberOfInterrupt = [];
mvUngrouped.osNumberOfReadsIssued = [];
mvUngrouped.osNumberOfWritesCompleted = [];
mvUngrouped.osNumberOfSwapInSinceLastBoot = [];
mvUngrouped.osNumberOfSwapOutSinceLastBoot = [];
mvUngrouped.osNumberOfProcessesCreated = [];
mvUngrouped.osNumberOfProcessesCurrentlyRunning = [];
mvUngrouped.osDiskUtilization = [];
mvUngrouped.osFreeSwapSpace = [];
mvUngrouped.osUsedSwapSpace = [];
mvUngrouped.osNumberOfAllocatedPage = [];
mvUngrouped.osNumberOfFreePages = [];
mvUngrouped.osNumberOfMajorPageFaults = [];
mvUngrouped.osNumberOfMinorPageFaults = [];
mvUngrouped.osNetworkSendKB = [];
mvUngrouped.osNetworkRecvKB = [];
mvUngrouped.clientTotalSubmittedTrans = [];
mvUngrouped.clientIndividualSubmittedTrans = [];
mvUngrouped.dbmsMeasuredCPU = [];
mvUngrouped.dbmsChangedRows = [];
mvUngrouped.dbmsCumChangedRows = [];
mvUngrouped.dbmsCumFlushedPages = [];
mvUngrouped.dbmsFlushedPages = [];
mvUngrouped.dbmsCurrentDirtyPages = [];
mvUngrouped.dbmsDirtyPages = [];
mvUngrouped.dbmsDataPages = [];
mvUngrouped.dbmsFreePages = [];
mvUngrouped.dbmsTotalPages = [];
mvUngrouped.dbmsThreadsRunning = [];
mvUngrouped.dbmsTotalWritesMB = [];
mvUngrouped.dbmsLogWritesMB = [];
mvUngrouped.dbmsNumberOfPhysicalLogWrites = [];
mvUngrouped.dbmsNumberOfDataReads = [];
mvUngrouped.dbmsNumberOfDataWrites = [];
mvUngrouped.dbmsNumberOfLogWriteRequests = [];
mvUngrouped.dbmsNumberOfFysncLogWrites = [];
mvUngrouped.dbmsNumberOfPendingLogWrites = [];
mvUngrouped.dbmsNumberOfPendingLogFsyncs = [];
mvUngrouped.dbmsNumberOfNextRowReadRequests = [];
mvUngrouped.dbmsNumberOfRowInsertRequests = [];
mvUngrouped.dbmsNumberOfFirstEntryReadRequests = [];
mvUngrouped.dbmsNumberOfKeyBasedReadRequests = [];
mvUngrouped.dbmsNumberOfNextKeyBasedReadRequests = [];
mvUngrouped.dbmsNumberOfPrevKeyBasedReadRequests = [];
mvUngrouped.dbmsNumberOfRowReadRequests = [];
mvUngrouped.dbmsPageWritesMB = [];
mvUngrouped.dbmsDoublePageWritesMB = [];
mvUngrouped.dbmsDoubleWritesOperations = [];
mvUngrouped.dbmsNumberOfPendingWrites = [];
mvUngrouped.dbmsNumberOfPendingReads = [];
mvUngrouped.dbmsBufferPoolWrites = [];
mvUngrouped.dbmsRandomReadAheads = [];
mvUngrouped.dbmsSequentialReadAheads = [];
mvUngrouped.dbmsNumberOfLogicalReadRequests = [];
mvUngrouped.dbmsNumberOfLogicalReadsFromDisk = [];
mvUngrouped.dbmsNumberOfWaitsForFlush = [];
mvUngrouped.dbmsCommittedCommands = [];
mvUngrouped.dbmsRolledbackCommands = [];
mvUngrouped.dbmsRollbackHandler = [];
mvUngrouped.dbmsCurrentLockWaits = [];
mvUngrouped.dbmsLockWaits = [];
mvUngrouped.dbmsLockWaitTime = [];
mvUngrouped.dbmsReadRequests = [];
mvUngrouped.dbmsReads = [];
mvUngrouped.dbmsPhysicalReadsMB = [];
mvUngrouped.dbmsPageSize = [];
mvUngrouped.dbmsBufferPoolSize = [];
mvUngrouped.dbmsLogFileSize = [];

mvGrouped.numOfTransType = [];
mvGrouped.cpu_usr = [];
mvGrouped.cpu_sys = [];
mvGrouped.cpu_idl = [];
mvGrouped.clientTransLatency = [];
mvGrouped.numberOfObservations = [];
mvGrouped.CoreVariance = [];
mvGrouped.AvgCpuUser = [];
mvGrouped.AvgCpuSys = [];
mvGrouped.AvgCpuIdle = [];
mvGrouped.AvgCpuWai = [];
mvGrouped.AvgCpuHiq = [];
mvGrouped.AvgCpuSiq = [];
mvGrouped.osAsynchronousIO = [];
mvGrouped.osNumberOfContextSwitches = [];
mvGrouped.osNumberOfSectorReads = [];
mvGrouped.osNumberOfSectorWrites = [];
mvGrouped.osAllocatedINodes = [];
mvGrouped.osAllocatedFileHandlers = [];
mvGrouped.osCountOfInterruptsServicedSinceBootTime = [];
mvGrouped.osNumberOfInterrupt = [];
mvGrouped.osNumberOfReadsIssued = [];
mvGrouped.osNumberOfWritesCompleted = [];
mvGrouped.osNumberOfSwapInSinceLastBoot = [];
mvGrouped.osNumberOfSwapOutSinceLastBoot = [];
mvGrouped.osNumberOfProcessesCreated = [];
mvGrouped.osNumberOfProcessesCurrentlyRunning = [];
mvGrouped.osDiskUtilization = [];
mvGrouped.osFreeSwapSpace = [];
mvGrouped.osUsedSwapSpace = [];
mvGrouped.osNumberOfAllocatedPage = [];
mvGrouped.osNumberOfFreePages = [];
mvGrouped.osNumberOfMajorPageFaults = [];
mvGrouped.osNumberOfMinorPageFaults = [];
mvGrouped.osNetworkSendKB = [];
mvGrouped.osNetworkRecvKB = [];
mvGrouped.clientTotalSubmittedTrans = [];
mvGrouped.clientIndividualSubmittedTrans = [];
mvGrouped.dbmsMeasuredCPU = [];
mvGrouped.dbmsChangedRows = [];
mvGrouped.dbmsCumChangedRows = [];
mvGrouped.dbmsCumFlushedPages = [];
mvGrouped.dbmsFlushedPages = [];
mvGrouped.dbmsCurrentDirtyPages = [];
mvGrouped.dbmsDirtyPages = [];
mvGrouped.dbmsDataPages = [];
mvGrouped.dbmsFreePages = [];
mvGrouped.dbmsTotalPages = [];
mvGrouped.dbmsThreadsRunning = [];
mvGrouped.dbmsTotalWritesMB = [];
mvGrouped.dbmsLogWritesMB = [];
mvGrouped.dbmsNumberOfPhysicalLogWrites = [];
mvGrouped.dbmsNumberOfDataReads = [];
mvGrouped.dbmsNumberOfDataWrites = [];
mvGrouped.dbmsNumberOfLogWriteRequests = [];
mvGrouped.dbmsNumberOfFysncLogWrites = [];
mvGrouped.dbmsNumberOfPendingLogWrites = [];
mvGrouped.dbmsNumberOfPendingLogFsyncs = [];
mvGrouped.dbmsNumberOfNextRowReadRequests = [];
mvGrouped.dbmsNumberOfRowInsertRequests = [];
mvGrouped.dbmsNumberOfFirstEntryReadRequests = [];
mvGrouped.dbmsNumberOfKeyBasedReadRequests = [];
mvGrouped.dbmsNumberOfNextKeyBasedReadRequests = [];
mvGrouped.dbmsNumberOfPrevKeyBasedReadRequests = [];
mvGrouped.dbmsNumberOfRowReadRequests = [];
mvGrouped.dbmsPageWritesMB = [];
mvGrouped.dbmsDoublePageWritesMB = [];
mvGrouped.dbmsDoubleWritesOperations = [];
mvGrouped.dbmsNumberOfPendingWrites = [];
mvGrouped.dbmsNumberOfPendingReads = [];
mvGrouped.dbmsBufferPoolWrites = [];
mvGrouped.dbmsRandomReadAheads = [];
mvGrouped.dbmsSequentialReadAheads = [];
mvGrouped.dbmsNumberOfLogicalReadRequests = [];
mvGrouped.dbmsNumberOfLogicalReadsFromDisk = [];
mvGrouped.dbmsNumberOfWaitsForFlush = [];
mvGrouped.dbmsCommittedCommands = [];
mvGrouped.dbmsRolledbackCommands = [];
mvGrouped.dbmsRollbackHandler = [];
mvGrouped.dbmsCurrentLockWaits = [];
mvGrouped.dbmsLockWaits = [];
mvGrouped.dbmsLockWaitTime = [];
mvGrouped.dbmsReadRequests = [];
mvGrouped.dbmsReads = [];
mvGrouped.dbmsPhysicalReadsMB = [];
mvGrouped.dbmsPageSize = [];
mvGrouped.dbmsBufferPoolSize = [];
mvGrouped.dbmsLogFileSize = [];

%%% END initialize

% Get min number of rows.
min_row = intmax;
min_row_d = intmax;

for i=1:num_dataset
  monitor = monitors{i};
  avglat = avglats{i};
  prclat = prclats{i};
  IndividualCount = IndividualCounts{i};
  dM = dMs{i};

  num_row = size(monitor, 1);
  if num_row < min_row
    min_row = num_row;
  end
  num_row = size(avglat, 1);
  if num_row < min_row
    min_row = num_row;
  end
  num_row = size(prclat.latenciesPCtile, 1);
  if num_row < min_row
    min_row = num_row;
  end
  num_row = size(IndividualCount, 1);
  if num_row < min_row
    min_row = num_row;
  end

  num_row_d = size(dM, 1);
  if num_row_d < min_row_d
    min_row_d = num_row_d;
  end

end

mvUngrouped.prclat = {};
mvGrouped.prclat = {};

for i=1:num_dataset

  monitor = monitors{i};
  header = headers{i};
  avglat = avglats{i};
  prclat = prclats{i};
  IndividualCount = IndividualCounts{i};
  dM = dMs{i};

  monitor = monitor(1:min_row, :);
  avglat = avglat(1:min_row, :);
  prclat.latenciesPCtile = prclat.latenciesPCtile(1:min_row, :, :);
  IndividualCount = IndividualCount(1:min_row, :);
  dM = dM(1:min_row_d, :);

  if nargin > 6
      [monitor_grouped avglat_grouped IndividualCount_grouped dM_grouped] = applyGroupingPolicy(groupingStrategy, monitor, avglat, IndividualCount, dM);
  else
      monitor_grouped = monitor;
      avglat_grouped = avglat;
      IndividualCount_grouped = IndividualCount;
      dM_grouped = dM;
  end

  monitor_grouped = monitor;
  avglat_grouped = avglat;
  IndividualCounts_grouped = IndividualCounts;
  dM_grouped = dM;

  mvUngrouped.prclat{end+1} = prclat;
  mvGrouped.prclat{end+1} = prclat;

  %%% For ungrouped mv
  mvUngrouped.cpu_usr = horzcat(mvUngrouped.cpu_usr, monitor(:,header.metadata.cpu_usr));     % here we ignore hyperthreading, i.e. we take all CPUs from dstat data.
  mvUngrouped.cpu_sys = horzcat(mvUngrouped.cpu_sys, monitor(:,header.metadata.cpu_sys));
  mvUngrouped.cpu_idl = horzcat(mvUngrouped.cpu_idl, monitor(:,header.metadata.cpu_idl));
  mvUngrouped.numOfTransType = horzcat(mvUngrouped.numOfTransType, size(IndividualCount,2));
  mvUngrouped.clientTransLatency = horzcat(mvUngrouped.clientTransLatency, avglat);
  mvUngrouped.numberOfObservations = horzcat(mvUngrouped.numberOfObservations, size(monitor, 1));

  mvUngrouped.CoreVariance = horzcat(mvUngrouped.CoreVariance, var(mvUngrouped.cpu_usr,0,2));
  %[mvUngrouped.AvgCpuUser mvUngrouped.AvgCpuSys mvUngrouped.AvgCpuIdle mvUngrouped.AvgCpuWai mvUngrouped.AvgCpuHiq mvUngrouped.AvgCpuSiq] = CpuAggregate2(monitor(:,:), header);
  [AvgCpuUser AvgCpuSys AvgCpuIdle AvgCpuWai AvgCpuHiq AvgCpuSiq] = CpuAggregate2(monitor(:,:), header);
  mvUngrouped.AvgCpuUser = horzcat(mvUngrouped.AvgCpuUser, AvgCpuUser);
  mvUngrouped.AvgCpuSys = horzcat(mvUngrouped.AvgCpuSys, AvgCpuSys);
  mvUngrouped.AvgCpuIdle = horzcat(mvUngrouped.AvgCpuIdle, AvgCpuIdle);
  mvUngrouped.AvgCpuWai = horzcat(mvUngrouped.AvgCpuWai, AvgCpuWai);
  mvUngrouped.AvgCpuHiq = horzcat(mvUngrouped.AvgCpuHiq, AvgCpuHiq);
  mvUngrouped.AvgCpuSiq = horzcat(mvUngrouped.AvgCpuSiq, AvgCpuSiq);

  mvUngrouped.osAsynchronousIO = horzcat(mvUngrouped.osAsynchronousIO, monitor(:,header.columns.aio));
  mvUngrouped.osNumberOfContextSwitches = horzcat(mvUngrouped.osNumberOfContextSwitches, monitor(:,header.columns.csw));
  if exist('extra') == 1
      mvUngrouped.osNumberOfSectorReads = horzcat(mvUngrouped.osNumberOfSectorReads,sum(monitor(:,extra.disk),2)./1024./1024); %MB
      mvUngrouped.osNumberOfSectorWrites = horzcat(mvUngrouped.osNumberOfSectorWrites,sum(monitor(:,header.extra.disk+1),2)./1024./1024); %MB
  else
      mvUngrouped.osNumberOfSectorReads = horzcat(mvUngrouped.osNumberOfSectorReads,monitor(:,header.columns.dsk_read)./1024./1024); %MB
      mvUngrouped.osNumberOfSectorWrites = horzcat(mvUngrouped.osNumberOfSectorWrites,monitor(:,header.columns.dsk_writ)./1024./1024); %MB
  end
  mvUngrouped.osAllocatedFileHandlers = horzcat(mvUngrouped.osAllocatedFileHandlers, monitor(:,header.columns.filesystem_files));
  mvUngrouped.osAllocatedINodes = horzcat(mvUngrouped.osAllocatedINodes, monitor(:,header.columns.filesystem_inodes));
  mvUngrouped.osCountOfInterruptsServicedSinceBootTime = horzcat(mvUngrouped.osCountOfInterruptsServicedSinceBootTime, monitor(:,header.columns.int));

  %% Ignore interrupts for now.
  %fn = fieldnames(header.metadata.interrupts);
  %mvUngrouped.osNumberOfInterrupt = length(fn);
  %for i=1:length(fn)
      %eval(['mvUngrouped.osInterruptCount' num2str(i) '= monitor(:,header.metadata.interrupts.' fn{i} ');']);
  %end

  if exist('extra') == 1
      mvUngrouped.osNumberOfReadsIssued = horzcat(mvUngrouped.osNumberOfReadsIssued, sum(monitor(:,extra.io),2)./1024./1024); %MB
      mvUngrouped.osNumberOfWritesCompleted = horzcat(mvUngrouped.osNumberOfWritesCompleted, sum(monitor(:,extra.io+1),2)./1024./1024); %MB
  else
      mvUngrouped.osNumberOfReadsIssued = horzcat(mvUngrouped.osNumberOfReadsIssued, monitor(:,header.columns.io_read)./1024./1024); %MB
      mvUngrouped.osNumberOfWritesCompleted = horzcat(mvUngrouped.osNumberOfWritesCompleted, monitor(:,header.columns.io_writ)./1024./1024); %MB
  end
  mvUngrouped.osNumberOfSwapInSinceLastBoot = horzcat(mvUngrouped.osNumberOfSwapInSinceLastBoot, monitor(:,header.columns.paging_in));
  mvUngrouped.osNumberOfSwapOutSinceLastBoot = horzcat(mvUngrouped.osNumberOfSwapOutSinceLastBoot, monitor(:,header.columns.paging_out));
  mvUngrouped.osNumberOfProcessesCreated = horzcat(mvUngrouped.osNumberOfProcessesCreated, monitor(:,header.columns.procs_new));
  mvUngrouped.osNumberOfProcessesCurrentlyRunning = horzcat(mvUngrouped.osNumberOfProcessesCurrentlyRunning, monitor(:,header.columns.procs_run));
  if exist('extra') == 1
      mvUngrouped.osDiskUtilization = horzcat(mvUngrouped.osDiskUtilization, sum(monitor(:,extra.util),2));
  else
      mvUngrouped.osDiskUtilization = horzcat(mvUngrouped.osDiskUtilization, monitor(:,header.columns.util));
  end

  mvUngrouped.osFreeSwapSpace = horzcat(mvUngrouped.osFreeSwapSpace, monitor(:,header.columns.swap_free));
  mvUngrouped.osUsedSwapSpace = horzcat(mvUngrouped.osUsedSwapSpace, monitor(:,header.columns.swap_used));
  mvUngrouped.osNumberOfAllocatedPage = horzcat(mvUngrouped.osNumberOfAllocatedPage, monitor(:,header.columns.virtual_alloc));
  mvUngrouped.osNumberOfFreePages = horzcat(mvUngrouped.osNumberOfFreePages, monitor(:,header.columns.virtual_free));
  mvUngrouped.osNumberOfMajorPageFaults = horzcat(mvUngrouped.osNumberOfMajorPageFaults, monitor(:,header.columns.virtual_majpf));
  mvUngrouped.osNumberOfMinorPageFaults = horzcat(mvUngrouped.osNumberOfMinorPageFaults, monitor(:,header.columns.virtual_minpf));
  mvUngrouped.osNetworkSendKB = horzcat(mvUngrouped.osNetworkSendKB, sum(monitor(:,header.metadata.net_send),2) ./1024);
  mvUngrouped.osNetworkRecvKB = horzcat(mvUngrouped.osNetworkRecvKB, sum(monitor(:,header.metadata.net_recv),2) ./1024);

  mvUngrouped.clientTotalSubmittedTrans = horzcat(mvUngrouped.clientTotalSubmittedTrans, IndividualCount);
  mvUngrouped.clientTotalSubmittedTrans = sum(mvUngrouped.clientTotalSubmittedTrans, 2);
  mvUngrouped.clientIndividualSubmittedTrans= horzcat(mvUngrouped.clientIndividualSubmittedTrans, IndividualCount);

  %Init
  if strcmpi(header.dbms, 'mysql')

    if isfield(header.columns, 'mysql_cpu')
      mvUngrouped.dbmsMeasuredCPU = monitor(:, header.columns.mysql_cpu);
    end

    mvUngrouped.dbmsChangedRows = horzcat(mvUngrouped.dbmsChangedRows, sum(dM(:,[header.columns.Innodb_rows_deleted header.columns.Innodb_rows_updated header.columns.Innodb_rows_inserted]),2));
    mvUngrouped.dbmsCumChangedRows = horzcat(mvUngrouped.dbmsCumChangedRows, cumsum(mvUngrouped.dbmsChangedRows));
    mvUngrouped.dbmsCumFlushedPages = horzcat(mvUngrouped.dbmsCumFlushedPages, monitor(:, header.columns.Innodb_buffer_pool_pages_flushed));
    mvUngrouped.dbmsFlushedPages = horzcat(mvUngrouped.dbmsFlushedPages, DoSmooth(dM(:, header.columns.Innodb_buffer_pool_pages_flushed), 10));
    mvUngrouped.dbmsCurrentDirtyPages = horzcat(mvUngrouped.dbmsCurrentDirtyPages, monitor(:, header.columns.Innodb_buffer_pool_pages_dirty));
    mvUngrouped.dbmsDirtyPages = horzcat(mvUngrouped.dbmsDirtyPages, dM(:, header.columns.Innodb_buffer_pool_pages_dirty));
    mvUngrouped.dbmsDataPages = horzcat(mvUngrouped.dbmsDataPages, monitor(:, header.columns.Innodb_buffer_pool_pages_data));
    mvUngrouped.dbmsFreePages = horzcat(mvUngrouped.dbmsFreePages, monitor(:, header.columns.Innodb_buffer_pool_pages_free));
    mvUngrouped.dbmsTotalPages = horzcat(mvUngrouped.dbmsTotalPages, monitor(:, header.columns.Innodb_buffer_pool_pages_total));
    mvUngrouped.dbmsThreadsRunning = horzcat(mvUngrouped.dbmsThreadsRunning, monitor(:, header.columns.Threads_running));
    mvUngrouped.dbmsTotalWritesMB = horzcat(mvUngrouped.dbmsTotalWritesMB, dM(:,header.columns.Innodb_data_written)./1024./1024); %MB
    mvUngrouped.dbmsLogWritesMB = horzcat(mvUngrouped.dbmsLogWritesMB, dM(:,header.columns.Innodb_os_log_written)./1024./1024); %MB
    mvUngrouped.dbmsNumberOfPhysicalLogWrites = horzcat(mvUngrouped.dbmsNumberOfPhysicalLogWrites, dM(:,header.columns.Innodb_log_writes));
    mvUngrouped.dbmsNumberOfDataReads = horzcat(mvUngrouped.dbmsNumberOfDataReads, dM(:,header.columns.Innodb_data_reads));
    mvUngrouped.dbmsNumberOfDataWrites = horzcat(mvUngrouped.dbmsNumberOfDataWrites, dM(:,header.columns.Innodb_data_writes));

    mvUngrouped.dbmsNumberOfLogWriteRequests = horzcat(mvUngrouped.dbmsNumberOfLogWriteRequests, dM(:,header.columns.Innodb_log_write_requests));
    mvUngrouped.dbmsNumberOfFysncLogWrites = horzcat(mvUngrouped.dbmsNumberOfFysncLogWrites, dM(:,header.columns.Innodb_os_log_fsyncs));
    mvUngrouped.dbmsNumberOfPendingLogWrites = horzcat(mvUngrouped.dbmsNumberOfPendingLogWrites, dM(:,header.columns.Innodb_os_log_pending_writes));
    mvUngrouped.dbmsNumberOfPendingLogFsyncs = horzcat(mvUngrouped.dbmsNumberOfPendingLogFsyncs, dM(:,header.columns.Innodb_os_log_pending_fsyncs));

    mvUngrouped.dbmsNumberOfNextRowReadRequests = horzcat(mvUngrouped.dbmsNumberOfNextRowReadRequests, dM(:,header.columns.Handler_read_rnd_next));
    mvUngrouped.dbmsNumberOfRowInsertRequests = horzcat(mvUngrouped.dbmsNumberOfRowInsertRequests, dM(:,header.columns.Handler_write));
    mvUngrouped.dbmsNumberOfFirstEntryReadRequests = horzcat(mvUngrouped.dbmsNumberOfFirstEntryReadRequests, dM(:,header.columns.Handler_read_first));
    mvUngrouped.dbmsNumberOfKeyBasedReadRequests = horzcat(mvUngrouped.dbmsNumberOfKeyBasedReadRequests, dM(:,header.columns.Handler_read_key));
    mvUngrouped.dbmsNumberOfNextKeyBasedReadRequests = horzcat(mvUngrouped.dbmsNumberOfNextKeyBasedReadRequests, dM(:,header.columns.Handler_read_next));
    mvUngrouped.dbmsNumberOfPrevKeyBasedReadRequests = horzcat(mvUngrouped.dbmsNumberOfPrevKeyBasedReadRequests, dM(:,header.columns.Handler_read_prev));
    mvUngrouped.dbmsNumberOfRowReadRequests = horzcat(mvUngrouped.dbmsNumberOfRowReadRequests, dM(:,header.columns.Handler_read_rnd));

    mvUngrouped.dbmsPageWritesMB = horzcat(mvUngrouped.dbmsPageWritesMB, dM(:,header.columns.Innodb_pages_written).*2.*16./1024); % to account for double write buffering
    mvUngrouped.dbmsDoublePageWritesMB = horzcat(mvUngrouped.dbmsDoublePageWritesMB, dM(:,header.columns.Innodb_dblwr_pages_written).*2.*16./1024); % to account for double write buffering
    mvUngrouped.dbmsDoubleWritesOperations = horzcat(mvUngrouped.dbmsDoubleWritesOperations, dM(:,header.columns.Innodb_dblwr_writes));

    mvUngrouped.dbmsNumberOfPendingWrites = horzcat(mvUngrouped.dbmsNumberOfPendingWrites, dM(:,header.columns.Innodb_data_pending_writes));
    mvUngrouped.dbmsNumberOfPendingReads = horzcat(mvUngrouped.dbmsNumberOfPendingReads, dM(:,header.columns.Innodb_data_pending_reads));

    mvUngrouped.dbmsBufferPoolWrites = horzcat(mvUngrouped.dbmsBufferPoolWrites, dM(:,header.columns.Innodb_buffer_pool_write_requests));
    mvUngrouped.dbmsRandomReadAheads = horzcat(mvUngrouped.dbmsRandomReadAheads, dM(:,header.columns.Innodb_buffer_pool_read_ahead_rnd));
    mvUngrouped.dbmsSequentialReadAheads = horzcat(mvUngrouped.dbmsSequentialReadAheads, dM(:,header.columns.Innodb_buffer_pool_read_ahead_seq));
    mvUngrouped.dbmsNumberOfLogicalReadRequests = horzcat(mvUngrouped.dbmsNumberOfLogicalReadRequests, dM(:,header.columns.Innodb_buffer_pool_read_requests));
    mvUngrouped.dbmsNumberOfLogicalReadsFromDisk = horzcat(mvUngrouped.dbmsNumberOfLogicalReadsFromDisk, dM(:,header.columns.Innodb_buffer_pool_reads));
    mvUngrouped.dbmsNumberOfWaitsForFlush = horzcat(mvUngrouped.dbmsNumberOfWaitsForFlush, dM(:,header.columns.Innodb_buffer_pool_wait_free));

    % stats on how many and what type of SQL statements were run
    mvUngrouped.dbmsCommittedCommands = horzcat(mvUngrouped.dbmsCommittedCommands, dM(:,header.columns.Com_commit));
    mvUngrouped.dbmsRolledbackCommands = horzcat(mvUngrouped.dbmsRolledbackCommands, dM(:,header.columns.Com_rollback));
    mvUngrouped.dbmsRollbackHandler = horzcat(mvUngrouped.dbmsRollbackHandler, dM(:,header.columns.Handler_rollback));

    lock_smoothing = 1;
    mvUngrouped.dbmsCurrentLockWaits = horzcat(mvUngrouped.dbmsCurrentLockWaits, DoSmooth(monitor(:,header.columns.Innodb_row_lock_current_waits), lock_smoothing));
    mvUngrouped.dbmsLockWaits = horzcat(mvUngrouped.dbmsLockWaits, DoSmooth(dM(:,header.columns.Innodb_row_lock_waits), lock_smoothing));
    mvUngrouped.dbmsLockWaitTime = horzcat(mvUngrouped.dbmsLockWaitTime, DoSmooth(dM(:,header.columns.Innodb_row_lock_time), lock_smoothing) / 1000); % to turn it into seconds!

    mvUngrouped.dbmsReadRequests = horzcat(mvUngrouped.dbmsReadRequests, dM(:, header.columns.Innodb_buffer_pool_read_requests));
    mvUngrouped.dbmsReads = horzcat(mvUngrouped.dbmsReads, dM(:, header.columns.Innodb_buffer_pool_reads));
    mvUngrouped.dbmsPhysicalReadsMB = horzcat(mvUngrouped.dbmsPhysicalReadsMB, dM(:,[header.columns.Innodb_data_read])./1024./1024);

    if isfield(header.columns, 'Innodb_page_size')
      mvUngrouped.dbmsPageSize = horzcat(mvUngrouped.dbmsPageSize, max(monitor(:, header.columns.Innodb_page_size)));
    end
    if isfield(header.columns, 'Innodb_buffer_pool_size')
      mvUngrouped.dbmsBufferPoolSize = horzcat(mvUngrouped.dbmsBufferPoolSize, max(monitor(:, header.columns.Innodb_buffer_pool_size)));
    end
    if isfield(header.columns, 'Innodb_log_file_size')
      mvUngrouped.dbmsLogFileSize = horzcat(mvUngrouped.dbmsLogFileSize, max(monitor(:, header.columns.Innodb_log_file_size)));
    end

  elseif strcmpi(header.dbms, 'psql')
      mvUngrouped.dbmsFlushedPages = horzcat(mvUngrouped.dbmsFlushedPages, DoSmooth(dM(:, header.columns.buffers_clean)+dM(:, header.columns.buffers_backend), 10));
      %mvUngrouped.dbmsFlushedPages = dM(:, header.columns.buffers_clean)+dM(:, header.columns.buffers_backend);
      mvUngrouped.dbmsLogWritesMB =horzcat(mvUngrouped.dbmsLogWritesMB, dM(:,header.columns.buffers_checkpoint).*8192./1024./1024); %MB
      mvUngrouped.dbmsPageWritesMB = horzcat(mvUngrouped.dbmsPageWritesMB, (dM(:,header.columns.buffers_clean)+dM(:,header.columns.buffers_backend)).*2.*8./1024); % to account for double write buffering
      mvUngrouped.dbmsCommittedCommands = horzcat(mvUngrouped.dbmsCommittedCommands, dM(:,header.columns.xact_commit));
      mvUngrouped.dbmsRolledbackCommands = horzcat(mvUngrouped.dbmsRolledbackCommands, dM(:,header.columns.xact_rollback));
      mvUngrouped.measuredCPU = horzcat(mvUngrouped.measuredCPU, monitor(:,header.columns.postgres_cpu) + monitor(:,header.columns.postgres_children_cpu));
      mvUngrouped.measuredWritesMB = horzcat(mvUngrouped.measuredWritesMB, monitor(:,header.columns.postgres_bytes_written) / 1024 / 1024);
      mvUngrouped.measuredReadsMB = horzcat(mvUngrouped.measuredReadsMB, monitor(:,header.columns.postgres_bytes_read) / 1024 / 1024);
      mvUngrouped.dbmsReadRequests = horzcat(mvUngrouped.dbmsReadRequests, dM(:, header.columns.blks_read) + dM(:, header.columns.blks_hit));
      mvUngrouped.dbmsReads = horzcat(mvUngrouped.dbmsReads, dM(:, header.columns.blks_read));
  else
      error(['Sorry, we currently do not support the ' header.dbms ' DBMS']);
  end


  %%% For grouped mv
  mvGrouped.cpu_usr = horzcat(mvGrouped.cpu_usr, monitor_grouped(:,header.metadata.cpu_usr));     % here we ignore hyperthreading, i.e. we take all CPUs from dstat data.
  mvGrouped.cpu_sys = horzcat(mvGrouped.cpu_sys, monitor_grouped(:,header.metadata.cpu_sys));
  mvGrouped.cpu_idl = horzcat(mvGrouped.cpu_idl, monitor_grouped(:,header.metadata.cpu_idl));
  mvGrouped.numOfTransType = horzcat(mvGrouped.numOfTransType, size(IndividualCount_grouped,2));
  mvGrouped.clientTransLatency = horzcat(mvGrouped.clientTransLatency, avglat);
  mvGrouped.numberOfObservations = horzcat(mvGrouped.numberOfObservations, size(monitor_grouped, 1));

  mvGrouped.CoreVariance = horzcat(mvGrouped.CoreVariance, var(mvGrouped.cpu_usr,0,2));
  %[mvGrouped.AvgCpuUser mvGrouped.AvgCpuSys mvGrouped.AvgCpuIdle mvGrouped.AvgCpuWai mvGrouped.AvgCpuHiq mvGrouped.AvgCpuSiq] = CpuAggregate2(monitor_grouped(:,:), header);
  [AvgCpuUser AvgCpuSys AvgCpuIdle AvgCpuWai AvgCpuHiq AvgCpuSiq] = CpuAggregate2(monitor_grouped(:,:), header);
  mvGrouped.AvgCpuUser = horzcat(mvGrouped.AvgCpuUser, AvgCpuUser);
  mvGrouped.AvgCpuSys = horzcat(mvGrouped.AvgCpuSys, AvgCpuSys);
  mvGrouped.AvgCpuIdle = horzcat(mvGrouped.AvgCpuIdle, AvgCpuIdle);
  mvGrouped.AvgCpuWai = horzcat(mvGrouped.AvgCpuWai, AvgCpuWai);
  mvGrouped.AvgCpuHiq = horzcat(mvGrouped.AvgCpuHiq, AvgCpuHiq);
  mvGrouped.AvgCpuSiq = horzcat(mvGrouped.AvgCpuSiq, AvgCpuSiq);


  mvGrouped.osAsynchronousIO = horzcat(mvGrouped.osAsynchronousIO, monitor_grouped(:,header.columns.aio));
  mvGrouped.osNumberOfContextSwitches = horzcat(mvGrouped.osNumberOfContextSwitches, monitor_grouped(:,header.columns.csw));
  if exist('extra') == 1
      mvGrouped.osNumberOfSectorReads = horzcat(mvGrouped.osNumberOfSectorReads,sum(monitor_grouped(:,extra.disk),2)./1024./1024); %MB
      mvGrouped.osNumberOfSectorWrites = horzcat(mvGrouped.osNumberOfSectorWrites,sum(monitor_grouped(:,header.extra.disk+1),2)./1024./1024); %MB
  else
      mvGrouped.osNumberOfSectorReads = horzcat(mvGrouped.osNumberOfSectorReads,monitor_grouped(:,header.columns.dsk_read)./1024./1024); %MB
      mvGrouped.osNumberOfSectorWrites = horzcat(mvGrouped.osNumberOfSectorWrites,monitor_grouped(:,header.columns.dsk_writ)./1024./1024); %MB
  end
  mvGrouped.osAllocatedFileHandlers = horzcat(mvGrouped.osAllocatedFileHandlers, monitor_grouped(:,header.columns.filesystem_files));
  mvGrouped.osAllocatedINodes = horzcat(mvGrouped.osAllocatedINodes, monitor_grouped(:,header.columns.filesystem_inodes));
  mvGrouped.osCountOfInterruptsServicedSinceBootTime = horzcat(mvGrouped.osCountOfInterruptsServicedSinceBootTime, monitor_grouped(:,header.columns.int));

  %% Ignore interrupts for now.
  %fn = fieldnames(header.metadata.interrupts);
  %mvGrouped.osNumberOfInterrupt = length(fn);
  %for i=1:length(fn)
      %eval(['mvGrouped.osInterruptCount' num2str(i) '= monitor_grouped(:,header.metadata.interrupts.' fn{i} ');']);
  %end

  if exist('extra') == 1
      mvGrouped.osNumberOfReadsIssued = horzcat(mvGrouped.osNumberOfReadsIssued, sum(monitor_grouped(:,extra.io),2)./1024./1024); %MB
      mvGrouped.osNumberOfWritesCompleted = horzcat(mvGrouped.osNumberOfWritesCompleted, sum(monitor_grouped(:,extra.io+1),2)./1024./1024); %MB
  else
      mvGrouped.osNumberOfReadsIssued = horzcat(mvGrouped.osNumberOfReadsIssued, monitor_grouped(:,header.columns.io_read)./1024./1024); %MB
      mvGrouped.osNumberOfWritesCompleted = horzcat(mvGrouped.osNumberOfWritesCompleted, monitor_grouped(:,header.columns.io_writ)./1024./1024); %MB
  end
  mvGrouped.osNumberOfSwapInSinceLastBoot = horzcat(mvGrouped.osNumberOfSwapInSinceLastBoot, monitor_grouped(:,header.columns.paging_in));
  mvGrouped.osNumberOfSwapOutSinceLastBoot = horzcat(mvGrouped.osNumberOfSwapOutSinceLastBoot, monitor_grouped(:,header.columns.paging_out));
  mvGrouped.osNumberOfProcessesCreated = horzcat(mvGrouped.osNumberOfProcessesCreated, monitor_grouped(:,header.columns.procs_new));
  mvGrouped.osNumberOfProcessesCurrentlyRunning = horzcat(mvGrouped.osNumberOfProcessesCurrentlyRunning, monitor_grouped(:,header.columns.procs_run));
  if exist('extra') == 1
      mvGrouped.osDiskUtilization = horzcat(mvGrouped.osDiskUtilization, sum(monitor_grouped(:,extra.util),2));
  else
      mvGrouped.osDiskUtilization = horzcat(mvGrouped.osDiskUtilization, monitor_grouped(:,header.columns.util));
  end

  mvGrouped.osFreeSwapSpace = horzcat(mvGrouped.osFreeSwapSpace, monitor_grouped(:,header.columns.swap_free));
  mvGrouped.osUsedSwapSpace = horzcat(mvGrouped.osUsedSwapSpace, monitor_grouped(:,header.columns.swap_used));
  mvGrouped.osNumberOfAllocatedPage = horzcat(mvGrouped.osNumberOfAllocatedPage, monitor_grouped(:,header.columns.virtual_alloc));
  mvGrouped.osNumberOfFreePages = horzcat(mvGrouped.osNumberOfFreePages, monitor_grouped(:,header.columns.virtual_free));
  mvGrouped.osNumberOfMajorPageFaults = horzcat(mvGrouped.osNumberOfMajorPageFaults, monitor_grouped(:,header.columns.virtual_majpf));
  mvGrouped.osNumberOfMinorPageFaults = horzcat(mvGrouped.osNumberOfMinorPageFaults, monitor_grouped(:,header.columns.virtual_minpf));
  mvGrouped.osNetworkSendKB = horzcat(mvGrouped.osNetworkSendKB, sum(monitor_grouped(:,header.metadata.net_send),2) ./1024);
  mvGrouped.osNetworkRecvKB = horzcat(mvGrouped.osNetworkRecvKB, sum(monitor_grouped(:,header.metadata.net_recv),2) ./1024);

  mvGrouped.clientTotalSubmittedTrans = horzcat(mvGrouped.clientTotalSubmittedTrans, IndividualCount_grouped);
  mvGrouped.clientTotalSubmittedTrans = sum(mvGrouped.clientTotalSubmittedTrans, 2);
  mvGrouped.clientIndividualSubmittedTrans= horzcat(mvGrouped.clientIndividualSubmittedTrans, IndividualCount_grouped);

  %Init
  if strcmpi(header.dbms, 'mysql')

    if isfield(header.columns, 'mysql_cpu')
      mvGrouped.dbmsMeasuredCPU = monitor_grouped(:, header.columns.mysql_cpu);
    end

    mvGrouped.dbmsChangedRows = horzcat(mvGrouped.dbmsChangedRows, sum(dM_grouped(:,[header.columns.Innodb_rows_deleted header.columns.Innodb_rows_updated header.columns.Innodb_rows_inserted]),2));
    mvGrouped.dbmsCumChangedRows = horzcat(mvGrouped.dbmsCumChangedRows, cumsum(mvGrouped.dbmsChangedRows));
    mvGrouped.dbmsCumFlushedPages = horzcat(mvGrouped.dbmsCumFlushedPages, monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_flushed));
    mvGrouped.dbmsFlushedPages = horzcat(mvGrouped.dbmsFlushedPages, DoSmooth(dM_grouped(:, header.columns.Innodb_buffer_pool_pages_flushed), 10));
    mvGrouped.dbmsCurrentDirtyPages = horzcat(mvGrouped.dbmsCurrentDirtyPages, monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_dirty));
    mvGrouped.dbmsDirtyPages = horzcat(mvGrouped.dbmsDirtyPages, dM_grouped(:, header.columns.Innodb_buffer_pool_pages_dirty));
    mvGrouped.dbmsDataPages = horzcat(mvGrouped.dbmsDataPages, monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_data));
    mvGrouped.dbmsFreePages = horzcat(mvGrouped.dbmsFreePages, monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_free));
    mvGrouped.dbmsTotalPages = horzcat(mvGrouped.dbmsTotalPages, monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_total));
    mvGrouped.dbmsThreadsRunning = horzcat(mvGrouped.dbmsThreadsRunning, monitor_grouped(:, header.columns.Threads_running));
    mvGrouped.dbmsTotalWritesMB = horzcat(mvGrouped.dbmsTotalWritesMB, dM_grouped(:,header.columns.Innodb_data_written)./1024./1024); %MB
    mvGrouped.dbmsLogWritesMB = horzcat(mvGrouped.dbmsLogWritesMB, dM_grouped(:,header.columns.Innodb_os_log_written)./1024./1024); %MB
    mvGrouped.dbmsNumberOfPhysicalLogWrites = horzcat(mvGrouped.dbmsNumberOfPhysicalLogWrites, dM_grouped(:,header.columns.Innodb_log_writes));
    mvGrouped.dbmsNumberOfDataReads = horzcat(mvGrouped.dbmsNumberOfDataReads, dM_grouped(:,header.columns.Innodb_data_reads));
    mvGrouped.dbmsNumberOfDataWrites = horzcat(mvGrouped.dbmsNumberOfDataWrites, dM_grouped(:,header.columns.Innodb_data_writes));

    mvGrouped.dbmsNumberOfLogWriteRequests = horzcat(mvGrouped.dbmsNumberOfLogWriteRequests, dM_grouped(:,header.columns.Innodb_log_write_requests));
    mvGrouped.dbmsNumberOfFysncLogWrites = horzcat(mvGrouped.dbmsNumberOfFysncLogWrites, dM_grouped(:,header.columns.Innodb_os_log_fsyncs));
    mvGrouped.dbmsNumberOfPendingLogWrites = horzcat(mvGrouped.dbmsNumberOfPendingLogWrites, dM_grouped(:,header.columns.Innodb_os_log_pending_writes));
    mvGrouped.dbmsNumberOfPendingLogFsyncs = horzcat(mvGrouped.dbmsNumberOfPendingLogFsyncs, dM_grouped(:,header.columns.Innodb_os_log_pending_fsyncs));

    mvGrouped.dbmsNumberOfNextRowReadRequests = horzcat(mvGrouped.dbmsNumberOfNextRowReadRequests, dM_grouped(:,header.columns.Handler_read_rnd_next));
    mvGrouped.dbmsNumberOfRowInsertRequests = horzcat(mvGrouped.dbmsNumberOfRowInsertRequests, dM_grouped(:,header.columns.Handler_write));
    mvGrouped.dbmsNumberOfFirstEntryReadRequests = horzcat(mvGrouped.dbmsNumberOfFirstEntryReadRequests, dM_grouped(:,header.columns.Handler_read_first));
    mvGrouped.dbmsNumberOfKeyBasedReadRequests = horzcat(mvGrouped.dbmsNumberOfKeyBasedReadRequests, dM_grouped(:,header.columns.Handler_read_key));
    mvGrouped.dbmsNumberOfNextKeyBasedReadRequests = horzcat(mvGrouped.dbmsNumberOfNextKeyBasedReadRequests, dM_grouped(:,header.columns.Handler_read_next));
    mvGrouped.dbmsNumberOfPrevKeyBasedReadRequests = horzcat(mvGrouped.dbmsNumberOfPrevKeyBasedReadRequests, dM_grouped(:,header.columns.Handler_read_prev));
    mvGrouped.dbmsNumberOfRowReadRequests = horzcat(mvGrouped.dbmsNumberOfRowReadRequests, dM_grouped(:,header.columns.Handler_read_rnd));

    mvGrouped.dbmsPageWritesMB = horzcat(mvGrouped.dbmsPageWritesMB, dM_grouped(:,header.columns.Innodb_pages_written).*2.*16./1024); % to account for double write buffering
    mvGrouped.dbmsDoublePageWritesMB = horzcat(mvGrouped.dbmsDoublePageWritesMB, dM_grouped(:,header.columns.Innodb_dblwr_pages_written).*2.*16./1024); % to account for double write buffering
    mvGrouped.dbmsDoubleWritesOperations = horzcat(mvGrouped.dbmsDoubleWritesOperations, dM_grouped(:,header.columns.Innodb_dblwr_writes));

    mvGrouped.dbmsNumberOfPendingWrites = horzcat(mvGrouped.dbmsNumberOfPendingWrites, dM_grouped(:,header.columns.Innodb_data_pending_writes));
    mvGrouped.dbmsNumberOfPendingReads = horzcat(mvGrouped.dbmsNumberOfPendingReads, dM_grouped(:,header.columns.Innodb_data_pending_reads));

    mvGrouped.dbmsBufferPoolWrites = horzcat(mvGrouped.dbmsBufferPoolWrites, dM_grouped(:,header.columns.Innodb_buffer_pool_write_requests));
    mvGrouped.dbmsRandomReadAheads = horzcat(mvGrouped.dbmsRandomReadAheads, dM_grouped(:,header.columns.Innodb_buffer_pool_read_ahead_rnd));
    mvGrouped.dbmsSequentialReadAheads = horzcat(mvGrouped.dbmsSequentialReadAheads, dM_grouped(:,header.columns.Innodb_buffer_pool_read_ahead_seq));
    mvGrouped.dbmsNumberOfLogicalReadRequests = horzcat(mvGrouped.dbmsNumberOfLogicalReadRequests, dM_grouped(:,header.columns.Innodb_buffer_pool_read_requests));
    mvGrouped.dbmsNumberOfLogicalReadsFromDisk = horzcat(mvGrouped.dbmsNumberOfLogicalReadsFromDisk, dM_grouped(:,header.columns.Innodb_buffer_pool_reads));
    mvGrouped.dbmsNumberOfWaitsForFlush = horzcat(mvGrouped.dbmsNumberOfWaitsForFlush, dM_grouped(:,header.columns.Innodb_buffer_pool_wait_free));

    % stats on how many and what type of SQL statements were run
    mvGrouped.dbmsCommittedCommands = horzcat(mvGrouped.dbmsCommittedCommands, dM_grouped(:,header.columns.Com_commit));
    mvGrouped.dbmsRolledbackCommands = horzcat(mvGrouped.dbmsRolledbackCommands, dM_grouped(:,header.columns.Com_rollback));
    mvGrouped.dbmsRollbackHandler = horzcat(mvGrouped.dbmsRollbackHandler, dM_grouped(:,header.columns.Handler_rollback));

    lock_smoothing = 1;
    mvGrouped.dbmsCurrentLockWaits = horzcat(mvGrouped.dbmsCurrentLockWaits, DoSmooth(monitor_grouped(:,header.columns.Innodb_row_lock_current_waits), lock_smoothing));
    mvGrouped.dbmsLockWaits = horzcat(mvGrouped.dbmsLockWaits, DoSmooth(dM_grouped(:,header.columns.Innodb_row_lock_waits), lock_smoothing));
    mvGrouped.dbmsLockWaitTime = horzcat(mvGrouped.dbmsLockWaitTime, DoSmooth(dM_grouped(:,header.columns.Innodb_row_lock_time), lock_smoothing) / 1000); % to turn it into seconds!

    mvGrouped.dbmsReadRequests = horzcat(mvGrouped.dbmsReadRequests, dM_grouped(:, header.columns.Innodb_buffer_pool_read_requests));
    mvGrouped.dbmsReads = horzcat(mvGrouped.dbmsReads, dM_grouped(:, header.columns.Innodb_buffer_pool_reads));
    mvGrouped.dbmsPhysicalReadsMB = horzcat(mvGrouped.dbmsPhysicalReadsMB, dM_grouped(:,[header.columns.Innodb_data_read])./1024./1024);

    if isfield(header.columns, 'Innodb_page_size')
      mvGrouped.dbmsPageSize = horzcat(mvGrouped.dbmsPageSize, max(monitor_grouped(:, header.columns.Innodb_page_size)));
    end
    if isfield(header.columns, 'Innodb_buffer_pool_size')
      mvGrouped.dbmsBufferPoolSize = horzcat(mvGrouped.dbmsBufferPoolSize, max(monitor_grouped(:, header.columns.Innodb_buffer_pool_size)));
    end
    if isfield(header.columns, 'Innodb_log_file_size')
      mvGrouped.dbmsLogFileSize = horzcat(mvGrouped.dbmsLogFileSize, max(monitor_grouped(:, header.columns.Innodb_log_file_size)));
    end

  elseif strcmpi(header.dbms, 'psql')
      mvGrouped.dbmsFlushedPages = horzcat(mvGrouped.dbmsFlushedPages, DoSmooth(dM_grouped(:, header.columns.buffers_clean)+dM_grouped(:, header.columns.buffers_backend), 10));
      %mvGrouped.dbmsFlushedPages = dM_grouped(:, header.columns.buffers_clean)+dM_grouped(:, header.columns.buffers_backend);
      mvGrouped.dbmsLogWritesMB =horzcat(mvGrouped.dbmsLogWritesMB, dM_grouped(:,header.columns.buffers_checkpoint).*8192./1024./1024); %MB
      mvGrouped.dbmsPageWritesMB = horzcat(mvGrouped.dbmsPageWritesMB, (dM_grouped(:,header.columns.buffers_clean)+dM_grouped(:,header.columns.buffers_backend)).*2.*8./1024); % to account for double write buffering
      mvGrouped.dbmsCommittedCommands = horzcat(mvGrouped.dbmsCommittedCommands, dM_grouped(:,header.columns.xact_commit));
      mvGrouped.dbmsRolledbackCommands = horzcat(mvGrouped.dbmsRolledbackCommands, dM_grouped(:,header.columns.xact_rollback));
      mvGrouped.measuredCPU = horzcat(mvGrouped.measuredCPU, monitor_grouped(:,header.columns.postgres_cpu) + monitor_grouped(:,header.columns.postgres_children_cpu));
      mvGrouped.measuredWritesMB = horzcat(mvGrouped.measuredWritesMB, monitor_grouped(:,header.columns.postgres_bytes_written) / 1024 / 1024);
      mvGrouped.measuredReadsMB = horzcat(mvGrouped.measuredReadsMB, monitor_grouped(:,header.columns.postgres_bytes_read) / 1024 / 1024);
      mvGrouped.dbmsReadRequests = horzcat(mvGrouped.dbmsReadRequests, dM_grouped(:, header.columns.blks_read) + dM_grouped(:, header.columns.blks_hit));
      mvGrouped.dbmsReads = horzcat(mvGrouped.dbmsReads, dM_grouped(:, header.columns.blks_read));
  else
      error(['Sorry, we currently do not support the ' header.dbms ' DBMS']);
  end

end % END for

%%%%%%%%%%%%%%%%%%%%%%%%

%%% For grouped (OLD)
%mvGrouped.cpu_usr = monitor_grouped(:,header.metadata.cpu_usr);     % here we ignore hyperthreading, i.e. we take all CPUs from dstat data.
%mvGrouped.cpu_sys = monitor_grouped(:,header.metadata.cpu_sys);
%mvGrouped.cpu_idl = monitor_grouped(:,header.metadata.cpu_idl);
%mvGrouped.numOfTransType = size(IndividualCounts_grouped,2);
%mvGrouped.clientTransLatency = avglat_grouped;
%mvGrouped.numberOfObservations = size(monitor_grouped, 1);

%mvGrouped.CoreVariance = var(mvGrouped.cpu_usr,0,2);
%[mvGrouped.AvgCpuUser mvGrouped.AvgCpuSys mvGrouped.AvgCpuIdle mvGrouped.AvgCpuWai mvGrouped.AvgCpuHiq mvGrouped.AvgCpuSiq] = CpuAggregate2(monitor_grouped(:,:), header);


%mvGrouped.osAsynchronousIO = monitor_grouped(:,header.columns.aio);
%mvGrouped.osNumberOfContextSwitches = monitor_grouped(:,header.columns.csw);

%if exist('extra') == 1
    %mvGrouped.osNumberOfSectorReads = sum(monitor_grouped(:,extra.disk),2)./1024./1024; %MB
    %mvGrouped.osNumberOfSectorWrites = sum(monitor_grouped(:,header.extra.disk+1),2)./1024./1024; %MB
%else
    %mvGrouped.osNumberOfSectorReads = monitor_grouped(:,header.columns.dsk_read)./1024./1024; %MB
    %mvGrouped.osNumberOfSectorWrites = monitor_grouped(:,header.columns.dsk_writ)./1024./1024; %MB
%end

%mvGrouped.osAllocatedFileHandlers = monitor_grouped(:,header.columns.filesystem_files);
%mvGrouped.osAllocatedINodes = monitor_grouped(:,header.columns.filesystem_inodes);
%mvGrouped.osCountOfInterruptsServicedSinceBootTime = monitor_grouped(:,header.columns.int);
%fn = fieldnames(header.metadata.interrupts);
%mvGrouped.osNumberOfInterrupt = length(fn);
%for i=1:length(fn)
    %eval(['mvGrouped.osInterruptCount' num2str(i) '= monitor_grouped(:,header.metadata.interrupts.' fn{i} ');']);
%end

%if exist('extra') == 1
    %mvGrouped.osNumberOfReadsIssued = sum(monitor_grouped(:,extra.io),2)./1024./1024; %MB
    %mvGrouped.osNumberOfWritesCompleted = sum(monitor_grouped(:,extra.io+1),2)./1024./1024; %MB
%else
    %mvGrouped.osNumberOfReadsIssued = monitor_grouped(:,header.columns.io_read)./1024./1024; %MB
    %mvGrouped.osNumberOfWritesCompleted = monitor_grouped(:,header.columns.io_writ)./1024./1024; %MB
%end

%mvGrouped.osNumberOfSwapInSinceLastBoot = monitor_grouped(:,header.columns.paging_in);
%mvGrouped.osNumberOfSwapOutSinceLastBoot = monitor_grouped(:,header.columns.paging_out);
%mvGrouped.osNumberOfProcessesCreated = monitor_grouped(:,header.columns.procs_new);
%mvGrouped.osNumberOfProcessesCurrentlyRunning = monitor_grouped(:,header.columns.procs_run);

%if exist('extra') == 1
    %mvGrouped.osDiskUtilization = sum(monitor_grouped(:,extra.util),2);
%else
    %mvGrouped.osDiskUtilization = monitor_grouped(:,header.columns.util);
%end
%mvGrouped.osFreeSwapSpace = monitor_grouped(:,header.columns.swap_free);
%mvGrouped.osUsedSwapSpace = monitor_grouped(:,header.columns.swap_used);
%mvGrouped.osNumberOfAllocatedPage = monitor_grouped(:,header.columns.virtual_alloc);
%mvGrouped.osNumberOfFreePages = monitor_grouped(:,header.columns.virtual_free);
%mvGrouped.osNumberOfMajorPageFaults = monitor_grouped(:,header.columns.virtual_majpf);
%mvGrouped.osNumberOfMinorPageFaults = monitor_grouped(:,header.columns.virtual_minpf);
%mvGrouped.osNetworkSendKB=monitor_grouped(:,header.metadata.net_send) ./1024;
%mvGrouped.osNetworkRecvKB=monitor_grouped(:,header.metadata.net_recv)./1024;

%mvGrouped.clientTotalSubmittedTrans=sum(IndividualCounts_grouped(:,:), 2);
%mvGrouped.clientIndividualSubmittedTrans=IndividualCounts_grouped;

%%Init
%if strcmpi(header.dbms, 'mysql')
    %mvGrouped.dbmsChangedRows = sum(dM_grouped(:,[header.columns.Innodb_rows_deleted header.columns.Innodb_rows_updated header.columns.Innodb_rows_inserted]),2);
    %mvGrouped.dbmsCumChangedRows=cumsum(mvGrouped.dbmsChangedRows);
    %mvGrouped.dbmsCumFlushedPages = monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_flushed);
    %mvGrouped.dbmsFlushedPages = DoSmooth(dM_grouped(:, header.columns.Innodb_buffer_pool_pages_flushed), 10);
    %%mvGrouped.dbmsFlushedPages = dM_grouped(:, header.columns.Innodb_buffer_pool_pages_flushed);
    %mvGrouped.dbmsCurrentDirtyPages = monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_dirty);
    %mvGrouped.dbmsDirtyPages = dM_grouped(:, header.columns.Innodb_buffer_pool_pages_dirty);
    %mvGrouped.dbmsDataPages = monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_data);
    %mvGrouped.dbmsFreePages = monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_free);
    %mvGrouped.dbmsTotalPages = monitor_grouped(:, header.columns.Innodb_buffer_pool_pages_total); 
    %mvGrouped.dbmsThreadsRunning = monitor_grouped(:, header.columns.Threads_running); 
    %mvGrouped.dbmsTotalWritesMB=dM_grouped(:,header.columns.Innodb_data_written)./1024./1024; %MB
    %mvGrouped.dbmsLogWritesMB=dM_grouped(:,header.columns.Innodb_os_log_written)./1024./1024; %MB
    %mvGrouped.dbmsNumberOfPhysicalLogWrites=dM_grouped(:,header.columns.Innodb_log_writes);
    %mvGrouped.dbmsNumberOfDataReads=dM_grouped(:,header.columns.Innodb_data_reads);
    %mvGrouped.dbmsNumberOfDataWrites=dM_grouped(:,header.columns.Innodb_data_writes);
    
    %mvGrouped.dbmsNumberOfLogWriteRequests=dM_grouped(:,header.columns.Innodb_log_write_requests);
    %mvGrouped.dbmsNumberOfFysncLogWrites=dM_grouped(:,header.columns.Innodb_os_log_fsyncs);
    %mvGrouped.dbmsNumberOfPendingLogWrites=dM_grouped(:,header.columns.Innodb_os_log_pending_writes);
    %mvGrouped.dbmsNumberOfPendingLogFsyncs=dM_grouped(:,header.columns.Innodb_os_log_pending_fsyncs);

    %mvGrouped.dbmsNumberOfNextRowReadRequests=dM_grouped(:,header.columns.Handler_read_rnd_next);
    %mvGrouped.dbmsNumberOfRowInsertRequests=dM_grouped(:,header.columns.Handler_write);
    %mvGrouped.dbmsNumberOfFirstEntryReadRequests=dM_grouped(:,header.columns.Handler_read_first);
    %mvGrouped.dbmsNumberOfKeyBasedReadRequests=dM_grouped(:,header.columns.Handler_read_key);
    %mvGrouped.dbmsNumberOfNextKeyBasedReadRequests=dM_grouped(:,header.columns.Handler_read_next);
    %mvGrouped.dbmsNumberOfPrevKeyBasedReadRequests=dM_grouped(:,header.columns.Handler_read_prev);
    %mvGrouped.dbmsNumberOfRowReadRequests=dM_grouped(:,header.columns.Handler_read_rnd);
    
    %mvGrouped.dbmsPageWritesMB=dM_grouped(:,header.columns.Innodb_pages_written).*2.*16./1024; % to account for double write buffering
    %mvGrouped.dbmsDoublePageWritesMB=dM_grouped(:,header.columns.Innodb_dblwr_pages_written).*2.*16./1024; % to account for double write buffering
    %mvGrouped.dbmsDoubleWritesOperations=dM_grouped(:,header.columns.Innodb_dblwr_writes);

    %mvGrouped.dbmsNumberOfPendingWrites=dM_grouped(:,header.columns.Innodb_data_pending_writes);
    %mvGrouped.dbmsNumberOfPendingReads=dM_grouped(:,header.columns.Innodb_data_pending_reads);
    
    %mvGrouped.dbmsBufferPoolWrites = dM_grouped(:,header.columns.Innodb_buffer_pool_write_requests);
    %mvGrouped.dbmsRandomReadAheads = dM_grouped(:,header.columns.Innodb_buffer_pool_read_ahead_rnd);
    %mvGrouped.dbmsSequentialReadAheads = dM_grouped(:,header.columns.Innodb_buffer_pool_read_ahead_seq);
    %mvGrouped.dbmsNumberOfLogicalReadRequests = dM_grouped(:,header.columns.Innodb_buffer_pool_read_requests);
    %mvGrouped.dbmsNumberOfLogicalReadsFromDisk = dM_grouped(:,header.columns.Innodb_buffer_pool_reads);
    %mvGrouped.dbmsNumberOfWaitsForFlush = dM_grouped(:,header.columns.Innodb_buffer_pool_wait_free);

    %% stats on how many and what type of SQL statements were run
    %mvGrouped.dbmsCommittedCommands=dM_grouped(:,header.columns.Com_commit);
    %mvGrouped.dbmsRolledbackCommands=dM_grouped(:,header.columns.Com_rollback);
    %mvGrouped.dbmsRollbackHandler=dM_grouped(:,header.columns.Handler_rollback);
    
    %% latency stats
    %% mvGrouped.measuredCPU=monitor_grouped(:,header.columns.mysqld_cpu)+monitor_grouped(:,header.columns.mysqld_children_cpu);
    %% mvGrouped.measuredWritesMB=monitor_grouped(:,header.columns.mysqld_bytes_written) / 1024 / 1024;
    %% mvGrouped.measuredReadsMB=monitor_grouped(:,header.columns.mysqld_bytes_read) / 1024 / 1024;

    %lock_smoothing = 1;
    %mvGrouped.dbmsCurrentLockWaits=DoSmooth(monitor_grouped(:,header.columns.Innodb_row_lock_current_waits), lock_smoothing);
    %mvGrouped.dbmsLockWaits=DoSmooth(dM_grouped(:,header.columns.Innodb_row_lock_waits), lock_smoothing);
    %mvGrouped.dbmsLockWaitTime=DoSmooth(dM_grouped(:,header.columns.Innodb_row_lock_time), lock_smoothing) / 1000; % to turn it into seconds!
    %%mvGrouped.dbmsCurrentLockWaits=monitor_grouped(:,header.columns.Innodb_row_lock_current_waits);
    %%mvGrouped.dbmsLockWaits=dM_grouped(:,header.columns.Innodb_row_lock_waits);
    %%mvGrouped.dbmsLockWaitTime=dM_grouped(:,header.columns.Innodb_row_lock_time) / 1000; % to turn it into seconds!
    %% if exist('OCTAVE_VERSION')
    %%     save('dbmsCurrentLockWaits_Octave.mat', '-v6', '-struct', 'mvGrouped', 'dbmsCurrentLockWaits');
    %%     save('dbmsLockWaits_Octave.mat', '-v6', '-struct', 'mvGrouped', 'dbmsLockWaits');
    %%     save('dbmsLockWaitTime_Octave.mat', '-v6', '-struct', 'mvGrouped', 'dbmsLockWaitTime');
    %% else
    %%     save('dbmsCurrentLockWaits_Matlab', '-v6', '-struct', 'mvGrouped', 'dbmsCurrentLockWaits');
    %%     save('dbmsLockWaits_Matlab', '-v6', '-struct', 'mvGrouped', 'dbmsLockWaits');
    %%     save('dbmsLockWaitTime_Matlab', '-v6', '-struct', 'mvGrouped', 'dbmsLockWaitTime');
    %% end

    %mvGrouped.dbmsReadRequests = dM_grouped(:, header.columns.Innodb_buffer_pool_read_requests);
    %mvGrouped.dbmsReads = dM_grouped(:, header.columns.Innodb_buffer_pool_reads);
    %mvGrouped.dbmsPhysicalReadsMB = dM_grouped(:,[header.columns.Innodb_data_read])./1024./1024;

%elseif strcmpi(header.dbms, 'psql')
    %mvGrouped.dbmsFlushedPages = DoSmooth(dM_grouped(:, header.columns.buffers_clean)+dM_grouped(:, header.columns.buffers_backend), 10);
    %%mvGrouped.dbmsFlushedPages = dM_grouped(:, header.columns.buffers_clean)+dM_grouped(:, header.columns.buffers_backend);
    %mvGrouped.dbmsLogWritesMB=dM_grouped(:,header.columns.buffers_checkpoint).*8192./1024./1024; %MB
    %mvGrouped.dbmsPageWritesMB=(dM_grouped(:,header.columns.buffers_clean)+dM_grouped(:,header.columns.buffers_backend)).*2.*8./1024; % to account for double write buffering
    %mvGrouped.dbmsCommittedCommands=dM_grouped(:,header.columns.xact_commit);
    %mvGrouped.dbmsRolledbackCommands=dM_grouped(:,header.columns.xact_rollback);
    %mvGrouped.measuredCPU=monitor_grouped(:,header.columns.postgres_cpu) + monitor_grouped(:,header.columns.postgres_children_cpu);
    %mvGrouped.measuredWritesMB=monitor_grouped(:,header.columns.postgres_bytes_written) / 1024 / 1024;
    %mvGrouped.measuredReadsMB=monitor_grouped(:,header.columns.postgres_bytes_read) / 1024 / 1024;
    %mvGrouped.dbmsReadRequests = dM_grouped(:, header.columns.blks_read) + dM_grouped(:, header.columns.blks_hit);
    %mvGrouped.dbmsReads = dM_grouped(:, header.columns.blks_read);
%else
    %error(['Sorry, we currently do not support the ' header.dbms ' DBMS']);
%end

elapsed = toc(overallTime);
fprintf(1,'load_modeling_variables time = %f\n', elapsed);

end

