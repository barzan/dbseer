classdef Plotter < handle
    properties
        mv
        Xdata
        Xlabel
    end
    
    methods
        function obj = set.mv(self, value)
            self.mv = value;
            self.Xdata = 1:1:value.numberOfObservations-1;
            self.Xdata = self.Xdata';
            self.Xlabel = 'Time (seconds)';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotIndividualCoreUsageUser(this)
            Xdata = {this.Xdata};
            Ydata = {this.mv.cpu_usr};
            Xlabel = this.Xlabel;
            Ylabel = 'Individual core usr usage';
            title = 'Individual core usr usage';
            legends = {'Core with MySQL'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotIndividualCoreUsageSys(this)
            Xdata = {this.Xdata};
            Ydata = {this.mv.cpu_sys};
            Xlabel = this.Xlabel;
            Ylabel = 'Individual core sys usage';
            title = 'Individual core sys usage';
            legends = {'Core with MySQL'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotInterCoreStandardDeviation(this)
            Xdata = {this.Xdata};
            Ydata = {sqrt(this.mv.CoreVariance)};
            Xlabel = this.Xlabel;
            Ylabel = 'Inter-core standard deviation';
            title = 'Standard deviation of core usage';
            legends = {'Inter-core std. dev.'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotAvgCpuUsage(this)
            mv = this.mv;
            Xdata = {this.Xdata}
            % Ydata = {[mv.AvgCpuUser mv.AvgCpuSys mv.AvgCpuWai mv.AvgCpuHiq mv.AvgCpuSiq mv.measuredCPU mv.AvgCpuIdle]};
            Ydata = {[mv.AvgCpuUser mv.AvgCpuSys mv.AvgCpuWai mv.AvgCpuHiq mv.AvgCpuSiq mv.AvgCpuIdle]};
            Xlabel = this.Xlabel;
            Ylabel = 'Average cpu usage (%)';
            title = 'Average CPU Usage';
            % legends = {'Usr', 'Sys', 'AvgCpuWai', 'AvgCpuHiq', 'AvgCpuSiq', 'MySQL Usage', 'Idle'};
            legends = {'Usr', 'Sys', 'AvgCpuWai', 'AvgCpuHiq', 'AvgCpuSiq', 'Idle'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotTPSCommitRollback(this)
            mv = this.mv;
            Xdata = {this.Xdata};
            Ydata = {mv.clientTotalSubmittedTrans};
            legends = {};
            legends{end+1} = 'Total client submitted transactions';
            for i=1:size(mv.clientIndividualSubmittedTrans, 2)
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.clientIndividualSubmittedTrans(:,i);
                legends{end+1} = horzcat('# Transactions ', num2str(i));
            end
            if isfield(mv, 'dbmsRollbackHandler')
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsRollbackHandler;
                legends{end+1} = 'DBMS Rollback Handler';
            end
            Xdata{end+1} = this.Xdata;
            Xdata{end+1} = this.Xdata;
            Ydata{end+1} = mv.dbmsCommittedCommands;
            Ydata{end+1} = mv.dbmsRolledbackCommands;
            legends{end+1} = 'DBMS Committed Commands';
            legends{end+1} = 'DBMS Rolledback Commands';
            
            % TODO: max throughput part is omitted
            Xlabel = this.Xlabel;
            Ylabel = 'Transactions (tps)';
            title = 'DBMS Transactions';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotContextSwitches(this)
            mv = this.mv;
            Xdata = {this.Xdata};
            Ydata = {mv.osNumberOfContextSwitches./1500};
            legends = {};
            legends{end+1} = 'Context Switches (x1500)';
            if isfield(mv, 'dbmsThreadsRunning')
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsThreadsRunning;
                legends{end+1} = 'Threads running';
            end
            Xlabel = this.Xlabel;
            Ylabel = '# of threads';
            title = 'Threads';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDiskWriteMB(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            if isfield(mv, 'dbmsTotalWritesMB')
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsTotalWritesMB;
                legends{end+1} = 'DB Total Writes (MB)';
            end
            Xdata{end+1} = this.Xdata;
            Ydata{end+1} = mv.dbmsLogWritesMB;
            legends{end+1} = 'DB Log Writes (MB)';
            Xdata{end+1} = this.Xdata;
            Ydata{end+1} = mv.dbmsPageWritesMB;
            legends{end+1} = 'DB Page Writes (MB) (pages=16K)';
            if isfield(mv, 'dbmsDoublePageWritesMB')
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsDoublePageWritesMB;
                legends{end+1} = 'DB Double Page Writes (MB) (half of dirty pages)';
            end
            Xdata{end+1} = this.Xdata;
            Xdata{end+1} = this.Xdata;
            Ydata{end+1} = mv.osNumberOfSectorWrites;
            Ydata{end+1} = mv.osNumberOfWritesCompleted;
            legends{end+1} = 'OS No. Sector Writes (actual IO)';
            legends{end+1} = 'Os No. Writes Completed';
            Xlabel = this.Xlabel;
            Ylabel = 'Written Data (MB/sec)';
            title = 'Write Volume (MB)';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDiskWriteMB_friendly(this)
            mv = this.mv;
            Xdata = {this.Xdata};
            if isfield(mv, 'dbmsDoublePageWritesMB')
                Ydata = {[mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.dbmsDoublePageWritesMB mv.osNumberOfSectorWrites]};
                % Ydata = {[mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.dbmsDoublePageWritesMB mv.osNumberOfSectorWrites mv.measuredWritesMB mv.measuredReadsMB]};
                legends = {'DB Total Writes','DB Log Writes','DB Page Writes', 'DB Double Page Writes','OS No. Sector Writes'};
                % legends = {'DB Total Writes','DB Log Writes','DB Page Writes', 'DB Double Page Writes','OS No. Sector Writes', 'Measured Writes', 'Measured Reads'};
            else
                Ydata = {[mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites]};
                % Ydata = {[mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites mv.measuredWritesMB mv.measuredReadsMB]};
                legends = {'DB Log Writes','DB Page Writes', 'OS No. Sector Writes'};
                % legends = {'DB Log Writes','DB Page Writes', 'OS No. Sector Writes', 'Measured Writes', 'Measured Reads'};
            end
            
            Xlabel = this.Xlabel;
            Ylabel = 'Write Volume (MB/sec)';
            title = 'Write Volume (MB)';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDiskWriteNum(this)
            mv = this.mv;
            if isfield(mv, 'dbmsNumberOfPhysicalLogWrites')
                Xdata = {this.Xdata};
                Ydata = {[mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites mv.osAsynchronousIO mv.dbmsNumberOfPendingWrites mv.dbmsNumberOfPendingLogWrites mv.dbmsNumberOfPendingLogFsyncs]};
                legends = {'DB No. Physical Log Writes','DB No. Data Writes','DB Double Writes Operations','DB No. Log Write Requests','DB Buffer Pool Writes','DB No. Fysnc Log Writes','osAsynchronousIO', 'dbmsNumberOfPendingWrites','dbmsNumberOfPendingLogWrites','dbmsNumberOfPendingLogFsyncs'}
                Xlabel = this.Xlabel;
                Ylabel = 'Number of';
                title = 'Write Requests (#)';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDiskWriteNum_friendly(this)
            mv = this.mv;
            if isfield(mv, 'dbmsNumberOfPhysicalLogWrites') && isfield(mv, 'dbmsNumberOfDataWrites') && isfield(mv, 'dbmsDoubleWritesOperations') && isfield(mv, 'dbmsNumberOfLogWriteRequests')...
                && isfield(mv, 'dbmsBufferPoolWrites') && isfield(mv, 'dbmsNumberOfFysncLogWrites')
                
                Xdata = {this.Xdata};
                Ydata = {[mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites]};
                title = 'Write Requests (#)';
                Xlabel = this.Xlabel;
                Ylabel = 'Number of';
                legends = {'DB No. Physical Log Writes','DB No. Data Writes','DB Double Writes Operations','DB No. Log Write Requests','DB Buffer Pool Writes','DB No. Fysnc Log Writes'};
                    
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDiskReadMB(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            if exist('mv.dbmsPhysicalReadsMB', 'var')
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsPhysicalReadsMB;
                legends{end+1} = 'InnoDB Data Read';
            end
            Xdata{end+1} = this.Xdata;
            Xdata{end+1} = this.Xdata;
            Ydata{end+1} = mv.osNumberOfSectorReads;
            Ydata{end+1} = mv.osNumberOfReadsIssued;
            legends{end+1} = 'Disk Read';
            legends{end+1} = 'IO Read';
            Xlabel = this.Xlabel;
            Ylabel = 'Read data (MB/sec)';
            title = 'Read Volume (MB)';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDiskReadNum(this)
            mv = this.mv;
            if isfield(mv, 'dbmsNumberOfDataReads')
                Xdata = {this.Xdata};
                Xlabel = this.Xlabel;
                Ydata = {[mv.dbmsNumberOfDataReads mv.dbmsNumberOfLogicalReadsFromDisk mv.dbmsNumberOfPendingReads]};
                Ylabel = 'Number of';
                legends = {'DB No. Data Reads','DB No. Logical Reads From Disk','DB No. Pending Reads'};
                title = 'Read Requests (#)';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotCacheHit(this)
            mv = this.mv;
            allowedRelativeDiff = 0.1;
            minFreq=100;
            
            %grouping by total TPS
            if isfield(mv, 'dbmsPhysicalReadsMB')
                [grouped freq] = GroupByAvg([mv.clientTotalSubmittedTrans mv.clientIndividualSubmittedTrans mv.dbmsReads mv.dbmsReadRequests mv.dbmsPhysicalReadsMB], 1, allowedRelativeDiff, minFreq, 10, 1000);
            else
                [grouped freq] = GroupByAvg([mv.clientTotalSubmittedTrans mv.clientIndividualSubmittedTrans mv.dbmsReads mv.dbmsReadRequests], 1, allowedRelativeDiff, minFreq, 10, 1000);
            end
            grouped = grouped(:,2:end);        
            actualCacheMiss = grouped(:,end-2) ./ grouped(:,end-1);
            x = sum(grouped(:,1:end-3),2);
            ratio = mean(mv.dbmsReads(mv.dbmsReadRequests>0) ./mv.dbmsReadRequests(mv.dbmsReadRequests>0));
            
            Xdata = {x};
            Ydata = {actualCacheMiss};
            Xlabel = 'TPS';
            Ylabel = 'Actual miss ratio';
            legends = {};
            
            if isfield(mv, 'dbmsPhysicalReadsMB')
                title = ['Avg Read(MB)=' num2str(mean(mv.dbmsPhysicalReadsMB),1) ' Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads),3) '/' num2str(mean(mv.dbmsReadRequests),1) '=' num2str(ratio,3)];
            else
                title = ['Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads),3) '/' num2str(mean(mv.dbmsReadRequests),1) '=' num2str(ratio,3)];
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotRowsChangedOverTime(this)
            mv = this.mv;
            if isfield(mv, 'dbmsChangedRows')
                Xdata = {this.Xdata};
                Xlabel = this.Xlabel;
                Ydata = {[mv.dbmsChangedRows mv.dbmsNumberOfRowInsertRequests]};
                Ylabel = '# Rows Changed';
                legends = {'Rows deleted','Rows updated','Rows inserted','HandlerWrite'};
                title = 'Rows Changed';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotRowsChangedPerWriteMB(this)
            mv = this.mv;
            if isfield(mv, 'dbmsChangedRows')
                temp = [mv.dbmsChangedRows mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
            elseif isfield(mv, 'dbmsTotalWritesMB')
                temp = [mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
            else
                temp = [mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
            end
            temp = sortrows(temp, 1);
            Xdata = {temp(:,1)};
            Ydata = {temp(:,2:end)};
            title = 'Rows changed vs. written data (MB)';
            Xlabel = '# Rows Changed';
            Ylabel = 'Written data (MB)';
            legends = {'MySQL total IO', 'MySQL log IO', 'MySQL data IO', 'System physical IO'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotRowsChangedPerWriteNo(this)
            mv = this.mv;
            if isfield(mv, 'dbmsNumberOfPhysicalLogWrites')
                Xdata = {mv.dbmsChangedRows};
                Ydata = {[mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites mv.osAsynchronousIO mv.dbmsNumberOfPendingWrites mv.dbmsNumberOfPendingLogWrites mv.dbmsNumberOfPendingLogFsyncs]};
                Xlabel = '# Rows Changed';
                Ylabel = 'Number of';
                legends = {'InnodbLogWrites', 'InnodbDataWrites', 'InnodbDblwrWrites', 'InnodbLogWriteRequests', 'InnodbBufferPoolWriteRequests', 'InnodbOsLogFsyncs', 'asyncAio', 'InnodbDataPendingWrites','InnodbOsLogPendingWrites','InnodbOsLogPendingFsyncs'};
                title = 'Rows Changed vs. # Write Requests';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDirtyPagesPrediction(this)
            % TODO: not done because of hard-coded numbers 
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotFlushRate(this)
            mv = this.mv
            Xdata = {this.Xdata};
            Ydata = {mv.dbmsFlushedPages};
            range = mv.dbmsFlushedPages(end-100:end,:);
            m1 = mean(range);
            m2 = quantile(range, 0.5);
            m3 = quantile(range, 0.95);
            m4 = quantile(range, 1);
            title = horzcat('Flush Rate: mean=',num2str(m1),' q.5=',num2str(m2),' q.95=',num2str(m3),' max=',num2str(m4));
            Xlabel = this.Xlabel;
            Ylabel = 'Pages flushed per second';
            legends = {'Actual # of pages flushed'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotNetwork(this)
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            Ydata = {[mv.osNetworkRecvKB mv.osNetworkSendKB]};
            Ylabel = 'KB';
            legends = {'Network recv(KB)','Network send(KB)'};
            title = 'Network';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatencyPrediction(this)
            % TODO: not implemented due to hard-coded number in original implementation.
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLockConcurrencyPrediction(this)
            % TODO: not implemented due to hard-coded number in original implementation.
        end

        function [title legends Xdata Ydata Xlabel Ylabel] = plotBarzanPrediction(this)
            % TODO: not implemented due to hard-coded number in original implementation.
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotDirtyPagesOverTime(this)
            % TODO: handle monitor variable.
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = 'Time';
            Ydata = {dbmsChangedRows dM(:,[Innodb_buffer_pool_pages_flushed]) monitor(:,[Innodb_buffer_pool_pages_data Innodb_buffer_pool_pages_dirty Innodb_buffer_pool_pages_free Innodb_buffer_pool_pages_total ])};
            Ylabel = '# of Pages';
            legends = {'Rows Changed', 'Flushed pages','pages with data','dirty pages','free pages','buffer pool size (in pages)'};
            title = 'Dirty pages over time';
        end

        function [title legends Xdata Ydata Xlabel Ylabel] = plotLockAnalysis(this)
            mv = this.mv;
            if isfield(mv, 'dbmsCurrentLockWaits')
                Xdata = {this.Xdata};
                Xlabel = this.Xlabel;
                Ydata = {normMatrix([mv.dbmsCurrentLockWaits mv.dbmsLockWaits mv.dbmsLockWaitTime])};
                Ylabel = 'Locks (Normalized)';
                legends = {'#locks being waited for','#waits, due to locks', 'time spent waiting for locks'};
                title = 'Lock analysis';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotPagingInOut(this)
            % TODO: handle monitor variable.
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            Ydata = {monitor(:,[paging_in paging_out virtual_majpf])};
            Ylabel = 'Memory';
            title = 'Memory Analysis';
            legends = {'paging_in','paging_out','virtual_majpf'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotCombinedAvgLatency(this)
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            %Ydata = [mean(mv.clientTransLatency(:,2:end),2) mean(mv.prclat.latenciesPCtile(:,2:end,6), 2)];
            combinedLatency = sum(mv.clientTransLatency .* mv.clientIndividualSubmittedTrans,2)./mv.clientTotalSubmittedTrans;
            combinedLatency(isnan(combinedLatency)) = 0;
            Ydata = {combinedLatency};
            %Xdata{end+1} = this.Xdata;
            %Ydata{end+1} = mean(mv.prclat.latenciesPCtile(:,2:end,6), 2);
            Ylabel = 'latency (sec)';
            legends = {'Average latency'}; %,'Avg 95 % latency'};
            title = 'Latency';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatency(this)
            % TODO: where does 'clientAvgLatencyA' come from?
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatencyOverall(this)
            mv = this.mv;
            AvgLatencyAllLittle = 160 ./ mv.clientTotalSubmittedTrans;
            AcgLatencyAll = sum(mv.clientIndividualSubmittedTrans .* mv.clientTransLatency, 2) ./ mv.clientTotalSubmittedTrans;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            Ydata = {[AvgLatencyAllLittle AcgLatencyAll]};
            Ylabel = 'Latency (sec)';
            a1= mae(AvgLatencyAllLittle, AcgLatencyAll);
            r1 = mre(AvgLatencyAllLittle, AcgLatencyAll);
            legends = {horzcat('Little"s law MAE=', num2str(a1), ' MRE=', num2str(r1)), 'actual avg latency'};
            title = 'Overall Latency';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatencyVersusCPU(this)
            mv = this.mv;
            Xdata = {};
            Xlabel = 'Average CPU';
            Ydata = {};
            legends = {};
            for i=1:mv.numOfTransType
                Xdata{end+1} = mean(mv.cpu_usr,2);
                Ydata{end+1} = mv.clientTransLatency(:,i);
                legends{end+1} = horzcat('tran', num2str(i));
            end
            Ylabel = 'latency (sec)';
            title = 'CPU vs Latency';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatency3D(this)
            % TODO: 3D support in JFreeChart?
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotWorkingSetSize(this)
            mv = this.mv;
            if isfield(mv, 'dbmsRandomReadAheads')
                temp=[ ...
                    mv.dbmsRandomReadAheads mv.dbmsSequentialReadAheads mv.dbmsNumberOfLogicalReadRequests ...
                    mv.dbmsNumberOfLogicalReadsFromDisk mv.dbmsNumberOfWaitsForFlush];    
                Xdata = {this.Xdata};
                Xlabel = this.Xlabel;
                Ydata = {normMatrix(temp)};
                Ylabel = '?';
                legends = {'InnodbBufferPoolReadAheadRnd', 'InnodbBufferPoolReadAheadSeq', 'InnodbBufferPoolReadRequests', 'InnodbBufferPoolReads','InnodbBufferPoolWaitFree'};
                title = 'Working Set Analysis';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotWorkingSetSize2(this)
            mv = this.mv;
            if isfield(mv, 'dbmsNumberOfNextRowReadRequests')
                temp=[mv.dbmsNumberOfFirstEntryReadRequests mv.dbmsNumberOfKeyBasedReadRequests mv.dbmsNumberOfNextKeyBasedReadRequests mv.dbmsNumberOfPrevKeyBasedReadRequests mv.dbmsNumberOfRowReadRequests mv.dbmsNumberOfNextRowReadRequests];
                Xdata = {this.Xdata};
                Xlabel = this.Xlabel;
                Ydata = {normMatrix(temp)};
                Ylabel = '?';
                legends = {'Handler_read_first', 'Handler_read_key', 'Handler_read_next', 'Handler_read_prev', 'Handler_read_rnd', 'Handler_read_rnd_next'};
                title = 'Working Set Analysis';
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatencyPerTPS(this)
            mv = this.mv;
            temp = [mv.clientTotalSubmittedTrans mv.clientTransLatency];
            temp = sortrows(temp,1);
            Xdata = {};
            Xlabel = 'TPS';
            Ydata = {};
            legends = {};
            for i=1:mv.numOfTransType
                Xdata{end+1} = temp(:,1);
                Ydata{end+1} = temp(:,i+1);
                legends{end+1} = horzcat('Avg Latency ', num2str(i));
            end
            title = 'Latency vs. TPS';
            Ylabel = 'Latency (sec)';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotLatencyPerLocktime(this)
            mv = this.mv;
            CurrentRowLockTime=mv.dbmsCurrentLockWaits;
    
            temp = [CurrentRowLockTime mv.clientTransLatency];
            temp = sortrows(temp,1);
            Xdata = {temp(:,1)};
            Xlabel = 'Row lock time';
            Ydata = {temp(:,2:end)};
            Ylabel = '';
            title = 'Latency vs. Lock time';
            legends = {'avg latency A','avg latency B'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotStrangeFeatures1(this)
            % TODO: do we need this?
        end

        function [title legends Xdata Ydata Xlabel Ylabel] = plotStrangeFeatures2(this)
            % TODO: do we need this?
        end

        function [title legends Xdata Ydata Xlabel Ylabel] = plotAllStrangeFeatures(this)
            % TODO: do we need this?
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotInterrupts(this)
            % TODO: handle monitor variable
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotFlushRatePrediction(this)
            % TODO: has hard-coded numbers...
        end
        
    end % end methods
end