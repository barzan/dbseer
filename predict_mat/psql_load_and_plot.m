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


function load_and_plot(signature, nextPlot, dim1, dim2)

overallTime = tic;

header_aligned_postgres

if MYSQL_ENV && Uptime_since_flush_status~=434
    error('The header is messed up!')
end

%monitor = csvread(horzcat('monitor-',signature),2);
%monitor = [monitor zeros(size(monitor,1),11)];

%avglat = load(horzcat('trans-',signature,'_avg_latency.al'));
prclat = load(horzcat('trans-',signature,'_prctile_latencies.mat'));
%locktime = load(horzcat(transactionprefix,'percona_transactions.csv_locktimes.al'));
%IndividualCounts = load(horzcat('trans-',signature,'_rough_trans_count.al'));
[monitor avglat IndividualCounts dM] = load3('./', signature, 1);


cpu_usr_indexes = [cpu1_usr cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr cpu9_usr cpu10_usr cpu11_usr cpu12_usr cpu13_usr cpu14_usr cpu15_usr cpu16_usr];
cpu_usr_indexes1 = [cpu1_usr cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr];
cpu_usr_indexes2 = [cpu9_usr cpu10_usr cpu11_usr cpu12_usr cpu13_usr cpu14_usr cpu15_usr cpu16_usr];
cpu_sys_indexes = [cpu1_sys cpu2_sys cpu3_sys cpu4_sys cpu5_sys cpu6_sys cpu7_sys cpu8_sys cpu9_sys cpu10_sys cpu11_sys cpu12_sys cpu13_sys cpu14_sys cpu15_sys cpu16_sys]; 
cpu_sys_indexes1 = [cpu1_sys cpu2_sys cpu3_sys cpu4_sys cpu5_sys cpu6_sys cpu7_sys cpu8_sys]; 
cpu_sys_indexes2 = [cpu9_sys cpu10_sys cpu11_sys cpu12_sys cpu13_sys cpu14_sys cpu15_sys cpu16_sys]; 

tstart= 10; %t1=1300 t12345=1300;
tend= size(monitor,1)- 10; %t1=2800 t12345=1650
tranA=1;
tranB=2;
transTypeRange=1:5;

startSmooth=1; % the offset of where the smooth data begins, FROM tstart
endSmooth=tend-tstart+1 -1; % the index of where the smooth data ends!

