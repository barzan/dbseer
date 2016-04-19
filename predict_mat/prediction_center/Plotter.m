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

classdef Plotter < handle
    properties
        mv
        Xdata
        Xlabel
    end

    methods
        function obj = set.mv(self, value)
            self.mv = value;
            self.Xdata = 1:1:min(value.numberOfObservations);
            self.Xdata = self.Xdata';
            self.Xlabel = 'Time (seconds)';
        end

        function [Xdata Ydata] = plotCustom(this, xField, yField)
          Xdata = {this.getData(xField)};
          Ydata = {this.getData(yField)};
        end

        function data = getData(this, field)
          if strcmp(field, 'time')
            data = [1:1:min(this.mv.numberOfObservations)]';
          elseif strcmp(field, 'averageTransLatency')
            mv = this.mv;
            combinedLatency = sum(mv.clientTransLatency .* mv.clientIndividualSubmittedTrans,2)./mv.clientTotalSubmittedTrans;
            combinedLatency(isnan(combinedLatency)) = 0;
            data = combinedLatency;
          else
            data = getfield(this.mv, field);
          end
          % this is temporary work-around.
          %if size(data,2) > 1
            %data = sum(data,2);
          %end
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotIndividualCoreUsageUser(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            Xlabel = this.Xlabel;
            Ylabel = 'Individual core usr usage';
            title = 'Individual core usr usage';
            timestamp = this.Xdata;

            start_idx = 1;
            for i=1:size(mv.numOfTransType,2)
                Xdata{end+1} = this.Xdata;
                num_core = size(mv.headers{i}.metadata.cpu_usr, 2);
                end_idx = start_idx + num_core - 1;
                Ydata{end+1} = this.mv.cpu_usr(:,start_idx:end_idx);
                start_idx = end_idx+1;
                server_name = this.getServerName(mv, i);
                for j=1:num_core
                    legends{end+1} = horzcat(server_name, ' Core ', num2str(j));
                end
            end
            %Xdata = {this.Xdata};
            %Ydata = {this.mv.cpu_usr};
            %legends = {'Core '};
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotIndividualCoreUsageSys(this)

            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            Xlabel = this.Xlabel;
            Ylabel = 'Individual core sys usage';
            title = 'Individual core sys usage';
            timestamp = this.Xdata;

            start_idx = 1;
            for i=1:size(mv.numOfTransType,2)
                Xdata{end+1} = this.Xdata;
                num_core = size(mv.headers{i}.metadata.cpu_sys, 2);
                end_idx = start_idx + num_core - 1;
                Ydata{end+1} = this.mv.cpu_sys(:,start_idx:end_idx);
                start_idx = end_idx+1;
                server_name = this.getServerName(mv, i);
                for j=1:num_core
                    legends{end+1} = horzcat(server_name, ' Core ', num2str(j));
                end
            end
            %Xdata = {this.Xdata};
            %Ydata = {this.mv.cpu_sys};
            %Xlabel = this.Xlabel;
            %Ylabel = 'Individual core sys usage';
            %title = 'Individual core sys usage';
            %legends = {'Core with MySQL'};
            %timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotInterCoreStandardDeviation(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            Xlabel = this.Xlabel;
            Ylabel = 'Inter-core standard deviation';
            title = 'Standard deviation of core usage';
            timestamp = this.Xdata;

            start_idx = 1;
            for i=1:size(mv.numOfTransType,2)
                Xdata{end+1} = this.Xdata;
                num_core = size(mv.headers{i}.metadata.cpu_usr, 2);
                end_idx = start_idx + num_core - 1;
                Ydata{end+1} = sqrt(this.mv.CoreVariance(:,i));
                start_idx = end_idx+1;
                server_name = this.getServerName(mv, i);
                legends{end+1} = horzcat(server_name, ' Inter-core std. dev.');
            end
            %Xdata = {this.Xdata};
            %Ydata = {sqrt(this.mv.CoreVariance)};
            %Xlabel = this.Xlabel;
            %Ylabel = 'Inter-core standard deviation';
            %title = 'Standard deviation of core usage';
            %legends = {'Inter-core std. dev.'};
            %timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotAvgCpuUsage(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            Xlabel = this.Xlabel;
            timestamp = this.Xdata;
            Ylabel = 'Average cpu usage (%)';
            title = 'Average CPU Usage';

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = [mv.AvgCpuUser(:,i) mv.AvgCpuSys(:,i) mv.AvgCpuWai(:,i) mv.AvgCpuHiq(:,i) mv.AvgCpuSiq(:,i) mv.AvgCpuIdle(:,i)];
                legends{end+1} = horzcat(server_name, ' Usr');
                legends{end+1} = horzcat(server_name, ' Sys');
                legends{end+1} = horzcat(server_name, ' AvgCpuWai');
                legends{end+1} = horzcat(server_name, ' AvgCpuHiq');
                legends{end+1} = horzcat(server_name, ' AvgCpuSiq');
                legends{end+1} = horzcat(server_name, ' Idle');
            end

            %Xdata = {this.Xdata};
            %Ydata = {[mv.AvgCpuUser mv.AvgCpuSys mv.AvgCpuWai mv.AvgCpuHiq mv.AvgCpuSiq mv.AvgCpuIdle]};
            %legends = {'Usr', 'Sys', 'AvgCpuWai', 'AvgCpuHiq', 'AvgCpuSiq', 'Idle'};
            %timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotTPSCommitRollback(this)
            mv = this.mv;
			Xdata = {};
			Ydata = {};
            legends = {};
            count = 1;
            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = mv.clientIndividualSubmittedTrans(:,count);
                    legends{end+1} = horzcat(server_name, ' # of Type ', num2str(j), ' Transactions');
                    count = count + 1;
                end
            end
			Xdata{end+1} = this.Xdata;
			Ydata{end+1} = mv.clientTotalSubmittedTrans;
            legends{end+1} = 'Total client submitted transactions';
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
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotContextSwitches(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            timestamp = this.Xdata;

            for i=1:size(mv.numOfTransType, 2)
                server_name = this.getServerName(mv , i);
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.osNumberOfContextSwitches(:,i) ./ 1500;
                legends{end+1} = horzcat(server_name, ' Context Switches (x1500)');
                if isfield(mv, 'dbmsThreadsRunning')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = mv.dbmsThreadsRunning(:,i);
                    legends{end+1} =  horzcat(server_name, ' Threads running');
                end
            end

            Xlabel = this.Xlabel;
            Ylabel = '# of threads';
            title = 'Threads';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDiskWriteMB(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType, 2)
                server_name = this.getServerName(mv , i);
                if isfield(mv, 'dbmsTotalWritesMB')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = mv.dbmsTotalWritesMB(:,i);
                    legends{end+1} = horzcat(server_name, ' DB Total Writes (MB)');
                end
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsLogWritesMB(:,i);
                legends{end+1} = horzcat(server_name, ' DB Log Writes (MB)');
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.dbmsPageWritesMB(:,i);
                legends{end+1} = horzcat(server_name, ' DB Page Writes (MB) (pages=16K)');
                if isfield(mv, 'dbmsDoublePageWritesMB')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = mv.dbmsDoublePageWritesMB(:,i);
                    legends{end+1} = horzcat(server_name, ' DB Double Page Writes (MB) (half of dirty pages)');
                end
                Xdata{end+1} = this.Xdata;
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.osNumberOfSectorWrites(:,i);
                Ydata{end+1} = mv.osNumberOfWritesCompleted(:,i);
                legends{end+1} = horzcat(server_name, ' OS No. Sector Writes (actual IO)');
                legends{end+1} = horzcat(server_name, ' OS No. Writes Completed');
            end
            Xlabel = this.Xlabel;
            Ylabel = 'Written Data (MB/sec)';
            title = 'Write Volume (MB)';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDiskWriteMB_friendly(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv , i);
                Xdata{end+1} = this.Xdata;
                if isfield(mv, 'dbmsDoublePageWritesMB')
                    Ydata{end+1} = [mv.dbmsTotalWritesMB(:,i) mv.dbmsLogWritesMB(:,i) mv.dbmsPageWritesMB(:,i) mv.dbmsDoublePageWritesMB(:,i) mv.osNumberOfSectorWrites(:,i)];
                    % Ydata = {[mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.dbmsDoublePageWritesMB mv.osNumberOfSectorWrites mv.measuredWritesMB mv.measuredReadsMB]};
                    legends{end+1} = horzcat(server_name, ' DB Total Writes');
                    legends{end+1} = horzcat(server_name, ' DB Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB Page Writes');
                    legends{end+1} = horzcat(server_name, ' DB Double Page Writes');
                    legends{end+1} = horzcat(server_name, ' OS No. Sector Writes');
                    % legends = {'DB Total Writes','DB Log Writes','DB Page Writes', 'DB Double Page Writes','OS No. Sector Writes', 'Measured Writes', 'Measured Reads'};
                else
                    Ydata{end+1} = [mv.dbmsLogWritesMB(:,i) mv.dbmsPageWritesMB(:,i) mv.osNumberOfSectorWrites(:,i)];
                    % Ydata = {[mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites mv.measuredWritesMB mv.measuredReadsMB]};
                    legends{end+1} = horzcat(server_name, ' DB Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB Page Writes');
                    legends{end+1} = horzcat(server_name, ' OS No. Sector Writes');
                    %legends = {'DB Log Writes','DB Page Writes', 'OS No. Sector Writes'};
                    % legends = {'DB Log Writes','DB Page Writes', 'OS No. Sector Writes', 'Measured Writes', 'Measured Reads'};
                end
            end

            Xlabel = this.Xlabel;
            Ylabel = 'Write Volume (MB/sec)';
            title = 'Write Volume (MB)';
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDiskWriteNum(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsNumberOfPhysicalLogWrites')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = [mv.dbmsNumberOfPhysicalLogWrites(:,i) mv.dbmsNumberOfDataWrites(:,i) mv.dbmsDoubleWritesOperations(:,i) mv.dbmsNumberOfLogWriteRequests(:,i) mv.dbmsBufferPoolWrites(:,i) mv.dbmsNumberOfFysncLogWrites(:,i) mv.osAsynchronousIO(:,i) mv.dbmsNumberOfPendingWrites(:,i) mv.dbmsNumberOfPendingLogWrites(:,i) mv.dbmsNumberOfPendingLogFsyncs(:,i)];
                    %legends = {'DB No. Physical Log Writes','DB No. Data Writes','DB Double Writes Operations','DB No. Log Write Requests','DB Buffer Pool Writes','DB No. Fysnc Log Writes','osAsynchronousIO', 'dbmsNumberOfPendingWrites','dbmsNumberOfPendingLogWrites','dbmsNumberOfPendingLogFsyncs'};
                    legends{end+1} = horzcat(server_name, ' DB No. Physical Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB No. Data Writes');
                    legends{end+1} = horzcat(server_name, ' DB Double Writes Operations');
                    legends{end+1} = horzcat(server_name, ' DB No. Log Write Requests');
                    legends{end+1} = horzcat(server_name, ' DB Buffer Pool Writes');
                    legends{end+1} = horzcat(server_name, ' DB No. Fysnc Log Writes');
                    legends{end+1} = horzcat(server_name, ' OS Asynchronous IOs');
                    legends{end+1} = horzcat(server_name, ' DB No. Pending Writes');
                    legends{end+1} = horzcat(server_name, ' DB No. Pending Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB No. Pending Log Fsyncs');
                end
            end

            Xlabel = this.Xlabel;
            Ylabel = 'Number of';
            title = 'Write Requests (#)';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDiskWriteNum_friendly(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsNumberOfPhysicalLogWrites') && isfield(mv, 'dbmsNumberOfDataWrites') && isfield(mv, 'dbmsDoubleWritesOperations') && isfield(mv, 'dbmsNumberOfLogWriteRequests')...
                    && isfield(mv, 'dbmsBufferPoolWrites') && isfield(mv, 'dbmsNumberOfFysncLogWrites')

                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = [mv.dbmsNumberOfPhysicalLogWrites(:,i) mv.dbmsNumberOfDataWrites(:,i) mv.dbmsDoubleWritesOperations(:,i) mv.dbmsNumberOfLogWriteRequests(:,i) mv.dbmsBufferPoolWrites(:,i) mv.dbmsNumberOfFysncLogWrites(:,i)];
                    %legends = {'DB No. Physical Log Writes','DB No. Data Writes','DB Double Writes Operations','DB No. Log Write Requests','DB Buffer Pool Writes','DB No. Fysnc Log Writes'};
                    legends{end+1} = horzcat(server_name, ' DB No. Physical Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB No. Data Writes');
                    legends{end+1} = horzcat(server_name, ' DB Double Writes Operations');
                    legends{end+1} = horzcat(server_name, ' DB No. Log Write Requests');
                    legends{end+1} = horzcat(server_name, ' DB Buffer Pool Writes');
                    legends{end+1} = horzcat(server_name, ' DB No. Fysnc Log Writes');
                end
            end
            title = 'Write Requests (#)';
            Xlabel = this.Xlabel;
            Ylabel = 'Number of';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDiskReadMB(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if exist('mv.dbmsPhysicalReadsMB', 'var')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = mv.dbmsPhysicalReadsMB(:,i);
                    legends{end+1} = horzcat(server_name, ' InnoDB Data Read');
                end
                Xdata{end+1} = this.Xdata;
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.osNumberOfSectorReads(:,i);
                Ydata{end+1} = mv.osNumberOfReadsIssued(:,i);
                legends{end+1} = horzcat(server_name, ' Disk Read');
                legends{end+1} = horzcat(server_name, ' IO Read');
            end
            Xlabel = this.Xlabel;
            Ylabel = 'Read data (MB/sec)';
            title = 'Read Volume (MB)';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDiskReadNum(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsNumberOfDataReads')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = [mv.dbmsNumberOfDataReads(:,i) mv.dbmsNumberOfLogicalReadsFromDisk(:,i) mv.dbmsNumberOfPendingReads(:,i)];
                    legends{end+1} = horzcat(server_name, ' DB No. Data Reads');
                    legends{end+1} = horzcat(server_name, ' DB No. Logical Reads From Disk');
                    legends{end+1} = horzcat(server_name, ' DB No. Pending Reads');
                    %legends = {'DB No. Data Reads','DB No. Logical Reads From Disk','DB No. Pending Reads'};
                end
            end
            Xlabel = this.Xlabel;
            Ylabel = 'Number of';
            title = 'Read Requests (#)';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotCacheHit(this)
            mv = this.mv;
            allowedRelativeDiff = 0.1;
            minFreq=100;
            Xdata = {};
            Ydata = {};
            legends = {};
            start_col = 1;

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);

                end_col = start_col + mv.numOfTransType(i) - 1;
                %grouping by total TPS
                if isfield(mv, 'dbmsPhysicalReadsMB')
                    [grouped freq] = GroupByAvg([mv.clientTotalSubmittedTrans mv.clientIndividualSubmittedTrans(:,start_col:end_col) mv.dbmsReads(:,i) mv.dbmsReadRequests(:,i) mv.dbmsPhysicalReadsMB(:,i)], 1, allowedRelativeDiff, minFreq, 10, 1000);
                else
                    [grouped freq] = GroupByAvg([mv.clientTotalSubmittedTrans mv.clientIndividualSubmittedTrans(:,start_col:end_col) mv.dbmsReads(:,i) mv.dbmsReadRequests(:,i)], 1, allowedRelativeDiff, minFreq, 10, 1000);
                end
                end_col = start_col + 1;
                grouped = grouped(:,2:end);
                actualCacheMiss = grouped(:,end-2) ./ grouped(:,end-1);
                x = sum(grouped(:,1:end-3),2);
                ratio = mean(mv.dbmsReads(mv.dbmsReadRequests(:,i)>0, i) ./mv.dbmsReadRequests(mv.dbmsReadRequests(:,i)>0, i));

                Xdata{end+1} = x;
                Ydata{end+1} = actualCacheMiss;

                if isfield(mv, 'dbmsPhysicalReadsMB')
                    %title = ['Avg Read(MB)=' num2str(mean(mv.dbmsPhysicalReadsMB),1) ' Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads),3) '/' num2str(mean(mv.dbmsReadRequests),1) '=' num2str(ratio,3)];
                    legends{end+1} = [server_name ' Avg Read(MB)=' num2str(mean(mv.dbmsPhysicalReadsMB(:,i)),1) ' Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads(:,i)),3) '/' num2str(mean(mv.dbmsReadRequests(:,i)),1) '=' num2str(ratio,3)];
                else
                    %title = ['Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads),3) '/' num2str(mean(mv.dbmsReadRequests),1) '=' num2str(ratio,3)];
                    legends{end+1} = [server_name ' Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads(:,i)),3) '/' num2str(mean(mv.dbmsReadRequests(:,i)),1) '=' num2str(ratio,3)];
                end
            end
            Xlabel = 'TPS';
            Ylabel = 'Actual miss ratio';
            title = 'Cache Miss Ratio';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotRowsChangedOverTime(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsChangedRows')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = [mv.dbmsChangedRows(:,i) mv.dbmsNumberOfRowInsertRequests(:,i)];
                    %legends = {'Rows deleted','Rows updated','Rows inserted','HandlerWrite'};
                    legends{end+1} = horzcat(server_name, ' Rows changed');
                    legends{end+1} = horzcat(server_name, ' No. Row Insert Requests');
                end
            end
            Xlabel = this.Xlabel;
            Ylabel = '# Rows Changed/Insert Requests';
            title = 'Rows Changed';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotRowsChangedPerWriteMB(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsChangedRows')
                    temp = [mv.dbmsChangedRows(:,i) mv.dbmsTotalWritesMB(:,i) mv.dbmsLogWritesMB(:,i) mv.dbmsPageWritesMB(:,i) mv.osNumberOfSectorWrites(:,i)];
                    legends{end+1} = horzcat(server_name, ' DB Total Writes');
                    legends{end+1} = horzcat(server_name, ' DB Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB Page Writes');
                    legends{end+1} = horzcat(server_name, ' OS No. Sector Writes');
                elseif isfield(mv, 'dbmsTotalWritesMB')
                    temp = [mv.dbmsTotalWritesMB(:,i) mv.dbmsLogWritesMB(:,i) mv.dbmsPageWritesMB(:,i) mv.osNumberOfSectorWrites(:,i)];
                    legends{end+1} = horzcat(server_name, ' DB Log Writes');
                    legends{end+1} = horzcat(server_name, ' DB Page Writes');
                    legends{end+1} = horzcat(server_name, ' OS No. Sector Writes');
                else
                    temp = [mv.dbmsLogWritesMB(:,i) mv.dbmsPageWritesMB(:,i) mv.osNumberOfSectorWrites(:,i)];
                    legends{end+1} = horzcat(server_name, ' DB Page Writes');
                    legends{end+1} = horzcat(server_name, ' OS No. Sector Writes');
                end
                temp = sortrows(temp, 1);
                Xdata{end+1} = temp(:,1);
                Ydata{end+1} = temp(:,2:end);
            end
            title = 'Rows changed vs. written data (MB)';
            Xlabel = '# Rows Changed';
            Ylabel = 'Written data (MB)';
            %legends = {'MySQL total IO', 'MySQL log IO', 'MySQL data IO', 'System physical IO'};
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotRowsChangedPerWriteNo(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsNumberOfPhysicalLogWrites')
                    Xdata{end+1} = mv.dbmsChangedRows;
                    %Ydata = {[mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites mv.osAsynchronousIO mv.dbmsNumberOfPendingWrites mv.dbmsNumberOfPendingLogWrites mv.dbmsNumberOfPendingLogFsyncs]};
                    Ydata{end+1} = [mv.dbmsNumberOfPhysicalLogWrites(:,i) mv.dbmsNumberOfDataWrites(:,i) mv.dbmsDoubleWritesOperations(:,i) mv.dbmsNumberOfLogWriteRequests(:,i) mv.dbmsBufferPoolWrites(:,i) mv.dbmsNumberOfFysncLogWrites(:,i) mv.dbmsNumberOfPendingWrites(:,i) mv.dbmsNumberOfPendingLogWrites(:,i) mv.dbmsNumberOfPendingLogFsyncs(:,i)];
                end
                %legends = {'InnodbLogWrites', 'InnodbDataWrites', 'InnodbDblwrWrites', 'InnodbLogWriteRequests', 'InnodbBufferPoolWriteRequests', 'InnodbOsLogFsyncs', 'asyncAio', 'InnodbDataPendingWrites','InnodbOsLogPendingWrites','InnodbOsLogPendingFsyncs'};
                %legends = {'InnodbLogWrites', 'InnodbDataWrites', 'InnodbDblwrWrites', 'InnodbLogWriteRequests', 'InnodbBufferPoolWriteRequests', 'InnodbOsLogFsyncs', 'asyncAio', 'InnodbDataPendingWrites','InnodbOsLogPendingWrites','InnodbOsLogPendingFsyncs'};
                legends{end+1} = horzcat(server_name, ' DB No. Physical Log Writes');
                legends{end+1} = horzcat(server_name, ' DB No. Data Writes');
                legends{end+1} = horzcat(server_name, ' DB Double Writes Operations');
                legends{end+1} = horzcat(server_name, ' DB No. Log Write Requests');
                legends{end+1} = horzcat(server_name, ' DB Buffer Pool Writes');
                legends{end+1} = horzcat(server_name, ' DB No. Fysnc Log Writes');
                legends{end+1} = horzcat(server_name, ' DB No. Pending Writes');
                legends{end+1} = horzcat(server_name, ' DB No. Pending Log Writes');
                legends{end+1} = horzcat(server_name, ' DB No. Pending Log Fsyncs');
            end
            Xlabel = '# Rows Changed';
            Ylabel = 'Number of Writes';
            title = 'Rows Changed vs. # Write Requests';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDirtyPagesPrediction(this)
            % TODO: not done because of hard-coded numbers
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotFlushRate(this)
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
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotNetwork(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = [mv.osNetworkRecvKB(:,i) mv.osNetworkSendKB(:,i)];
                legends{end+1} = horzcat(server_name, ' Network recv(KB)');
                legends{end+1} = horzcat(server_name, ' Network send(KB)');
            end
            Xlabel = this.Xlabel;
            Ylabel = 'KB';
            %legends = {'Network recv(KB)','Network send(KB)'};
            title = 'Network';
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPrediction(this)
            % TODO: not implemented due to hard-coded number in original implementation.
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLockConcurrencyPrediction(this)
            % TODO: not implemented due to hard-coded number in original implementation.
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotBarzanPrediction(this)
            % TODO: not implemented due to hard-coded number in original implementation.
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotDirtyPagesOverTime(this)
            % TODO: handle monitor variable.
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = 'Time';
            Ydata = {dbmsChangedRows dM(:,[Innodb_buffer_pool_pages_flushed]) monitor(:,[Innodb_buffer_pool_pages_data Innodb_buffer_pool_pages_dirty Innodb_buffer_pool_pages_free Innodb_buffer_pool_pages_total ])};
            Ylabel = '# of Pages';
            legends = {'Rows Changed', 'Flushed pages','pages with data','dirty pages','free pages','buffer pool size (in pages)'};
            title = 'Dirty pages over time';
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLockAnalysis(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsCurrentLockWaits')
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = normMatrix([mv.dbmsCurrentLockWaits(:,i) mv.dbmsLockWaits(:,i) mv.dbmsLockWaitTime(:,i)]);
                    %Ydata{end+1} = [mv.dbmsCurrentLockWaits(:,i) mv.dbmsLockWaits(:,i) mv.dbmsLockWaitTime(:,i)];
                    %legends = {'#locks being waited for','#waits, due to locks', 'time spent waiting for locks'};
                    legends{end+1} = horzcat(server_name, ' No. locks being waited for');
                    legends{end+1} = horzcat(server_name, ' No. waits, due to locks');
                    legends{end+1} = horzcat(server_name, ' Time spent waiting for locks');
                end
            end
            Xlabel = this.Xlabel;
            Ylabel = 'Locks (Normalized)';
            %Ylabel = 'Locks';
            title = 'Lock analysis';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotPagingInOut(this)
            % TODO: handle monitor variable.
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            Ydata = {monitor(:,[paging_in paging_out virtual_majpf])};
            Ylabel = 'Memory';
            title = 'Memory Analysis';
            legends = {'paging_in','paging_out','virtual_majpf'};
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotCombinedAvgLatency(this)
            mv = this.mv;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            %Ydata = [mean(mv.clientTransLatency(:,2:end),2) mean(mv.prclat.latenciesPCtile(:,2:end,6), 2)];
            combinedLatency = sum(mv.clientTransLatency .* mv.clientIndividualSubmittedTrans,2)./mv.clientTotalSubmittedTrans;
            combinedLatency(isnan(combinedLatency)) = 0;
            Ydata = {combinedLatency};
            %Xdata{end+1} = this.Xdata;
            %Ydata{end+1} = mean(mv.prclat.latenciesPCtile(:,2:end,6), 2);
            Ylabel = 'Latency (seconds)'; % temp
            legends = {'Average Latency'}; %,'Avg 95 % latency'};
            title = 'Average Latency';
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatency(this)
            % TODO: where does 'clientAvgLatencyA' come from?
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyOverall(this)
            mv = this.mv;
            %AvgLatencyAllLittle = 160 ./ mv.clientTotalSubmittedTrans;
            AvgLatencyAllLittle = mean(sum(mv.clientIndividualSubmittedTrans .* mv.clientTransLatency, 2)) ./ mv.clientTotalSubmittedTrans;
            AcgLatencyAll = sum(mv.clientIndividualSubmittedTrans .* mv.clientTransLatency, 2) ./ mv.clientTotalSubmittedTrans;
			AvgLatencyAllLittle(~isfinite(AvgLatencyAllLittle)) = 0;
			AcgLatencyAll(~isfinite(AcgLatencyAll)) = 0;
            Xdata = {this.Xdata};
            Xlabel = this.Xlabel;
            Ydata = {[AvgLatencyAllLittle AcgLatencyAll]};
            Ylabel = 'Latency (sec)';
            a1= mae(AvgLatencyAllLittle, AcgLatencyAll);
            r1 = mre(AvgLatencyAllLittle, AcgLatencyAll);
            legends = {horzcat('Little"s law MAE=', num2str(a1), ' MRE=', num2str(r1)), 'Actual Average Latency'};
            title = 'Overall Latency';
			timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyVersusCPU(this)
            mv = this.mv;
            Xdata = {};
            Xlabel = 'Average CPU';
            Ydata = {};
            legends = {};
            temp = [];
            start_col = 1;
            for i=1:size(mv.numOfTransType, 2)
                end_col = start_col + mv.numOfTransType(i) - 1;
                min_length = min([size(mv.AvgCpuUser(:, i), 1) size(mv.clientTransLatency,1) size(this.Xdata,1)]);
                temp = [mv.AvgCpuUser(1:min_length,i) mv.clientTransLatency(1:min_length,start_col:end_col) this.Xdata(1:min_length,:)];
                start_col = end_col + 1;
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    %Xdata{end+1} = mean(mv.cpu_usr,2);
                    %Ydata{end+1} = mv.clientTransLatency(:,i);
                    legends{end+1} = horzcat(server_name, ' Type ', num2str(j));
                end
            end
            %temp = [mean(mv.cpu_usr(1:min_length,:),2) mv.clientTransLatency(1:min_length,:) this.Xdata(1:min_length,:)];
            % temp = [mean(mv.cpu_usr,2) mv.clientTransLatency this.Xdata];
            Ylabel = 'latency (sec)';
            title = 'CPU vs Latency';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyVersusCPU99(this)
            mv = this.mv;
            Xdata = {};
            Xlabel = 'Average CPU';
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                latencies = mv.prclat{i}.latenciesPCtile(:,[2:mv.numOfTransType+1],7);
                latencies(isnan(latencies)) = 0;
                min_length = min([size(mv.AvgCpuUser(:,i), 1) size(latencies,1) size(this.Xdata,1)]);
                temp = [mv.AvgCpuUser(1:min_length,i) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
                temp(isnan(temp)) = 0;
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    %Xdata{end+1} = mean(mv.cpu_usr,2);
                    %Ydata{end+1} = mv.clientTransLatency(:,i);
                    legends{end+1} = horzcat(server_name, ' Type ', num2str(j));
                end
            end
            Ylabel = 'latency (sec)';
            title = 'CPU vs Latency (99% Quantile)';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyVersusCPUMedian(this)
            mv = this.mv;
            Xdata = {};
            Xlabel = 'Average CPU';
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                latencies = mv.prclat{i}.latenciesPCtile(:,[2:mv.numOfTransType+1],3);
                latencies(isnan(latencies)) = 0;
                min_length = min([size(mv.AvgCpuUser(:,i), 1) size(latencies,1) size(this.Xdata,1)]);
                temp = [mv.AvgCpuUser(1:min_length,i) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
                temp(isnan(temp)) = 0;
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    %Xdata{end+1} = mean(mv.cpu_usr,2);
                    %Ydata{end+1} = mv.clientTransLatency(:,i);
                    legends{end+1} = horzcat(server_name, ' Type ', num2str(j));
                end
            end

            %latencies = mv.prclat.latenciesPCtile(:,[2:mv.numOfTransType+1],3);
            %latencies(isnan(latencies)) = 0;
            %min_length = min([size(mv.cpu_usr, 1) size(latencies,1) size(this.Xdata,1)]);
            %temp = [mean(mv.cpu_usr(1:min_length,:),2) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
            %% temp = [mean(mv.cpu_usr,2) latencies this.Xdata];
            %temp = sortrows(temp,1);
            %timestamp = temp(:, end);
            %temp = temp(:,1:end-1);
            %for i=1:mv.numOfTransType
                %Xdata{end+1} = temp(:,1);
                %Ydata{end+1} = temp(:,i+1);
                %%Xdata{end+1} = mean(mv.cpu_usr,2);
                %%Ydata{end+1} = mv.clientTransLatency(:,i);
                %legends{end+1} = horzcat('Type ', num2str(i));
            %end
            Ylabel = 'latency (sec)';
            title = 'CPU vs Latency (Median)';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatency3D(this)
            % TODO: 3D support in JFreeChart?
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotWorkingSetSize(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsRandomReadAheads')
                    temp=[ ...
                        mv.dbmsRandomReadAheads(:,i) mv.dbmsSequentialReadAheads(:,i) mv.dbmsNumberOfLogicalReadRequests(:,i) ...
                        mv.dbmsNumberOfLogicalReadsFromDisk(:,i) mv.dbmsNumberOfWaitsForFlush(:,i)];
                    Xdata{end+1} = this.Xdata;
                    Ydata{end+1} = normMatrix(temp);
                    %Ydata{end+1} = temp;
                    legends{end+1} = horzcat(server_name, ' InnodbBufferPoolReadAheadRnd');
                    legends{end+1} = horzcat(server_name, ' InnodbBufferPoolReadAheadSeq');
                    legends{end+1} = horzcat(server_name, ' InnodbBufferPoolReadRequests');
                    legends{end+1} = horzcat(server_name, ' InnodbBufferPoolReads');
                    legends{end+1} = horzcat(server_name, ' InnodbBufferPoolWaitFree');
                    %legends = {'InnodbBufferPoolReadAheadRnd', 'InnodbBufferPoolReadAheadSeq', 'InnodbBufferPoolReadRequests', 'InnodbBufferPoolReads','InnodbBufferPoolWaitFree'};
                end
            end
            Xlabel = this.Xlabel;
            %Ylabel = 'Normalized';
            Ylabel = '# of request/read/waits (normalized)';
            title = 'Working Set Analysis';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotWorkingSetSize2(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};

            for i=1:size(mv.numOfTransType,2)
                server_name = this.getServerName(mv, i);
                if isfield(mv, 'dbmsNumberOfNextRowReadRequests')
                    temp=[mv.dbmsNumberOfFirstEntryReadRequests(:,i) mv.dbmsNumberOfKeyBasedReadRequests(:,i) mv.dbmsNumberOfNextKeyBasedReadRequests(:,i) mv.dbmsNumberOfPrevKeyBasedReadRequests(:,i) mv.dbmsNumberOfRowReadRequests(:,i) mv.dbmsNumberOfNextRowReadRequests(:,i)];
                    Xdata{end+1} = this.Xdata;
                    %Ydata{end+1} = temp;
                    Ydata{end+1} = normMatrix(temp);
                    legends{end+1} = horzcat(server_name, 'Handler_read_first');
                    legends{end+1} = horzcat(server_name, 'Handler_read_key');
                    legends{end+1} = horzcat(server_name, 'Handler_read_next');
                    legends{end+1} = horzcat(server_name, 'Handler_read_prev');
                    legends{end+1} = horzcat(server_name, 'Handler_read_rnd');
                    legends{end+1} = horzcat(server_name, 'Handler_read_rnd_next');
                    %legends = {'Handler_read_first', 'Handler_read_key', 'Handler_read_next', 'Handler_read_prev', 'Handler_read_rnd', 'Handler_read_rnd_next'};
                end
            end
            Xlabel = this.Xlabel;
            %Ylabel = 'Normalized';
            Ylabel = '# of handler reads (normalized)';
            title = 'Working Set Analysis';
            timestamp = this.Xdata;
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPerTPS(this)
            mv = this.mv;
            start_col = 1;
            Xdata = {};
            Xlabel = 'TPS';
            Ydata = {};
            legends = {};
            for i=1:size(mv.numOfTransType,2)
                end_col = start_col + mv.numOfTransType(i) - 1;
                min_length = min([size(mv.clientTotalSubmittedTrans, 1) size(mv.clientTransLatency,1) size(this.Xdata,1)]);
                temp = [mv.clientTotalSubmittedTrans(1:min_length,:) mv.clientTransLatency(1:min_length,start_col:end_col) this.Xdata(1:min_length,:)];
                start_col = end_col + 1;
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    legends{end+1} = horzcat(server_name, ' Avg Latency of Type ', num2str(j));
                end
            end
            title = 'Latency vs. TPS';
            Ylabel = 'Latency (sec)';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPerTPS99(this)
            mv = this.mv;
            Xdata = {};
            Xlabel = 'TPS';
            Ydata = {};
            legends = {};
            for i=1:size(mv.numOfTransType,2)
                latencies = mv.prclat{i}.latenciesPCtile(:,[2:mv.numOfTransType+1],7);
                latencies(isnan(latencies)) = 0;
                min_length = min([size(mv.clientTotalSubmittedTrans, 1) size(latencies,1) size(this.Xdata,1)]);
                temp = [mv.clientTotalSubmittedTrans(1:min_length,:) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    legends{end+1} = horzcat(server_name, ' Avg Latency of Type ', num2str(j));
                end
            end
            title = 'Latency vs. TPS (99% Quantile)';
            Ylabel = 'Latency (sec)';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPerTPSMedian(this)
            mv = this.mv;
            Xdata = {};
            Xlabel = 'TPS';
            Ydata = {};
            legends = {};
            for i=1:size(mv.numOfTransType,2)
                latencies = mv.prclat{i}.latenciesPCtile(:,[2:mv.numOfTransType+1],3);
                latencies(isnan(latencies)) = 0;
                min_length = min([size(mv.clientTotalSubmittedTrans, 1) size(latencies,1) size(this.Xdata,1)]);
                temp = [mv.clientTotalSubmittedTrans(1:min_length,:) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    legends{end+1} = horzcat(server_name, ' Avg Latency of Type ', num2str(j));
                end
            end
			%latencies = mv.prclat.latenciesPCtile(:,[2:mv.numOfTransType+1],3);
			%latencies(isnan(latencies)) = 0;
            %min_length = min([size(mv.clientTotalSubmittedTrans, 1) size(latencies,1) size(this.Xdata,1)]);
            %temp = [mv.clientTotalSubmittedTrans(1:min_length,:) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
            %temp = sortrows(temp,1);
			%timestamp = temp(:, end);
			%temp = temp(:,1:end-1);
            %Xdata = {};
            %Xlabel = 'TPS';
            %Ydata = {};
            %legends = {};
            %for i=1:mv.numOfTransType
                %Xdata{end+1} = temp(:,1);
                %Ydata{end+1} = temp(:,i+1);
                %legends{end+1} = horzcat('Avg Latency of Type ', num2str(i));
            %end
            title = 'Latency vs. TPS (Median)';
            Ylabel = 'Latency (sec)';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPerLocktime(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            start_col = 1;

            for i=1:size(mv.numOfTransType,2)

                end_col = start_col + mv.numOfTransType(i) - 1;
                CurrentRowLockTime=mv.dbmsCurrentLockWaits(:,i);
                min_length = min([size(CurrentRowLockTime, 1) size(mv.clientTransLatency,1) size(this.Xdata,1)]);
                temp = [CurrentRowLockTime(1:min_length,:) mv.clientTransLatency(1:min_length,start_col:end_col) this.Xdata(1:min_length,:)];
                start_col = end_col + 1;
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                %Xdata = {temp(:,1)};
                %Ydata = {temp(:,2:end)};
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    legends{end+1} = horzcat(server_name, ' Avg Latency of Type ', num2str(j));
                end
            end

            Xlabel = 'Row lock time';
            Ylabel = '';
            title = 'Latency vs. Lock time';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPerLocktime99(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            start_col = 1;

            for i=1:size(mv.numOfTransType, 2)
                end_col = start_col + mv.numOfTransType(i) - 1;
                CurrentRowLockTime=mv.dbmsCurrentLockWaits(:,i);
                latencies = mv.prclat{i}.latenciesPCtile(:,[2:mv.numOfTransType+1],7);
                latencies(isnan(latencies)) = 0;

                min_length = min([size(CurrentRowLockTime, 1) size(latencies,1) size(this.Xdata,1)]);
                temp = [CurrentRowLockTime(1:min_length,:) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
                start_col = end_col + 1;

                % temp = [CurrentRowLockTime latencies this.Xdata];
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    legends{end+1} = horzcat(server_name, ' Avg Latency of Type ', num2str(j));
                end
            end
            Xlabel = 'Row lock time';
            Ylabel = '';
            title = 'Latency vs. Lock time (99% Quantile)';
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotLatencyPerLocktimeMedian(this)
            mv = this.mv;
            Xdata = {};
            Ydata = {};
            legends = {};
            start_col = 1;

            for i=1:size(mv.numOfTransType, 2)
                end_col = start_col + mv.numOfTransType(i) - 1;
                CurrentRowLockTime=mv.dbmsCurrentLockWaits(:,i);
                latencies = mv.prclat{i}.latenciesPCtile(:,[2:mv.numOfTransType+1],3);
                latencies(isnan(latencies)) = 0;

                min_length = min([size(CurrentRowLockTime, 1) size(latencies,1) size(this.Xdata,1)]);
                temp = [CurrentRowLockTime(1:min_length,:) latencies(1:min_length,:) this.Xdata(1:min_length,:)];
                start_col = end_col + 1;

                % temp = [CurrentRowLockTime latencies this.Xdata];
                temp = sortrows(temp,1);
                timestamp = temp(:, end);
                temp = temp(:,1:end-1);
                server_name = this.getServerName(mv, i);
                for j=1:mv.numOfTransType(i)
                    Xdata{end+1} = temp(:,1);
                    Ydata{end+1} = temp(:,j+1);
                    legends{end+1} = horzcat(server_name, ' Avg Latency of Type ', num2str(j));
                end
            end
            Xlabel = 'Row lock time';
            Ylabel = '';
            title = 'Latency vs. Lock time (Median)';


            %CurrentRowLockTime=mv.dbmsCurrentLockWaits;

            %latencies = mv.prclat.latenciesPCtile(:,[2:mv.numOfTransType+1],3);
            %latencies(isnan(latencies)) = 0;

            %min_length = min([size(CurrentRowLockTime, 1) size(latencies,1) size(this.Xdata,1)]);
            %temp = [CurrentRowLockTime(1:min_length,:) latencies(1:min_length,:) this.Xdata(1:min_length,:)];

            %temp = sortrows(temp,1);
            %timestamp = temp(:, end);
            %temp = temp(:,1:end-1);
            %Xdata = {temp(:,1)};
            %Xlabel = 'Row lock time';
            %Ydata = {temp(:,2:end)};
            %Ylabel = '';
            %title = 'Latency vs. Lock time (Median)';
            %legends = {};
            %for i=1:mv.numOfTransType
                %legends{end+1} = horzcat('Avg Latency of Type ', num2str(i));
            %end
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotTransactionMix(this)
            mv = this.mv;

            title = 'Transaction Mix';
            %Xdata = {[1:size(mv.clientIndividualSubmittedTrans, 2)]};
            Xdata = {[1:max(mv.numOfTransType)]};
            start_col = 1;
            tx_mix = [];
            for i=1:size(mv.numOfTransType,2)
                end_col = start_col + mv.numOfTransType(i) - 1;
                tx_mix = vertcat(tx_mix, sum(mv.clientIndividualSubmittedTrans(:, start_col:end_col), 1));
                start_col = end_col + 1;
            end
            %Ydata = {sum(mv.clientIndividualSubmittedTrans, 1)};
            Ydata = {sum(tx_mix, 1)};
            Xlabel = '';
            Ylabel = '';
            legends = {};
            timestamp = this.Xdata;
            for i=1:size(mv.numOfTransType,2)
                legends{end+1} = ['Transaction Type ' num2str(i)];
            end
        end

        function [name] = getServerName(this, mv, i)
            if isfield(mv.headers{i}, 'name')
                name = ['(' mv.headers{i}.name ')'];
            else
                name = ['(Server ' num2str(i)  ')'];
            end
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotStrangeFeatures1(this)
            % TODO: do we need this?
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotStrangeFeatures2(this)
            % TODO: do we need this?
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotAllStrangeFeatures(this)
            % TODO: do we need this?
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotInterrupts(this)
            % TODO: handle monitor variable
        end

        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotFlushRatePrediction(this)
            % TODO: has hard-coded numbers...
        end

        %% Plot functions for prediction console
        function [title legends Xdata Ydata Xlabel Ylabel timestamp] = plotForPrediction(this)
            mv = this.mv;
			Xdata = {};
			Ydata = {};
            legends = {};
            for i=1:size(mv.clientIndividualSubmittedTrans, 2)
                Xdata{end+1} = this.Xdata;
                Ydata{end+1} = mv.clientIndividualSubmittedTrans(:,i);
                legends{end+1} = horzcat('# of Type ', num2str(i), ' Transactions');
            end
			Xdata{end+1} = this.Xdata;
			Ydata{end+1} = mv.clientTotalSubmittedTrans;
            legends{end+1} = 'Total client submitted transactions';
            
            % TODO: max throughput part is omitted
            Xlabel = this.Xlabel;
            Ylabel = 'Transactions (tps)';
            title = 'DBMS Transactions';
			timestamp = this.Xdata;
        end
    end % end methods
end