%if monotoneTPS ~= 0
%    Xdata = sum(IndividualCounts(tstart:tend,:)')';
%    xlab = 'TPS';
%else
    Xdata = tstart:1:tend;
    Xdata = Xdata' - tstart;
    xlab = 'Time (sec)';
%end

if nargin < 4
    dim1=3;
    dim2=1;
end

if nargin < 2
    screen_size = get(0, 'ScreenSize');
    fh = figure('Name',signature,'Color',[1 1 1]);
    %set(fh, 'Position', [0 0 screen_size(3) screen_size(4)]);
    nextPlot=1;
end

fontsize = 14; % 14 normal, 40 paper;
linewidth=1; % 1 normal, 6.5 paper;
format('long');


if 1==0
    IndividualCoreUsageUser=1;
    IndividualCoreUsageSys=0;
    InterCoreVariance=0;
    AvgCpuUsage=1;
    TPSCommitRollback=1;
    ContextSwitches=0;
    DiskWriteMB=0;
    DiskWriteMB_friendly=1;
    DiskWriteNum=0;
    DiskWriteNum_friendly=0;
    FlushRate=1;
    DiskReadMB=1;
    DiskReadNum=1;
    RowsChangedOverTime=0;
    RowsChangedPerWriteMB=0;
    RowsChangedPerWriteNo=0;
    LockAnalysis=1;
    LatencyA=0;
    LatencyB=0;
    LatencyOverall=0;
    Network=0;
    CacheHit=0;
else
    IndividualCoreUsageUser=0;
    IndividualCoreUsageSys=0;
    InterCoreVariance=0;
    AvgCpuUsage=0;
    TPSCommitRollback=1;
    ContextSwitches=0;
    DiskWriteMB=0;
    DiskWriteMB_friendly=0;
    DiskWriteNum=0;
    DiskWriteNum_friendly=0;
    FlushRate=0;
    DiskReadMB=0;
    DiskReadNum=0;
    RowsChangedOverTime=0;
    RowsChangedPerWriteMB=0;
    RowsChangedPerWriteNo=0;
    LockAnalysis=1;
    LatencyA=0;
    LatencyB=0;
    LatencyOverall=0;
    Network=1;
    CacheHit=0;
end

BarzanPrediction=0;

StrangeFeatures1=0;
StrangeFeatures2=0;
AllStrangeFeatures=0;
Interrupts=0;
DirtyPagesPrediction=0; %tested this! It's solid!
FlushRatePrediction=0; %tested this! It's solid! Nov 9th, 2011
LatencyPrediction=0;
LockConcurrencyPrediction=0;
DirtyPagesOverTime=0;
PagingInOut=0;
CombinedAvgLatency=0;
LatencyVersusCPU=0;
Latency3D=0;
workingSetSize=0;
workingSetSize2=0;
LatencyPerTPS=0;
LatencyPerLocktime=0;

%Init
if MYSQL_ENV

	rowsChanged = sum(diff(monitor(tstart:tend,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]))')';
	cumRowsChanged=cumsum(rowsChanged);
	cumPagesFlushed = monitor(tstart+1:tend, Innodb_buffer_pool_pages_flushed);
	pagesFlushed = smooth(diff(monitor(tstart:tend, Innodb_buffer_pool_pages_flushed)), 10);
	currentPagesDirty = monitor(tstart+1:tend, Innodb_buffer_pool_pages_dirty);
	pagesDirtied = diff(monitor(tstart:tend, Innodb_buffer_pool_pages_dirty));
	pagesWithData = monitor(tstart+1:tend, Innodb_buffer_pool_pages_data);
	pagesFree = monitor(tstart+1:tend, Innodb_buffer_pool_pages_free);
	pagesTotal = monitor(tstart+1:tend, Innodb_buffer_pool_pages_total); 

	mysqlTotalIOw=diff(monitor(tstart:tend,Innodb_data_written))./1024./1024; %MB
	mysqlLogIOw=diff(monitor(tstart:tend,Innodb_os_log_written))./1024./1024; %MB
	mysqlPagesWrittenMB=diff(monitor(tstart:tend,Innodb_pages_written)).*2.*16./1024; % to account for double write buffering
	mysqlPagesDblWrittenMB=diff(monitor(tstart:tend,Innodb_dblwr_pages_written)).*2.*16./1024; % to account for double write buffering
	sysPhysicalIOw=monitor(tstart+1:tend,dsk_writ)./1024./1024; %MB


	ComCommit=diff(monitor(tstart:tend,Com_commit));
	ComRollback=diff(monitor(tstart:tend,Com_rollback));
	HandlerRollback=diff(monitor(tstart:tend,Handler_rollback));
	SubmittedTransTotal=sum(IndividualCounts(tstart+1:tend,:)')';
	SubmittedTransA=IndividualCounts(tstart+1:tend,tranA);
	SubmittedTransB=IndividualCounts(tstart+1:tend,tranB);
	SubmittedTransInd=IndividualCounts(tstart+1:tend,transTypeRange);

	AvgLatencyA = avglat(tstart+1:tend,tranA+1);
	AvgLatencyB = avglat(tstart+1:tend,tranB+1);

	[AvgCpuUser AvgCpuSys AvgCpuIdle AvgCpuWai AvgCpuHiq AvgCpuSiq] = CpuAggregate(monitor(tstart+1:tend,:));
	CoreVariance = var(monitor(tstart+1:tend,[cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr])')';
	dbmsCpu=monitor(tstart+1:tend,mysqld_cpu)+monitor(tstart+1:tend,mysqld_children_cpu);
	dbmsWrittenMB=monitor(tstart+1:tend,mysqld_bytes_written) / 1024 / 1024;
	dbmsReadMB=monitor(tstart+1:tend,mysqld_bytes_read) / 1024 / 1024;

	NetworkSendKB=(monitor(tstart+1:tend,net0_send)+monitor(tstart+1:tend,net1_send)) ./1024;
	NetworkRecvKB=(monitor(tstart+1:tend,net0_recv)+monitor(tstart+1:tend,net1_recv))./1024;

	lock_smoothing = 1;
	LocksBeingWaitedFor=smooth(monitor(tstart+1:tend,Innodb_row_lock_current_waits),lock_smoothing);
	NumOfWaitsDueToLocks=smooth(diff(monitor(tstart:tend,Innodb_row_lock_waits)),lock_smoothing);
	TimeSpentWaitingForLocks=smooth(diff(monitor(tstart:tend,Innodb_row_lock_time)),lock_smoothing) / 1000; % to turn it into seconds!


	KeyReadRequests = diff(monitor(tstart:tend, Innodb_buffer_pool_read_requests));
	PhysicalKeyReads = diff(monitor(tstart:tend, Innodb_buffer_pool_reads));
	PhysicalReadMB = diff(monitor(tstart:tend,[Innodb_data_read]))./1024./1024;

else % POSTGRES_ENV
	%rowsChanged = sum(diff(monitor(tstart:tend,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]))')';
	%cumRowsChanged=cumsum(rowsChanged);
	cumPagesFlushed = monitor(tstart+1:tend, buffers_clean) + monitor(tstart+1:tend, buffers_backend);
	pagesFlushed = DoSmooth(diff(cumPagesFlushed), 10);
	%currentPagesDirty = monitor(tstart+1:tend, Innodb_buffer_pool_pages_dirty);
	%pagesDirtied = diff(monitor(tstart:tend, Innodb_buffer_pool_pages_dirty));

	sysPhysicalIOw=monitor(tstart+1:tend,dsk_writ)./1024./1024; %MB

	ComCommit=diff(monitor(tstart:tend, xact_commit));
	ComRollback=diff(monitor(tstart:tend, xact_rollback));
	SubmittedTransTotal=sum(IndividualCounts(tstart+1:tend,:)')';
	SubmittedTransA=IndividualCounts(tstart+1:tend,tranA);
	SubmittedTransB=IndividualCounts(tstart+1:tend,tranB);
	SubmittedTransInd=IndividualCounts(tstart+1:tend,transTypeRange);

	AvgLatencyA = avglat(tstart+1:tend,tranA+1);
	AvgLatencyB = avglat(tstart+1:tend,tranB+1);

	[AvgCpuUser AvgCpuSys AvgCpuIdle AvgCpuWai AvgCpuHiq AvgCpuSiq] = CpuAggregate(monitor(tstart+1:tend,:));
	CoreVariance = var(monitor(tstart+1:tend,[cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr])')';
	dbmsCpu=monitor(tstart+1:tend,postgres_cpu)+monitor(tstart+1:tend, postgres_children_cpu);
	dbmsWrittenMB=monitor(tstart+1:tend,postgres_bytes_written) / 1024 / 1024;
	dbmsReadMB=monitor(tstart+1:tend,postgres_bytes_read) / 1024 / 1024;

	NetworkSendKB=monitor(tstart+1:tend,net0_send)./1024; %+monitor(tstart+1:tend,net1_send)) ./1024;
	NetworkRecvKB=monitor(tstart+1:tend,net0_recv)./1024; %+monitor(tstart+1:tend,net1_recv))./1024;

	lock_smoothing = 1;
	%LocksBeingWaitedFor=smooth(monitor(tstart+1:tend,Innodb_row_lock_current_waits),lock_smoothing);
    
	%NumOfWaitsDueToLocks=smooth(diff(monitor(tstart:tend,confl_lock)),lock_smoothing);
    NumOfWaitsDueToLocks=DoSmooth(diff(monitor(tstart:tend,confl_lock)),lock_smoothing);
    
	%TimeSpentWaitingForLocks=smooth(diff(monitor(tstart:tend,Innodb_row_lock_time)),lock_smoothing) / 1000; % to turn it into seconds!


	KeyReadRequests = diff(monitor(tstart:tend, blks_read));
	PhysicalKeyReads = diff(monitor(tstart:tend, blks_read)-monitor(tstart:tend, blks_hit));
	%PhysicalReadMB = diff(monitor(tstart:tend,[Innodb_data_read]))./1024./1024;

end
%Init end

if IndividualCoreUsageUser==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end,:), monitor(tstart+1:tend,cpu_usr_indexes1), ':');
    title('Individual core Usr usage');
    xlabel(xlab);
    %ylabel('Individual usr cpu usage per core (%)');
    ylabel('Individual core usr usage');
    legend('Core with mysql');
    grid on;
    nextPlot=nextPlot+1;
end

if IndividualCoreUsageSys==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end,:), monitor(tstart+1:tend,cpu_sys_indexes1), ':');
    title('Individual core Sys usage');
    xlabel(xlab);
    %ylabel('Individual usr cpu usage per core (%)');
    ylabel('Individual core sys usage');
    legend('Core with mysql');
    grid on;
    nextPlot=nextPlot+1;
end

if InterCoreVariance==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end,:), CoreVariance);
    title('Variance in core usage');
    xlabel(xlab);
    %ylabel('Individual usr cpu usage per core (%)');
    ylabel('Inter-core variance');
    grid on;
    nextPlot=nextPlot+1;
end

if AvgCpuUsage==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end,:), [AvgCpuUser AvgCpuSys AvgCpuWai AvgCpuHiq AvgCpuSiq], '-.');
    hold on;
    plot(Xdata(2:end,:), dbmsCpu, 'k-.');
    plot(Xdata(2:end,:), AvgCpuIdle, 'r-.');    
    
    title('Avg Cpu Usage');
    xlabel(xlab);
    ylabel('Avg Cpu Usage (%)');
    legend('Usr', 'Sys', 'AvgCpuWai', 'AvgCpuHiq', 'AvgCpuSiq', 'MySQL Usage', 'Idle');
    
    grid on;
    nextPlot=nextPlot+1;
end

if TPSCommitRollback==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    ph1 = plot(Xdata(2:end), SubmittedTransTotal,'kd');
    hold on;
    plot(Xdata(2:end), SubmittedTransA,'m*');
    plot(Xdata(2:end), SubmittedTransB,'go');
    plot(Xdata(2:end), [ComCommit ComRollback]);
    
    [xThroughput yThroughput] = findMaxThroughput(SubmittedTransTotal);
    if ~isempty(yThroughput) 
        ph2 = drawLine('h', 'b-', yThroughput);
        ph3 = drawLine('v', 'm-', xThroughput);
    end
    
    title(horzcat('Max throughput ', num2str(yThroughput), ' TPS at t=', num2str(xThroughput),' '));
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('Overal TPS', horzcat('# of Trx ', num2str(tranA)), horzcat('# of Trx ', num2str(tranB)), 'ComCommit','ComRollback', 'Location', 'SouthEast');
    grid on;
    nextPlot=nextPlot+1;
end

if ContextSwitches==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata, monitor(tstart:tend,[csw])./1500, Xdata, monitor(tstart:tend,[Threads_running]),'-');
    title('Threads');
    xlabel(xlab);
    ylabel('# of threads');
    legend('Context Switches (x1500)','Threads running');
    grid on;
    nextPlot=nextPlot+1;
end

if DiskWriteMB==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), [diff(monitor(tstart:tend,[Innodb_data_written Innodb_os_log_written]))./1024./1024 ...
        diff(monitor(tstart:tend,[Innodb_pages_written Innodb_dblwr_pages_written])).*16./1024],...
        Xdata, monitor(tstart:tend,[dsk_writ io_writ])./1024./1024,'-');
    hold on;
    %plot(,'-.');
    title('Write Volume (MB)');
    xlabel(xlab);
    ylabel('written data (MB/sec)');
    legend('InnodbDataWritten','Mysql log, i.e. InnodbOsLogWritten','InnodbPagesWritten (*16K)','half of Dirty pages, i.e. InnodbDblwrPagesWritten((*16K))','dskWrit, i.e. actual IO','ioWrit');
    grid on;
    nextPlot=nextPlot+1;
end

if DiskWriteMB_friendly==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), [mysqlTotalIOw mysqlLogIOw mysqlPagesWrittenMB mysqlPagesDblWrittenMB sysPhysicalIOw dbmsWrittenMB dbmsReadMB], '-.');
    title('Write Volume (MB)');
    xlabel(xlab);
    ylabel('Write Volume (MB/sec)');
    legend('MySQL total IO','MySQL log IO','mysqlPagesWrittenMB', 'mysqlPagesDblWrittenMB','System physical IO', 'Mysql Written (MB)', 'Mysql Read (MB)');
    grid on;
    nextPlot=nextPlot+1;
    
    fprintf(1,'total=%f, log=%f, dataPage=%f, dataDblPages=%f, physical=%f\n',mean(mysqlTotalIOw), mean(mysqlLogIOw), ...
        mean(mysqlPagesWrittenMB), mean(mysqlPagesDblWrittenMB), mean(sysPhysicalIOw));
end


if DiskWriteNum==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), diff(monitor(tstart:tend,[Innodb_log_writes Innodb_data_writes Innodb_dblwr_writes Innodb_log_write_requests Innodb_buffer_pool_write_requests Innodb_os_log_fsyncs async_aio])),...
    	Xdata, monitor(tstart:tend,[Innodb_data_pending_writes Innodb_os_log_pending_writes Innodb_os_log_pending_fsyncs]),'-');
    title('Write Requests (#)');
    xlabel(xlab);
    ylabel('Number of');
    legend('InnodbLogWrites','InnodbDataWrites','InnodbDblwrWrites','InnodbLogWriteRequests','InnodbBufferPoolWriteRequests','InnodbOsLogFsyncs','asyncAio',...
        'InnodbDataPendingWrites','InnodbOsLogPendingWrites','InnodbOsLogPendingFsyncs');
    grid on;
    nextPlot=nextPlot+1;
end

if DiskWriteNum_friendly==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), diff(monitor(tstart:tend,[Innodb_log_writes Innodb_data_writes Innodb_dblwr_writes Innodb_log_write_requests Innodb_buffer_pool_write_requests Innodb_os_log_fsyncs])),'-');
    title('Write Requests (#)');
    xlabel(xlab);
    ylabel('Number of');
    legend('InnodbLogWrites','InnodbDataWrites','InnodbDblwrWrites','InnodbLogWriteRequests','InnodbBufferPoolWriteRequests','InnodbOsLogFsyncs');
    grid on;
    nextPlot=nextPlot+1;
end

if DiskReadMB==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), diff(monitor(tstart:tend,[Innodb_data_read]))./1024./1024,...
        Xdata, monitor(tstart:tend,[dsk_read io_read])./1024./1024,'-');
    hold on;
    title('Read Volume (MB)');
    xlabel(xlab);
    ylabel('Read data (MB/sec)');
    legend('InnodbDataRead','dskRead','ioRead');
    grid on;
    nextPlot=nextPlot+1;
end


if DiskReadNum==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), diff(monitor(tstart:tend,[Innodb_data_reads Innodb_buffer_pool_reads])),...
        Xdata, monitor(tstart:tend,[Innodb_data_pending_reads]),'-');
    title('Read Requests (#)');
    xlabel(xlab);
    ylabel('Number of');
    legend('InnodbDataReads','InnodbBufferPoolReads','InnodbDataPendingReads');
    grid on;
    nextPlot=nextPlot+1;
end

if CacheHit==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    allowedRelativeDiff = 0.1;
    minFreq=100;
    if 1==1
        %grouping by total TPS
        [grouped freq] = GroupByAvg([SubmittedTransTotal SubmittedTransInd PhysicalKeyReads KeyReadRequests PhysicalReadMB], 1, allowedRelativeDiff, minFreq, 10, 1000);
        grouped = grouped(:,2:end);        
        %goupring by individual counts
        %[grouped freq] = GroupByAvg([SubmittedTransInd PhysicalKeyReads KeyReadRequests], 1:size(SubmittedTransInd,2), allowedRelativeDiff, minFreq, 10, 1000);
    else
        nPoints = 1;
        idx = randsample(size(SubmittedTransInd,1), nPoints, false);
        grouped = [SubmittedTransInd(idx,:) PhysicalKeyReads(idx,:) KeyReadRequests(idx,:) ];
    end    
    
    actualCacheMiss = grouped(:,end-2) ./ grouped(:,end-1);
    x = sum(grouped(:,1:end-3),2);
    plot(x, actualCacheMiss, 'b-.');
    hold on;
    for i=1:size(x,1)
        text(x(i),actualCacheMiss(i),num2str(grouped(i,end)));
    end
    
    %predictedCacheMiss = CacheMissRate([1], chosenSubmittedInd);    
    %plot(chosenX, predictedCacheMiss, 'r-.');
    %MRE = mre(predictedCacheMiss, actualCacheMiss(chosenInd));
    
    xlabel('TPS');
    ylabel('Actual miss ratio');
    %legend();
    ratio = mean(PhysicalKeyReads(KeyReadRequests>0) ./KeyReadRequests(KeyReadRequests>0));
    title(horzcat('Avg Read(MB)=',num2str(mean(PhysicalReadMB)),' Actual Cache miss ratio=', num2str(mean(actualCacheMiss)), ' =', num2str(mean(PhysicalKeyReads)), ' / ', num2str(mean(KeyReadRequests)), '=', num2str(ratio)));
    grid on;
    nextPlot=nextPlot+1;
end


if RowsChangedOverTime==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), diff(monitor(tstart:tend,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted Handler_write])),'-');
    title('Rows changed');
    xlabel(xlab);
    ylabel('# Rows changed');
    legend('Rows deleted','Rows updated','Rows inserted','HandlerWrite');
    grid on;
    nextPlot=nextPlot+1;
end

if RowsChangedPerWriteMB==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = [rowsChanged diff(monitor(tstart:tend,[Innodb_data_written Innodb_os_log_written]))./1024./1024 ...
        diff(monitor(tstart:tend,[Innodb_pages_written])).*2.*16./1024 monitor(tstart+1:tend,[dsk_writ])./1024./1024];
    temp = sortrows(temp, 1);
    plot(temp(:,1), temp(:,2:end));    
    title('Rows changed vs. written data (MB)');
    xlabel('# Rows Changed');
    ylabel('Written data (MB)');
    legend('MySQL total IO','MySQL log IO','MySQL data IO','System physical IO');
    grid on;
    nextPlot=nextPlot+1;
end

if RowsChangedPerWriteNo==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(rowsChanged,...
        diff(monitor(tstart:tend,[Innodb_log_writes Innodb_data_writes Innodb_dblwr_writes Innodb_log_write_requests Innodb_buffer_pool_write_requests Innodb_os_log_fsyncs async_aio])), '*',...
        rowsChanged, ...
        monitor(tstart+1:tend,[Innodb_data_pending_writes Innodb_os_log_pending_writes Innodb_os_log_pending_fsyncs]),'.');
    title('Rows changed vs. # write requests');
    xlabel('# Rows Changed');
    ylabel('Number of ');
    legend('InnodbLogWrites','InnodbDataWrites','InnodbDblwrWrites','InnodbLogWriteRequests','InnodbBufferPoolWriteRequests','InnodbOsLogFsyncs','asyncAio',...
        'InnodbDataPendingWrites','InnodbOsLogPendingWrites','InnodbOsLogPendingFsyncs');
    grid on;
    nextPlot=nextPlot+1;
end

if DirtyPagesPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
   
    if 1==0
        trainTime=tic;    
        trainSize = size(rowsChanged,1)/2;        
        D1 = lsqcurvefit(@mapRowsToPages, mean(pagesWithData)/8, cumRowsChanged(1:trainSize,:), currentPagesDirty(1:trainSize,:));
        
        trainData = zeros(trainSize,3);
        trainData(:,1) = rowsChanged(1:trainSize,:);
        trainData(:,2) = pagesFlushed(1:trainSize,:);
        trainData(1,3) = currentPagesDirty(1,1);            
        D2 = lsqcurvefit(@recursiveDirtyPageEstimate, mean(pagesWithData)/8, trainData, currentPagesDirty(1:trainSize,:));
                
        elapsed=toc(trainTime);
        fprintf(1,'Train time=%f\n', elapsed);
    else
        %for t1
        D1 = 158086.769859;
        D2 = 142131;% t1=156919.490116; t12345=142131 []
    end
    fprintf(1,'Best database cardinality estimations1=%f and estimation2=%f\n', D1, D2);
    
    testTime=tic;
    predictedDirtyPages1 = mapRowsToPages(D1, cumRowsChanged);
    
    testData = zeros(size(rowsChanged,1),3);
    testData(:,1) = rowsChanged;
    testData(:,2) = pagesFlushed;
    testData(1,3) = currentPagesDirty(1,1); 
    predictedDirtyPages2 = recursiveDirtyPageEstimate(D2, testData);
    
    %bestC = [2113090.173030 2000000 1 0.287701]; % t12345
    bestC = [2099659.5012109471 2000000 1 0.2757456055]; %t1
    
    log_capacity = bestC(1);
    max_log_capacity = bestC(2);
    maxPagesPerSecs = bestC(3); 
    logSizePerTransaction = bestC(4);
    [predictedDirtyPages3 predictedFlushRates]= estimateWriteIO(currentPagesDirty(1,1),D2,log_capacity,max_log_capacity,maxPagesPerSecs,logSizePerTransaction,rowsChanged);
    elapsed=toc(testTime);
    fprintf(1,'Test time=%f\n', elapsed);       
    
    MAE2=mae(predictedDirtyPages2, currentPagesDirty);
    MRE2=mre(predictedDirtyPages2, currentPagesDirty, true);
    fprintf(1,'Dirty page prediction from actual Flush MAE=%f, MRE=%f\n', MAE2, MRE2);
        
    MAE3=mae(predictedDirtyPages3, currentPagesDirty);
    MRE3=mre(predictedDirtyPages3, currentPagesDirty, true);
    fprintf(1,'Dirty page prediction from estimated Flush MAE=%f, MRE=%f\n', MAE3, MRE3);
        
    MAEf=mae(predictedFlushRates, currentPagesDirty);
    MREf=mre(predictedFlushRates, currentPagesDirty, true);
    fprintf(1,'Dirty flush rate from estimated dirty pages MAE=%f, MRE=%f\n', MAEf, MREf);
        
    temp = [SubmittedTransTotal currentPagesDirty predictedDirtyPages2 predictedDirtyPages3 predictedFlushRates];
    temp = sortrows(temp,1);
    ph1 = plot(temp(:,1), temp(:,2), 'c*');
    hold on;    
    ph2 = plot(temp(:,1), temp(:,3:5), '-');
    %plot(([pagesFlushed pagesTotal pagesWithData]), '*');
    %plot(([mysqlPagesDblWrittenMB *(1024 /(16*2))*1024 predictedDirtyPages1]), '-');
    
    
    title('Dirty page prediction using #rows changed');
    xlabel('TPS');
    ylabel('# of dirty pages');
    legend('Actual DP', 'Predcited DP from actual Flush', 'Predcited DP from estimated Flush', ...
          'Predicted flush rate', 'Actual Page flush rate','Total # pages','# data pages', '# written pages (using written MB)', 'Predicted (old model)');

    grid on;
    nextPlot=nextPlot+1;
end


if FlushRate==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    plot(Xdata(2:end), pagesFlushed,'-');
    hold on;
    range = pagesFlushed(end-100:end,:);
    m1 = mean(range);
    m2 = quantile(range, 0.5);
    m3 = quantile(range, 0.95);
    m4 = quantile(range, 1);
    title(horzcat('Flush Rate: mean=',num2str(m1),' q.5=',num2str(m2),' q.95=',num2str(m3),' max=',num2str(m4)));
    xlabel(xlab);
    ylabel('Pages flushed per sec');
    legend('Actual # of pages flushed');
    grid on;
    nextPlot=nextPlot+1;
end

if FlushRatePrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);

    %grouping for brk-100:  0.4, 50, 10, 1000 => 4 points
    %grouping for brk-200: 0.3, 50, 10, 1000 => 15 points
    %grouping for brk-800: 0.2, 70, 10, 1000
    %grping for brk-900: 0.3, 100, 10, 1000
    
    allowedRelativeDiff = 0.3;
    minFreq=30;
    %load('grouping.mat');
    if 1==1
        %grouping by total TPS
        %[grouped freq] = GroupByAvg([SubmittedTransTotal SubmittedTransInd pagesFlushed], 1, allowedRelativeDiff, minFreq, 10, 1000);
        %grouped = grouped(:,2:end);        
        %goupring by individual counts
        [grouped freq] = GroupByAvg([SubmittedTransInd pagesFlushed], 1:size(SubmittedTransInd,2), allowedRelativeDiff, minFreq, 10, 1000);
    else
        idx = randperm(size(SubmittedTransInd,1));
        idx = idx(1:10);
        grouped = [SubmittedTransInd(idx,:) pagesFlushed(idx,:)];
    end
 
%bestC = [200808             1000               20];
%temp = [SubmittedTransInd pagesFlushed];
%For wiki100k-dist-100
%grouped = zeros(6, size(temp,2));
%grouped(1,:) = mean(temp(2250:3900,:));
%grouped(2,:) = mean(temp(5250:6900,:));
%grouped(3,:) = mean(temp(8250:9900,:));
%grouped(4,:) = mean(temp(11240:12900,:));
%grouped(5,:) = mean(temp(14240:15940,:));
%grouped(6,:) = mean(temp(17240:17730,:));
%bestC = [50202 300 150];

%For wiki100k-io
%grouped = zeros(8, size(temp,2));
%grouped(1,:) = mean(temp(2350:3900,:));
%grouped(2,:) = mean(temp(5300:6900,:));
%grouped(3,:) = mean(temp(8250:9900,:));
%grouped(4,:) = mean(temp(11350:12900,:));
%grouped(5,:) = mean(temp(14200:15850,:));
%grouped(6,:) = mean(temp(17350:18800,:));
%grouped(7,:) = mean(temp(20200:21800,:));
%grouped(8,:) = mean(temp(23300:24950,:));
%bestC = [94099 300 119]; error: 18%

%For wiki-dist-100 and wiki-dist-900
%temp = [SubmittedTransInd pagesFlushed];
%grouped = zeros(10, size(temp,2));
%grouped(1,:) = mean(temp(1250:2900,:));
%grouped(2,:) = mean(temp(4250:5900,:));
%grouped(3,:) = mean(temp(7250:8900,:));
%grouped(4,:) = mean(temp(10250:11900,:));
%grouped(5,:) = mean(temp(13250:14900,:));
%grouped(6,:) = mean(temp(16250:17900,:));
%grouped(7,:) = mean(temp(19250:20900,:));
%grouped(8,:) = mean(temp(22250:23900,:));
%grouped(9,:) = mean(temp(25250:26900,:));
%grouped(10,:) = mean(temp(28250:29900,:));
%bestC = [50202 300 100];
% bestC = [67950 300 39]; for wiki-dist-900 => 3%
% bestC = [115050 300 27]; for wiki-dist-100 => 5%

    groupedCounts = grouped(:,1:size(SubmittedTransInd,2));
    groupedPagesFlushed = grouped(:,size(SubmittedTransInd,2)+1);
    
    if 1==0
        opt = optimset('MaxIter', 400, 'MaxFunEvals', 400, 'TolFun', 0.000000000001, 'DiffMinChange', 1, 'DiffMaxChange', 100);
        bmm = size(groupedCounts,1) / 2;
        bestC = lsqcurvefit(@cfFlushRateApprox, ...
            [2008080/20 300 50],...
            groupedCounts(1:bmm,:), groupedPagesFlushed(1:bmm,:), ...
            [2008080/40 299 10], [2008080/10 301 100], ... %[1.5e+6 190 1e+6], [2.5e+6 250 1.6e+6],
            opt) %lower and upper bounds, and options
        if 1==0 %  I think this worked perfectly for 
            bestC = lsqcurvefit(@expectedFlushRate, ...
                [1980367 200 1300000],...
                groupedCounts, groupedPagesFlushed, ...
                [1980366 190 1e+3], [1980367 220 1e+8], ... %[1.5e+6 190 1e+6], [2.5e+6 250 1.6e+6],
                opt) %lower and upper bounds, and options
        end
    else
        bestC = [2008080/1.8649 1000 2.57]; %t12345-brk-900, grouping(0.3, 100, 10, 1000), err:8.8%
        bestC = [2008080/1.45 1000 2.69]; %t12345-brk-800, grouping(0.2, 70, 10, 1000), err:1% => for brk-900, we get 14%, for brk-100 we get 12%, for brk-200 we get 14%
        bestC = [2008080/2.37 1000 0.8728274424]; %t12345-brk-100, grouping(0.4, 50, 10, 1000)
        bestC = [2008080/mean([1.8649 1.45 2.37]) 1000 mean([2.57 2.69 0.872])];
        %bestC = [2339382.8443              1000                10];
        %bestC = [2008080   216 0.14e7]; %wiki1k-io with cfFlushRateApprox gives 13% error
        %bestC = [2000   216 375102]; %wiki1k-io with cfFlushRateApprox using the real distribution!!
        %bestC = [2008080  216 0.44e6]; %synthetic powerlaw dist+wiki1k, no division by sum(PP)
        bestC = [50202 300 150];
        bestC = [94099 300 119];
        bestC100 = [115050 300 27];
        bestC900 = [67950  300 39];
        bestC = (bestC100 + bestC900) / 2;
        bestC = [115050 300 27];;
    end
    fprintf(1,'FlushRate bestC=%10.10f\n',bestC);
    
    if 1==1
        fid = fopen('bestC.txt', 'a'); 
        fprintf(fid, '%s\n', num2str(bestC));
        fclose(fid);
    end
    
    %max_log_capacity = bestC(1);
    %maxPagesPerSecs = bestC(2); 
    %domainCardinality = bestC(3);
    
    
    %predictedFlushRate = expectedFlushRate(bestC, ComCommit); 
    %MAE = mae(predictedFlushRate, pagesFlushed);
    %MRE = mre(predictedFlushRate, pagesFlushed);
    %fprintf(1, 'FlushRate prediction: Pointwise error:MAE=%f, MRE=%f\n', MAE, MRE);

%    [predictedGroupedFlush dpol] = expectedFlushRate(bestC, groupedCounts);   
%    predictedGroupedFlush = mcFlushRate(bestC, groupedCounts);   
    predictedGroupedFlush = cfFlushRateApprox(bestC, groupedCounts);
    MAE = mae(predictedGroupedFlush, groupedPagesFlushed);
    MRE = mre(predictedGroupedFlush, groupedPagesFlushed);
    fprintf(1, 'FlushRate prediction: expected value error:MAE=%f, MRE=%f\n', MAE, MRE);    
    
    %asymp = dpol .* (groupedCounts ./ max_log_capacity) ./ ((groupedCounts ./ max_log_capacity)-1)
    plot(groupedCounts(:,tranA) ./ sum(groupedCounts,2), [groupedPagesFlushed predictedGroupedFlush],'*');
    %plot(sum(groupedCounts,2), [groupedPagesFlushed predictedGroupedFlush],'*');

    
    title(horzcat('Flush rate prediction ', num2str(MRE),'%% '));
    xlabel('Average TPS');
    ylabel('# of pages flushed');
    legend('Actual flush rate', 'Predicted flush rate');

    grid on;
    nextPlot=nextPlot+1;
end

if Network==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), [NetworkRecvKB NetworkSendKB],'-');
    title('Network');
    xlabel(xlab);
    ylabel('KB');
    legend('Network recv(KB)','Network send(KB)');
    grid on;
    nextPlot=nextPlot+1;  
end

if LatencyPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    myCounts = zeros(tend-tstart,5);
    AllLatencies = zeros(endSmooth-startSmooth+1, 5);
    myCounts(:,[tranA tranB]) = [SubmittedTransA SubmittedTransB];
    AllLatencies(:,[tranA tranB]) = [AvgLatencyA(startSmooth:endSmooth,:) AvgLatencyB(startSmooth:endSmooth,:)];
    
    ratio = 0.3;
    trainSt=startSmooth;
    trainEnd=startSmooth+ (endSmooth-startSmooth)*ratio;
    testSt=trainEnd;
    testEnd=endSmooth;    
    if 1==1
        tic;   
        bestC = lsqcurvefit(@fitRealistic, [0.1 0.0001 0.00000001 0.01], myCounts(trainSt:trainEnd,:), ...
        AllLatencies(trainSt:trainEnd,:), ...
         [0 0 0.00000001 0.000001], [1 1 1 100],... %lower and upper bounds
         {'MaxIter',1000, 'TolFun', 0.00001}) % curve fitting params!
        fprintf(1,'training time=');
        toc;
    else
        % this is good for 25-dist
        bestC = [0.1050874431 0.0000999981 0.0000000100 0.0307117300];
        % this is for 12-dist
        bestC = [0.0983629881 0.0001000000 0.0000000100 0.0005281376];
    end
    tic;
    fprintf(1,'bestC=%10.10f\n',bestC);
    predictedLatencies = fitRealistic(bestC, myCounts(testSt:testEnd,:));
    %mean(predictedLatencies)
    %NOTE: Uncomment the following two lines for getting a baseline accuracy!
    %av = mean(AllLatencies(trainSt:trainEnd,:));
    %predictedLatencies = repmat(av, testEnd-testSt+1, 1);
    
    
    fprintf(1,'testing time=');
    toc;
    temp = [SubmittedTransTotal(testSt:testEnd,:) ...
            AllLatencies(testSt:testEnd,tranA) predictedLatencies(:,tranA) AllLatencies(testSt:testEnd,tranB) predictedLatencies(:,tranB)];
    %temp = sortrows(temp,1);
    plot(temp(:,2:end));
    
    MAEa = mae(temp(:,3), temp(:,2));
    MAEb = mae(temp(:,5), temp(:,4));
    MREa = mre(temp(:,3), temp(:,2));
    MREb = mre(temp(:,5), temp(:,4));
    
    title(horzcat('MAEa=',num2str(MAEa),' MREa=',num2str(MREa),' MAEb=',num2str(MAEb),' MREb=',num2str(MREb)));
    xlabel('TPS');
    ylabel('latency (sec)');
    legend('Actual latency A', 'Predicted latency A', 'Actual latency B', 'Predicted latency B');
    
    grid on;
    nextPlot=nextPlot+1;    
end

if LockConcurrencyPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    idx = randperm(size(SubmittedTransTotal,1));
    idx = idx(1:30);
    
    [xMax yMax] = findMaxThroughput(SubmittedTransTotal);
    if ~isempty(yMax)
        idx = xMax:(xMax+5);
        range = SubmittedTransInd(idx,:);
    else
        range = SubmittedTransInd(end-5:end,:);
    end
    bestC1 = [0.1250000000/100000 0.0001000000*1 1*43 0.4]; %[0.1250000000/100000 0.0001000000*1 1*43 0.4] = 35% err on t12345-brk1 with: 0.2 test/training ratio, grouping=(0.03, 30, 10, 650)
    
    bestC1 = [0.1250000000/2.5 0.0001000000*1 1*2 0.4*2]; % [0.1250000000/2.5 0.0001000000*1 1*2 0.4*2] = best for throughput prediction on t12, t12345
    bestC2 = [0.1250000000/2.6 0.0001000000*1 1*2 0.4*2]; %0.0100000000];
    bestC3 = [0.1250000000/2.2 0.0001000000*1 1*2 0.4*2]; %0.0100000000];
    bestC4 = [0.1250000000/1 0.0001000000*1 1*2 0.4*2]; %0.0100000000];
    
    b1=0; b2=0; b3=0; b4=0;
    tic;
    [all_R all_aT_total all_M_total all_Vp all_V all_W all_Pcon all_totalWaits all_TimeSpentWaiting] = useLockModel(bestC1, range);
    predictedM1 = all_M_total;
    if find(all_W==Inf); b1=1; end
    [all_R all_aT_total all_M_total all_Vp all_V all_W all_Pcon all_totalWaits all_TimeSpentWaiting] = useLockModel(bestC2, range);
    predictedM2 = all_M_total;
    if find(all_W==Inf); b2=1; end
    [all_R all_aT_total all_M_total all_Vp all_V all_W all_Pcon all_totalWaits all_TimeSpentWaiting] = useLockModel(bestC3, range);
    predictedM3 = all_M_total;
    if find(all_W==Inf); b3=1; end
    [all_R all_aT_total all_M_total all_Vp all_V all_W all_Pcon all_totalWaits all_TimeSpentWaiting] = useLockModel(bestC4, range);
    predictedM4 = all_M_total;
    if find(all_W==Inf); b4=1; end

    fprintf(1,'testing time=');
    toc;
    temp = [sum(range,2) predictedM1 predictedM2 predictedM3 predictedM4];
    temp = sortrows(temp,1);    
    
    %uncomment to see the shape!
    %temp = [temp(:,1) normMatrix(temp(:,2:end))]; 
    
    ph1 = plot(temp(:,1), temp(:,2:end), ':*');
    hold on;
    maxConcurrency = 160;
    %line([temp(1,1) temp(1,end)],[maxConcurrency maxConcurrency], 'Color', 'b', 'Linewidth', linewidth);
    
    title(horzcat('Concurrency Prediction: ',num2str(b1),num2str(b2),num2str(b3),num2str(b4),' ')); %horzcat('MAE=',num2str(MAE),' MRE=',num2str(MRE),' MRE2=',num2str(MRE2),' MRE3=',num2str(MRE3),' MRE4=',num2str(MRE4),' tps=(',num2str(MRE_tps),'%,',num2str(MAE_tps),'),class=(',num2str(MRE_class),'%,',num2str(MAE_class),')'));
    xlabel('TPS');
    ylabel('Expected number of in-flight transactions');
    legend('Predicted (our contention model)', 'p2', 'p3', 'p4', 'max # of connections');
    
    grid on;
    nextPlot=nextPlot+1;    
end


if BarzanPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    myCounts = zeros(tend-tstart,5);
    myCounts(:,[tranA tranB]) = [SubmittedTransA SubmittedTransB];
    lockMetrics = [LocksBeingWaitedFor(startSmooth:endSmooth,:) NumOfWaitsDueToLocks(startSmooth:endSmooth,:) TimeSpentWaitingForLocks(startSmooth:endSmooth,:)];
    
    ph1 = plot(SubmittedTransTotal(startSmooth:endSmooth,:), lockMetrics(:,3), 'b*');
    hold on;
    barzanIdx = [1 400 800 1200 1600 2000];
    barzanCV = myCounts(barzanIdx,:);

    for i=1:0.1:2
        fprintf(1,'i is %d\n', i);
        bestC = [0.125 0.0001  i 0.4];
        fprintf(1,'bestC=%10.10f\n',bestC);
        predictedLock = fitRealistic(bestC, barzanCV);
        ph2 = plot(barzanCV(:,1), predictedLock, 'r-');
        MAE = mae(predictedLock, lockMetrics(barzanIdx,3))
        MRE = mre(predictedLock, lockMetrics(barzanIdx,3))
    end
    xlabel('TPS');
    ylabel('Total time spent acquiring row locks (sec)');
    legend('Actual (reported by MySQL)', 'Predicted (our contention model)');
    
    grid on;
    nextPlot=nextPlot+1;    
end


if DirtyPagesOverTime==1 
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), [rowsChanged diff(monitor(tstart:tend,[Innodb_buffer_pool_pages_flushed])) ...
        monitor(tstart+1:tend,[Innodb_buffer_pool_pages_data Innodb_buffer_pool_pages_dirty Innodb_buffer_pool_pages_free Innodb_buffer_pool_pages_total ])], '-');
    title('Dirty pages over time');
    xlabel('Time');
    ylabel('# of Pages');
    legend('Rows Changed', 'Flushed pages','pages with data','dirty pages','free pages','buffer pool size (in pages)');
    grid on;
    nextPlot=nextPlot+1;    
end

if LockAnalysis==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    if MYSQL_ENV
        plot(Xdata(2:end), normMatrix([LocksBeingWaitedFor NumOfWaitsDueToLocks TimeSpentWaitingForLocks]),'*');
        legend('#locks being waited for','#waits, due to locks', 'time spent waiting for locks');
    elseif POSTGRES_ENV
        plot(Xdata(2:end), NumOfWaitsDueToLocks,'*');
        legend('#waits, due to locks');
    else
        error('Unknown DBMS');
    end
    title('Lock analysis');
    xlabel(xlab);
    ylabel('Locks (Normalized)');

    
    
    grid on;
    nextPlot=nextPlot+1;
end

if PagingInOut==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata, monitor(tstart:tend,[paging_in paging_out virtual_majpf]),'-');
    %mem_buff mem_cach mem_free mem_used 
    title('Memory analysis');
    xlabel(xlab);
    ylabel('Memory');
    legend('paging_in','paging_out','virtual_majpf');
    %'mem_buff','mem_cach','mem_free','mem_used',
    grid on;
    nextPlot=nextPlot+1;
end


if CombinedAvgLatency==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata, mean(avglat(tstart:tend,2:end)')','b-');
    hold on;
    plot(Xdata, mean(prclat.latenciesPCtile(tstart:tend,2:end,6)')','r-'); % showing 95%tile
    title('latency');
    xlabel(xlab);
    ylabel('latency (sec)');
    legend('Avg latency','Avg 95 % latency');
    grid on;
    nextPlot=nextPlot+1;
end

if LatencyA==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), AvgLatencyA,'b-');
    hold on;
    plot(Xdata, prclat.latenciesPCtile(tstart:tend,tranA+1,6),'r-'); % showing 95%tile
    plot(Xdata, prclat.latenciesPCtile(tstart:tend,tranA+1,7),'g-'); % showing 99%tile    
    title('latency');
    xlabel(xlab);
    ylabel('Latency (sec)');
    legend(horzcat('Latency ', num2str(tranA)),'95 % Latency A','99 % latency A');
    
    grid on;
    nextPlot=nextPlot+1;
end

if LatencyB==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(2:end), AvgLatencyB,'b-');
    hold on;
    plot(Xdata, prclat.latenciesPCtile(tstart:tend,tranB+1,6),'r-'); % showing 95%tile
    plot(Xdata, prclat.latenciesPCtile(tstart:tend,tranB+1,7),'g-'); % showing 99%tile
    title('latency');
    xlabel(xlab);
    ylabel('Latency (sec)');
    legend(horzcat('Latency ', num2str(tranB)),'95 % latency B','99 % latency B');
    grid on;
    nextPlot=nextPlot+1;
end

if LatencyOverall==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);  
    AvgLatencyAllLittle = 160 ./ SubmittedTransTotal;
    AcgLatencyAll = (SubmittedTransA .* AvgLatencyA + SubmittedTransB .* AvgLatencyB) ./ SubmittedTransTotal;
    plot(Xdata(2:end), [AvgLatencyAllLittle AcgLatencyAll] ,'*');
    a1= mae(AvgLatencyAllLittle, AcgLatencyAll);
    r1 = mre(AvgLatencyAllLittle, AcgLatencyAll);
    title('Overall latency');
    xlabel(xlab);
    ylabel('Latency (sec)');
    legend(horzcat('Little"s law MAE=', num2str(a1), ' MRE=', num2str(r1)), 'actual avg latency');
    
    grid on;
    nextPlot=nextPlot+1;
end

if LatencyVersusCPU==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(mean(monitor(tstart:tend,cpu_usr_indexes)')', avglat(tstart:tend,tranA),'b-');
    hold on;
    plot(mean(monitor(tstart:tend,cpu_usr_indexes)')', avglat(tstart:tend,tranA),'b-');
    title('CPU vs Latency');
    xlabel('Average CPU');
    ylabel('latency (sec)');
    legend('tran A','tran B');
    grid on;
    nextPlot=nextPlot+1;
end

if Latency3D==1 %this is for producing 3D crap!
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot3(IndividualCounts(tstart:tend,1), IndividualCounts(tstart:tend,2), avglat(tstart:tend,1), '-', ...
          IndividualCounts(tstart:tend,1), IndividualCounts(tstart:tend,2), avglat(tstart:tend,2), '-');
    title('latency');
    xlabel('Trans 1');
    ylabel('Trans 2');
    zlabel('latency (sec)');
    legend('latency 1','latency 2');
    grid on;
    nextPlot=nextPlot+1;
    set(gcf,'Color','w');
end

if workingSetSize==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp=diff(monitor(tstart:tend, [ ...
        Innodb_buffer_pool_read_ahead_rnd Innodb_buffer_pool_read_ahead_seq Innodb_buffer_pool_read_requests ...
        Innodb_buffer_pool_reads Innodb_buffer_pool_wait_free]));    

    plot(normMatrix(temp));
    legend(...
        'InnodbBufferPoolReadAheadRnd','InnodbBufferPoolReadAheadSeq','InnodbBufferPoolReadRequests',...
        'InnodbBufferPoolReads','InnodbBufferPoolWaitFree');
    
    % Innodb_buffer_pool_read_ahead? 
    % Innodb_buffer_pool_read_ahead_evicted?
    
    
    
    title('Working Set Analysis');
    xlabel(xlab);
    ylabel('?');
    grid on;
    nextPlot=nextPlot+1;
end

if workingSetSize2==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp=diff(monitor(tstart:tend, [ ...
        Handler_read_first Handler_read_key Handler_read_next Handler_read_prev Handler_read_rnd Handler_read_rnd_next]));

    plot(normMatrix(temp));
    legend(...
        'Handler_read_first','Handler_read_key','Handler_read_next','Handler_read_prev','Handler_read_rnd','Handler_read_rnd_next');  
    
    title('Working Set Analysis');
    xlabel(xlab);
    ylabel('?');
    grid on;
    nextPlot=nextPlot+1;
end


if LatencyPerTPS==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    AvgLatencyA=avglat(tstart+1:tend,tranA+1);
    AvgLatencyB=avglat(tstart+1:tend,tranB+1);
    
    temp = [SubmittedTransTotal AvgLatencyA AvgLatencyB];
    temp = sortrows(temp,1);
    plot(temp(:,1),temp(:,2:end));
    
    legend('avg latency A','avg latency B')
    title('Latency vs TPS');
    xlabel('TPS');
    ylabel('Latency (sec)');
    grid on;
    nextPlot=nextPlot+1;
end

if LatencyPerLocktime==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);

    RowLockTime=diff(monitor(tstart:tend,Innodb_row_lock_time));
    CurrentRowLockTime=monitor(tstart+1:tend,Innodb_row_lock_current_waits);
    
    temp = [CurrentRowLockTime AvgLatencyA AvgLatencyB];
    temp = sortrows(temp,1);
    plot(temp(:,1),temp(:,2:end));
        
    legend('avg latency A','avg latency B');    
    title('Latency vs Locktime');
    xlabel('row lock time');
    ylabel('');
    grid on;
    nextPlot=nextPlot+1;
end
    
if StrangeFeatures1==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(tstart+1:tend, [intr_82 intr_83 intr_84 intr_85 intr_86]);
    %temp=normMatrix(temp);
    plot(Xdata(2:end), temp,'-');

    title('Streange featurs 1');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('intr_82','intr_83','intr_84','intr_85','intr_86');
    grid on;
    nextPlot=nextPlot+1;
end

    
if StrangeFeatures2==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(tstart+1:tend, [files inodes]); 
    %temp=normMatrix(temp);
    plot(Xdata(2:end), temp,'-');

    title('Streange featurs 2');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('files','inodes');
    grid on;
    nextPlot=nextPlot+1;
end

    
if AllStrangeFeatures==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(tstart+1:tend, [dsk_read dsk_writ io_read io_writ async_aio swap_used swap_free paging_in paging_out virtual_majpf virtual_minpf virtual_alloc virtual_free files inodes intr_19 intr_23 intr_33 intr_79 intr_80 intr_81 intr_82 intr_83 intr_84 intr_85 intr_86 int csw proc_run proc_new sda_util]);
    %temp=normMatrix(temp);
    
    plot(Xdata(2:end), temp,'-');

    title('Streange featurs!');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('dsk_read','dsk_writ','io_read','io_writ','async_aio','swap_used','swap_free','paging_in','paging_out','virtual_majpf','virtual_minpf','virtual_alloc','virtual_free','files','inodes','intr_19','intr_23','intr_33','intr_79','intr_80','intr_81','intr_82','intr_83','intr_84','intr_85','intr_86','int','csw','proc_run','proc_new','sda_util');
    grid on;
    nextPlot=nextPlot+1;
end

 
if Interrupts==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(tstart+1:tend, [int csw proc_run proc_new sda_util]);
    %temp=normMatrix(temp);
    plot(Xdata(2:end), temp,'-');

    title('Interrupts');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('int','csw','proc_run','proc_new','sda_util');
    grid on;
    nextPlot=nextPlot+1;
end

elapsed = toc(overallTime);
fprintf(1,'elapsed time=%f\n',elapsed);

if exist('ph1')
    set(ph1, 'LineWidth', linewidth);
end
if exist('ph2')
    set(ph2, 'LineWidth', linewidth);
end
if exist('ph3')
    set(ph3, 'LineWidth', linewidth);
end
if exist('ph4')
    set(ph4, 'LineWidth', linewidth);
end
if exist('ph5')
    set(ph5, 'LineWidth', linewidth);
end
if exist('ph6')
    set(ph6, 'LineWidth', linewidth);
end


end
