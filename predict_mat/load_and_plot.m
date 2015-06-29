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

function load_and_plot2(data_dir, signature, plotDesc, nextPlot, dim1, dim2)

%plotDesc = {'IndividualCoreUsageUser','IndividualCoreUsageSys','InterCoreStandardDeviation','AvgCpuUsage','TPSCommitRollback','ContextSwitches',
%'DiskWriteMB','DiskWriteMB_friendly','DiskWriteNum','DiskWriteNum_friendly','FlushRate','DiskReadMB','DiskReadNum','RowsChangedOverTime',
%'RowsChangedPerWriteMB','RowsChangedPerWriteNo','LockAnalysis','LatencyA','LatencyB','LatencyOverall','Network','CacheHit',
%'BarzanPrediction','StrangeFeatures1','StrangeFeatures2','AllStrangeFeatures','Interrupts','DirtyPagesPrediction','FlushRatePrediction',
%'LatencyPrediction','LockConcurrencyPrediction','DirtyPagesOverTime','PagingInOut','CombinedAvgLatency','LatencyVersusCPU','Latency3D',
%'workingSetSize','workingSetSize2','LatencyPerTPS','LatencyPerLocktime'};

mv = load_modeling_variables(data_dir, signature);

overallTime = tic;
startSmooth=1; % the offset of where the smooth data begins, FROM tstart
endSmooth= mv.numberOfObservations -1; % the index of where the smooth data ends!

%if monotoneTPS ~= 0
%    Xdata = sum(IndividualCounts(tstart:tend,:)')';
%    xlab = 'TPS';
%else
    Xdata = 1:1:mv.numberOfObservations;  % 1 row skipped from the beginning and 10 rows skipped at the end in load3_offset
    xlab = 'Time (sec)';
%end

if nargin < 6
    dim1=2;
    dim2=4;
end

if nargin < 4
    %screen_size = get(0, 'ScreenSize');
    %fh = figure('Name',signature,'Color',[1 1 1]);
    %set(fh, 'Position', [0 0 screen_size(3) screen_size(4)]);
    nextPlot=1;
end

fontsize = 14; % 14 normal, 40 paper;
linewidth=1; % 1 normal, 6.5 paper;
format('long');


if sum(ismember(plotDesc,'IndividualCoreUsageUser'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:,:), mv.cpu_usr, ':');
    title('Individual core Usr usage');
    xlabel(xlab);
    %ylabel('Individual usr cpu usage per core (%)');
    ylabel('Individual core usr usage');
    legend('Core with mysql');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'IndividualCoreUsageSys'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:,:), mv.cpu_sys, ':');
    title('Individual core Sys usage');
    xlabel(xlab);
    %ylabel('Individual usr cpu usage per core (%)');
    ylabel('Individual core sys usage');
    legend('Core with mysql');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'InterCoreStandardDeviation'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:,:), sqrt(mv.CoreVariance));
    title('Stdev of core usage');
    xlabel(xlab);
    ylabel('Inter-core stdev');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'AvgCpuUsage'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:,:), [mv.AvgCpuUser mv.AvgCpuSys mv.AvgCpuWai mv.AvgCpuHiq mv.AvgCpuSiq], '-.');
    hold on;
    plot(Xdata(:,:), mv.measuredCPU, 'k-.');
    plot(Xdata(:,:), mv.AvgCpuIdle, 'r-.');    
    
    title('Avg Cpu Usage');
    xlabel(xlab);
    ylabel('Avg Cpu Usage (%)');
    legend('Usr', 'Sys', 'AvgCpuWai', 'AvgCpuHiq', 'AvgCpuSiq', 'MySQL Usage', 'Idle');
    
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'TPSCommitRollback'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    ph1 = plot(Xdata(:), mv.clientTotalSubmittedTrans,'kd');
    hold all;
    legend('-DynamicLegend');
    for i=1:size(mv.clientIndividualSubmittedTrans, 2)
        plot(Xdata(:), mv.clientIndividualSubmittedTrans(:,i), nextPlotStyle, 'DisplayName', ['# Transactions ' num2str(i)]);        
    end
    
    if isfield(mv, 'dbmsRollbackHandler')
        plot(Xdata(:), mv.dbmsRollbackHandler, nextPlotStyle, 'DisplayName', 'dbmsRollbackHandler');
    end    
    plot(Xdata(:), mv.dbmsCommittedCommands, nextPlotStyle, 'DisplayName', 'dbmsCommittedCommands');
    plot(Xdata(:), mv.dbmsRolledbackCommands, nextPlotStyle, 'DisplayName', 'dbmsRolledbackCommands');    
    
    [xThroughput yThroughput] = findMaxThroughput(mv.clientTotalSubmittedTrans);
    if ~isempty(yThroughput) 
        ph2 = drawLine('h', 'b-', yThroughput);
        ph3 = drawLine('v', 'm-', xThroughput);
    end
    
    title(horzcat('Max throughput ', num2str(yThroughput), ' TPS at t=', num2str(xThroughput),' '));
    xlabel(xlab);
    
    ylabel('Transactions (tps)');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'ContextSwitches'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata, mv.osNumberOfContextSwitches./1500, nextPlotStyle, 'DisplayName', 'Context Switches (x1500)');
    hold all;
    legend('-DynamicLegend');    
    if isfield(mv, 'dbmsThreadsRunning')
        plot(Xdata, mv.dbmsThreadsRunning, nextPlotStyle, 'DisplayName', 'Threads running');
    end
    title('Threads');
    xlabel(xlab);
    ylabel('# of threads');
    %legend('Context Switches (x1500)','Threads running');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'DiskWriteMB'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    if isfield(mv, 'dbmsTotalWritesMB')
        plot(Xdata, mv.dbmsTotalWritesMB, nextPlotStyle, 'DisplayName', ' DB Total Writes(MB)');
        hold all;
        legend('-DynamicLegend');
    end
    plot(Xdata, mv.dbmsLogWritesMB, nextPlotStyle, 'DisplayName', 'DB Log Writes (MB)');
    hold all;
    legend('-DynamicLegend');
    plot(Xdata, mv.dbmsPageWritesMB, nextPlotStyle, 'DisplayName', 'DB Page Writes (MB) (page=16K)');
    if isfield(mv, 'dbmsDoublePageWritesMB')
        plot(Xdata, mv.dbmsDoublePageWritesMB, nextPlotStyle, 'DisplayName', 'DB Double Page Writes (MB) (half of dirty pages)');
    end
    plot(Xdata, mv.osNumberOfSectorWrites, nextPlotStyle, 'DisplayName', 'OS No. Sector Writes (actual IO)');
    plot(Xdata, mv.osNumberOfWritesCompleted, nextPlotStyle, 'DisplayName', 'OS No. Writes Completed');
    
    %hold on;
    %plot(,'-.');
    title('Write Volume (MB)');
    xlabel(xlab);
    ylabel('Written data (MB/sec)');
    %legend('InnodbDataWritten','Mysql log, i.e. InnodbOsLogWritten','InnodbPagesWritten (*16K)','half of Dirty pages, i.e. InnodbDblwrPagesWritten((*16K))','dskWrit, i.e. actual IO','ioWrit');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'DiskWriteMB_friendly'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    if isfield(mv, 'dbmsDoublePageWritesMB')
        plot(Xdata(:), [mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.dbmsDoublePageWritesMB mv.osNumberOfSectorWrites mv.measuredWritesMB mv.measuredReadsMB], '-.');
    else
        plot(Xdata(:), [mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites mv.measuredWritesMB mv.measuredReadsMB], '-.');
    end
    title('Write Volume (MB)');
    xlabel(xlab);
    ylabel('Write Volume (MB/sec)');
    if isfield(mv, 'dbmsDoublePageWritesMB')
        legend('DB Total Writes)','DB Log Writes','DB Page Writes', 'DB Double Page Writes','OS No. Sector Writes', 'Measured Writes', 'Measured Reads');
    else
        legend('DB Log Writes','DB Page Writes', 'OS No. Sector Writes', 'Measured Writes', 'Measured Reads');
    end
    grid on;
    nextPlot=nextPlot+1;
    
    if isfield(mv, 'dbmsDoublePageWritesMB')
        fprintf(1,'total=%f, log=%f, dataPage=%f, dataDblPages=%f, physical=%f\n',mean(mv.dbmsTotalWritesMB), mean(mv.dbmsLogWritesMB), ...
            mean(mv.dbmsPageWritesMB), mean(mv.dbmsDoublePageWritesMB), mean(mv.osNumberOfSectorWrites));
    else
        fprintf(1,'log=%f, dataPage=%f, physical=%f\n',mean(mv.dbmsLogWritesMB), mean(mv.dbmsPageWritesMB), mean(mv.osNumberOfSectorWrites));
    end
end


if sum(ismember(plotDesc,'DiskWriteNum'))==1
    if isfield(mv, 'dbmsNumberOfPhysicalLogWrites')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        plot(Xdata(:), [mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites mv.osAsynchronousIO],...
            Xdata, [mv.dbmsNumberOfPendingWrites mv.dbmsNumberOfPendingLogWrites mv.dbmsNumberOfPendingLogFsyncs],'-');
        title('Write Requests (#)');
        xlabel(xlab);
        ylabel('Number of');
        legend('DB No. Physical Log Writes','DB No. Data Writes','DB Double Writes Operations','DB No. Log Write Requests','DB Buffer Pool Writes','DB No. Fysnc Log Writes','osAsynchronousIO',...
            'dbmsNumberOfPendingWrites','dbmsNumberOfPendingLogWrites','dbmsNumberOfPendingLogFsyncs');
        grid on;
        nextPlot=nextPlot+1;
    end
end

if sum(ismember(plotDesc,'DiskWriteNum_friendly'))==1 
    if isfield(mv, 'dbmsNumberOfPhysicalLogWrites') && isfield(mv, 'dbmsNumberOfDataWrites') && isfield(mv, 'dbmsDoubleWritesOperations') && isfield(mv, 'dbmsNumberOfLogWriteRequests')...
            && isfield(mv, 'dbmsBufferPoolWrites') && isfield(mv, 'dbmsNumberOfFysncLogWrites')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        plot(Xdata(:), [mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites],'-');
        title('Write Requests (#)');
        xlabel(xlab);
        ylabel('Number of');
        legend('DB No. Physical Log Writes','DB No. Data Writes','DB Double Writes Operations','DB No. Log Write Requests','DB Buffer Pool Writes','DB No. Fysnc Log Writes');
        grid on;
        nextPlot=nextPlot+1;
    end
end

if sum(ismember(plotDesc,'DiskReadMB'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    if exist('mv.dbmsPhysicalReadsMB', 'var')
        plot(Xdata, mv.dbmsPhysicalReadsMB, nextPlotStyle, 'DisplayName', 'InnodbDataRead');
    end
    plot(Xdata, mv.osNumberOfSectorReads, nextPlotStyle, 'DisplayName', 'dskRead');
    plot(Xdata, mv.osNumberOfReadsIssued, nextPlotStyle, 'DisplayName', 'ioRead');
    hold on;
    title('Read Volume (MB)');
    xlabel(xlab);
    ylabel('Read data (MB/sec)');
    grid on;
    nextPlot=nextPlot+1;
end


if sum(ismember(plotDesc,'DiskReadNum'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    if isfield(mv, 'dbmsNumberOfDataReads')
        plot(Xdata(:), [mv.dbmsNumberOfDataReads mv.dbmsNumberOfLogicalReadsFromDisk],...
            Xdata, mv.dbmsNumberOfPendingReads,'-');
        title('Read Requests (#)');
        xlabel(xlab);
        ylabel('Number of');
        legend('DB No. Data Reads','DB No. Logical Reads From Disk','DB No. Pending Reads');
        grid on;
        nextPlot=nextPlot+1;
    end
end

if sum(ismember(plotDesc,'CacheHit'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    allowedRelativeDiff = 0.1;
    minFreq=100;
    if 1==1
        %grouping by total TPS
        if isfield(mv, 'dbmsPhysicalReadsMB')
            [grouped freq] = GroupByAvg([mv.clientTotalSubmittedTrans mv.clientIndividualSubmittedTrans mv.dbmsReads mv.dbmsReadRequests mv.dbmsPhysicalReadsMB], 1, allowedRelativeDiff, minFreq, 10, 1000);
        else
            [grouped freq] = GroupByAvg([mv.clientTotalSubmittedTrans mv.clientIndividualSubmittedTrans mv.dbmsReads mv.dbmsReadRequests], 1, allowedRelativeDiff, minFreq, 10, 1000);
        end
        grouped = grouped(:,2:end);        
        %goupring by individual counts
        %[grouped freq] = GroupByAvg([clientIndSubmittedTrans dbmsReads dbmsReadRequests], 1:size(clientIndSubmittedTrans,2), allowedRelativeDiff, minFreq, 10, 1000);
    else
        nPoints = 1;
        idx = randsample(size(clientIndSubmittedTrans,1), nPoints, false);
        grouped = [clientIndSubmittedTrans(idx,:) dbmsReads(idx,:) dbmsReadRequests(idx,:) ];
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
    ratio = mean(mv.dbmsReads(mv.dbmsReadRequests>0) ./mv.dbmsReadRequests(mv.dbmsReadRequests>0));
    
    if isfield(mv, 'dbmsPhysicalReadsMB')
        title(['Avg Read(MB)=' num2str(mean(mv.dbmsPhysicalReadsMB),1) ' Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads),3) '/' num2str(mean(mv.dbmsReadRequests),1) '=' num2str(ratio,3)]);
    else
        title(['Actual Cache Miss Ratio=', num2str(mean(actualCacheMiss),3) '=' num2str(mean(mv.dbmsReads),3) '/' num2str(mean(mv.dbmsReadRequests),1) '=' num2str(ratio,3)]);
    end
    grid on;
    nextPlot=nextPlot+1;
end


if sum(ismember(plotDesc,'RowsChangedOverTime'))==1
    if isfield(mv, 'dbmsChangedRows')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        plot(Xdata(:), [mv.dbmsChangedRows mv.dbmsNumberOfRowInsertRequests],'-');
        title('Rows changed');
        xlabel(xlab);
        ylabel('# Rows changed');
        legend('Rows deleted','Rows updated','Rows inserted','HandlerWrite');
        grid on;
        nextPlot=nextPlot+1;
    end
end

if sum(ismember(plotDesc,'RowsChangedPerWriteMB'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    if isfield(mv, 'dbmsChangedRows')
        temp = [mv.dbmsChangedRows mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
    elseif isfield(mv, 'dbmsTotalWritesMB')
        temp = [mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
    else
        temp = [mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
    end
    temp = sortrows(temp, 1);
    plot(temp(:,1), temp(:,2:end));    
    title('Rows changed vs. written data (MB)');
    xlabel('# Rows Changed');
    ylabel('Written data (MB)');
    legend('MySQL total IO','MySQL log IO','MySQL data IO','System physical IO');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'RowsChangedPerWriteNo'))==1
    if isfield(mv, 'dbmsNumberOfPhysicalLogWrites')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        plot(mv.dbmsChangedRows,...
            [mv.dbmsNumberOfPhysicalLogWrites mv.dbmsNumberOfDataWrites mv.dbmsDoubleWritesOperations mv.dbmsNumberOfLogWriteRequests mv.dbmsBufferPoolWrites mv.dbmsNumberOfFysncLogWrites mv.osAsynchronousIO], '*',...
            mv.dbmsChangedRows, ...
            [mv.dbmsNumberOfPendingWrites mv.dbmsNumberOfPendingLogWrites mv.dbmsNumberOfPendingLogFsyncs],'.');
        title('Rows changed vs. # write requests');
        xlabel('# Rows Changed');
        ylabel('Number of ');
        legend('InnodbLogWrites','InnodbDataWrites','InnodbDblwrWrites','InnodbLogWriteRequests','InnodbBufferPoolWriteRequests','InnodbOsLogFsyncs','asyncAio',...
            'InnodbDataPendingWrites','InnodbOsLogPendingWrites','InnodbOsLogPendingFsyncs');
        grid on;
        nextPlot=nextPlot+1;
    end
end

if sum(ismember(plotDesc,'DirtyPagesPrediction'))==1
    if isfield(mv, 'dbmsChangedRows')
    
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);

        if 1==0
            trainTime=tic;    
            trainSize = size(mv.dbmsChangedRows,1)/2;        
            D1 = lsqcurvefit(@mapRowsToPages, mean(dbmsDataPages)/8, mv.dbmsCumChangedRows(1:trainSize,:), mv.dbmsCurrentDirtyPages(1:trainSize,:));

            trainData = zeros(trainSize,3);
            trainData(:,1) = mv.dbmsChangedRows(1:trainSize,:);
            trainData(:,2) = mv.dbmsFlushedPages(1:trainSize,:);
            trainData(1,3) = mv.dbmsCurrentDirtyPages(1,1);            
            D2 = lsqcurvefit(@recursiveDirtyPageEstimate, mean(dbmsDataPages)/8, trainData, mv.dbmsCurrentDirtyPages(1:trainSize,:));

            elapsed=toc(trainTime);
            fprintf(1,'Train time=%f\n', elapsed);
        else
            %for t1
            D1 = 158086.769859;
            D2 = 142131;% t1=156919.490116; t12345=142131 []
        end
        fprintf(1,'Best database cardinality estimations1=%f and estimation2=%f\n', D1, D2);

        testTime=tic;
        %predictedDirtyPages1 = mapRowsToPages(D1, mv.dbmsCumChangedRows);

        testData = zeros(size(mv.dbmsChangedRows,1),3);
        testData(:,1) = mv.dbmsChangedRows;
        testData(:,2) = mv.dbmsFlushedPages;
        testData(1,3) = mv.dbmsCurrentDirtyPages(1,1); 
        predictedDirtyPages2 = recursiveDirtyPageEstimate(D2, testData);

        %bestC = [2113090.173030 2000000 1 0.287701]; % t12345
        bestC = [2099659.5012109471 2000000 1 0.2757456055]; %t1

        log_capacity = bestC(1);
        max_log_capacity = bestC(2);
        maxPagesPerSecs = bestC(3); 
        logSizePerTransaction = bestC(4);
        [predictedDirtyPages3 predictedFlushRates]= estimateWriteIO(mv.dbmsCurrentDirtyPages(1,1),D2,log_capacity,max_log_capacity,maxPagesPerSecs,logSizePerTransaction,mv.dbmsChangedRows);
        elapsed=toc(testTime);
        fprintf(1,'Test time=%f\n', elapsed);       

        MAE2=mae(predictedDirtyPages2, dbmsCurrentDirtyPages);
        MRE2=mre(predictedDirtyPages2, dbmsCurrentDirtyPages, true);
        fprintf(1,'Dirty page prediction from actual Flush MAE=%f, MRE=%f\n', MAE2, MRE2);

        MAE3=mae(predictedDirtyPages3, dbmsCurrentDirtyPages);
        MRE3=mre(predictedDirtyPages3, dbmsCurrentDirtyPages, true);
        fprintf(1,'Dirty page prediction from estimated Flush MAE=%f, MRE=%f\n', MAE3, MRE3);

        MAEf=mae(predictedFlushRates, dbmsCurrentDirtyPages);
        MREf=mre(predictedFlushRates, dbmsCurrentDirtyPages, true);
        fprintf(1,'Dirty flush rate from estimated dirty pages MAE=%f, MRE=%f\n', MAEf, MREf);

        temp = [mv.clientTotalSubmittedTrans dbmsCurrentDirtyPages predictedDirtyPages2 predictedDirtyPages3 predictedFlushRates];
        temp = sortrows(temp,1);
        ph1 = plot(temp(:,1), temp(:,2), 'c*');
        hold on;    
        ph2 = plot(temp(:,1), temp(:,3:5), '-');
        %plot(([dbmsFlushedPages dbmsTotalPages dbmsDataPages]), '*');
        %plot(([dbmsDoublePageWritesMB *(1024 /(16*2))*1024 predictedDirtyPages1]), '-');


        title('Dirty page prediction using #rows changed');
        xlabel('TPS');
        ylabel('# of dirty pages');
        legend('Actual DP', 'Predcited DP from actual Flush', 'Predcited DP from estimated Flush', ...
              'Predicted flush rate', 'Actual Page flush rate','Total # pages','# data pages', '# written pages (using written MB)', 'Predicted (old model)');

        grid on;
        nextPlot=nextPlot+1;
    end
end


if sum(ismember(plotDesc,'FlushRate'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    plot(Xdata(:), mv.dbmsFlushedPages,'-');
    hold on;
    range = mv.dbmsFlushedPages(end-100:end,:);
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


if sum(ismember(plotDesc,'Network'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:), [mv.osNetworkRecvKB mv.osNetworkSendKB],'-');
    title('Network');
    xlabel(xlab);
    ylabel('KB');
    legend('Network recv(KB)','Network send(KB)');
    grid on;
    nextPlot=nextPlot+1;  
end

if sum(ismember(plotDesc,'LatencyPrediction'))==1
    hold all;
    legend('-DynamicLegend');
    
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    AllLatencies = zeros(endSmooth-startSmooth+1, 5);
    myCounts = mv.clientIndividualSubmittedTrans;
    AllLatencies = mv.clientTransLatency;
    
    ratio = 0.3;
    trainSt=startSmooth;
    trainEnd=startSmooth+ (endSmooth-startSmooth)*ratio;
    testSt=trainEnd;
    testEnd=endSmooth;    
    testRange=testSt:testEnd;
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
    workingTitle = '';
    for i=1:mv.numOfTransType
       MAE = mae(mv.clientTransLatency(testRange,i), predictedLatencies(testRange,i));
       MRE = mre(mv.clientTransLatency(testRange,i), predictedLatencies(testRange,i)); 
       plot(mv.clientTransLatency(testRange,i), nextPlotStyle, 'DisplayName', ['Actual latency' num2str(i)]);
       plot(predictedLatencies(testRange,i), nextPlotStyle, 'DisplayName', ['Predicted latency' num2str(i)]);
       workingTitle = [workingTitle 'MAE(' num2str(i) ')=' num2str(MAE) ' '];
       workingTitle = [workingTitle 'MRE(' num2str(i) ')=' num2str(MRE) ' '];
    end
    
    title(workingTitle);
    xlabel('TPS');
    ylabel('latency (sec)');
    
    grid on;
    nextPlot=nextPlot+1;    
end

if sum(ismember(plotDesc,'LockConcurrencyPrediction'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    idx = randperm(size(clientTotalSubmittedTrans,1));
    idx = idx(1:30);
    
    [xMax yMax] = findMaxThroughput(clientTotalSubmittedTrans);
    if ~isempty(yMax)
        idx = xMax:(xMax+5);
        range = clientIndSubmittedTrans(idx,:);
    else
        range = clientIndSubmittedTrans(end-5:end,:);
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


if sum(ismember(plotDesc,'BarzanPrediction'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    myCounts = mv.clientIndividualSubmittedTrans;
    lockMetrics = [dbmsCurrentLockWaits(startSmooth:endSmooth,:) dbmsLockWaits(startSmooth:endSmooth,:) dbmsLockWaitTime(startSmooth:endSmooth,:)];
    
    ph1 = plot(clientTotalSubmittedTrans(startSmooth:endSmooth,:), lockMetrics(:,3), 'b*');
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


if sum(ismember(plotDesc,'DirtyPagesOverTime'))==1 
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:), [dbmsChangedRows dM(:,[Innodb_buffer_pool_pages_flushed]) ...
        monitor(:,[Innodb_buffer_pool_pages_data Innodb_buffer_pool_pages_dirty Innodb_buffer_pool_pages_free Innodb_buffer_pool_pages_total ])], '-');
    title('Dirty pages over time');
    xlabel('Time');
    ylabel('# of Pages');
    legend('Rows Changed', 'Flushed pages','pages with data','dirty pages','free pages','buffer pool size (in pages)');
    grid on;
    nextPlot=nextPlot+1;    
end

if sum(ismember(plotDesc,'LockAnalysis'))==1
    
    if isfield(mv, 'dbmsCurrentLockWaits')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);

        %tempPlain = [mv.dbmsCurrentLockWaits];
        %tempDiff = [mv.dbmsLockWaits mv.dbmsLockWaitTime];
            %Table_locks_immediate Table_locks_waited 


        plot(Xdata(:), normMatrix([mv.dbmsCurrentLockWaits mv.dbmsLockWaits mv.dbmsLockWaitTime]),'*');
        title('Lock analysis');
        xlabel(xlab);
        ylabel('Locks (Normalized)');
        legend('#locks being waited for','#waits, due to locks', 'time spent waiting for locks');

        %mean([tempPlain tempDiff])

        grid on;
        nextPlot=nextPlot+1;
    end
end

if sum(ismember(plotDesc,'PagingInOut'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata, monitor(:,[paging_in paging_out virtual_majpf]),'-');
    %mem_buff mem_cach mem_free mem_used 
    title('Memory analysis');
    xlabel(xlab);
    ylabel('Memory');
    legend('paging_in','paging_out','virtual_majpf');
    %'mem_buff','mem_cach','mem_free','mem_used',
    grid on;
    nextPlot=nextPlot+1;
end


if sum(ismember(plotDesc,'CombinedAvgLatency'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata, mean(avglat(:,2:end),2),'b-');
    hold on;
    plot(Xdata, mean(prclat.latenciesPCtile(:,2:end,6), 2),'r-'); % showing 95%tile
    title('latency');
    xlabel(xlab);
    ylabel('latency (sec)');
    legend('Avg latency','Avg 95 % latency');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'Latency'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(Xdata(:), clientAvgLatencyA,'b-');
    hold on;
    
    for i=1:mv.numOfTransType
        plot(Xdata, mv.prclat.latenciesPCtile(:,i+1,6), nextPlotStyle, 'DisplayName', ['95 % Latency ' num2str(i)]); % showing 95%tile
        plot(Xdata, mv.prclat.latenciesPCtile(:,i+1,7), nextPlotStyle, 'DisplayName', ['99 % Latency ' num2str(i)]); % showing 99%tile    
    end
    
    title('latency');
    xlabel(xlab);
    ylabel('Latency (sec)');
    
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'LatencyOverall'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);  
    AvgLatencyAllLittle = 160 ./ mv.clientTotalSubmittedTrans;
    AcgLatencyAll = sum(mv.clientIndividualSubmittedTrans .* mv.clientTransLatency, 2) ./ mv.clientTotalSubmittedTrans;
    plot(Xdata(:), [AvgLatencyAllLittle AcgLatencyAll] ,'*');
    a1= mae(AvgLatencyAllLittle, AcgLatencyAll);
    r1 = mre(AvgLatencyAllLittle, AcgLatencyAll);
    title('Overall latency');
    xlabel(xlab);
    ylabel('Latency (sec)');
    legend(horzcat('Little"s law MAE=', num2str(a1), ' MRE=', num2str(r1)), 'actual avg latency');
    
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'LatencyVersusCPU'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    for i=1:mv.numOfTransType
        plot(mean(mv.cpu_usr, 2), mv.clientTransLatency(:,i), nextPlotStyle, 'DisplayName', ['tran' num2str(i)]);
    end
    hold on;
    
    title('CPU vs Latency');
    xlabel('Average CPU');
    ylabel('latency (sec)');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'Latency3D'))==1 %this is for producing 3D crap!
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot3(IndividualCounts(:,1), IndividualCounts(:,2), avglat(:,1), '-', ...
          IndividualCounts(:,1), IndividualCounts(:,2), avglat(:,2), '-');
    title('latency');
    xlabel('Trans 1');
    ylabel('Trans 2');
    zlabel('latency (sec)');
    legend('latency 1','latency 2');
    grid on;
    nextPlot=nextPlot+1;
    set(gcf,'Color','w');
end

if sum(ismember(plotDesc,'workingSetSize'))==1
    if isfield(mv, 'dbmsRandomReadAheads')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        temp=[ ...
            mv.dbmsRandomReadAheads mv.dbmsSequentialReadAheads mv.dbmsNumberOfLogicalReadRequests ...
            mv.dbmsNumberOfLogicalReadsFromDisk mv.dbmsNumberOfWaitsForFlush];    

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
end

if sum(ismember(plotDesc,'workingSetSize2'))==1
    if isfield(mv, 'dbmsNumberOfNextRowReadRequests')
        subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        temp=[mv.dbmsNumberOfFirstEntryReadRequests mv.dbmsNumberOfKeyBasedReadRequests mv.dbmsNumberOfNextKeyBasedReadRequests mv.dbmsNumberOfPrevKeyBasedReadRequests mv.dbmsNumberOfRowReadRequests mv.dbmsNumberOfNextRowReadRequests];

        plot(normMatrix(temp));
        legend(...
            'Handler_read_first','Handler_read_key','Handler_read_next','Handler_read_prev','Handler_read_rnd','Handler_read_rnd_next');  

        title('Working Set Analysis');
        xlabel(xlab);
        ylabel('?');
        grid on;
        nextPlot=nextPlot+1;
    end
end


if sum(ismember(plotDesc,'LatencyPerTPS'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    temp = [mv.clientTotalSubmittedTrans mv.clientTransLatency];
    temp = sortrows(temp,1);
    for i=1:mv.numOfTransType
        plot(temp(:,1), temp(:,i+1), nextPlotStyle, 'DisplayName', ['avg latency ' num2str(i)])
    end
    
    title('Latency vs TPS');
    xlabel('TPS');
    ylabel('Latency (sec)');
    grid on;
    nextPlot=nextPlot+1;
end

if sum(ismember(plotDesc,'LatencyPerLocktime'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);

    %RowLockTime=mv.dbmsLockWaitTime;
    CurrentRowLockTime=mv.dbmsCurrentLockWaits;
    
    temp = [CurrentRowLockTime mv.clientTransLatency];
    temp = sortrows(temp,1);
    plot(temp(:,1),temp(:,2:end));
        
    legend('avg latency A','avg latency B');    
    title('Latency vs Locktime');
    xlabel('row lock time');
    ylabel('');
    grid on;
    nextPlot=nextPlot+1;
end
    
if sum(ismember(plotDesc,'StrangeFeatures1'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(:, [intr_82 intr_83 intr_84 intr_85 intr_86]);
    %temp=normMatrix(temp);
    plot(Xdata(:), temp,'-');

    title('Streange featurs 1');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('intr_82','intr_83','intr_84','intr_85','intr_86');
    grid on;
    nextPlot=nextPlot+1;
end

    
if sum(ismember(plotDesc,'StrangeFeatures2'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(:, [files inodes]); 
    %temp=normMatrix(temp);
    plot(Xdata(:), temp,'-');

    title('Streange featurs 2');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('files','inodes');
    grid on;
    nextPlot=nextPlot+1;
end

    
if sum(ismember(plotDesc,'AllStrangeFeatures'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(:, [dsk_read dsk_writ io_read io_writ async_aio swap_used swap_free paging_in paging_out virtual_majpf virtual_minpf virtual_alloc virtual_free files inodes intr_19 intr_23 intr_33 intr_79 intr_80 intr_81 intr_82 intr_83 intr_84 intr_85 intr_86 int csw proc_run proc_new sda_util]);
    %temp=normMatrix(temp);
    
    plot(Xdata(:), temp,'-');

    title('Streange featurs!');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('dsk_read','dsk_writ','io_read','io_writ','async_aio','swap_used','swap_free','paging_in','paging_out','virtual_majpf','virtual_minpf','virtual_alloc','virtual_free','files','inodes','intr_19','intr_23','intr_33','intr_79','intr_80','intr_81','intr_82','intr_83','intr_84','intr_85','intr_86','int','csw','proc_run','proc_new','sda_util');
    grid on;
    nextPlot=nextPlot+1;
end

 
if sum(ismember(plotDesc,'Interrupts'))==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    temp = monitor(:, [mv.osCountOfInterruptsServicedSinceBootTime mv.osNumberOfContextSwitches mv.osNumberOfProcessesCurrentlyRunning mv.osNumberOfProcessesCreated mv.osDiskUtilization]);
    %temp=normMatrix(temp);
    plot(Xdata(:), temp,'-');

    title('Interrupts');
    xlabel(xlab);
    ylabel('Transactions (tps)');
    legend('int','csw','proc_run','proc_new','sda_util');
    grid on;
    nextPlot=nextPlot+1;
end


if sum(ismember(plotDesc,'FlushRatePrediction'))==1
    error('to be fixed ...');
    
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);

    allowedRelativeDiff = 0.3;
    minFreq=30;
    test_config = struct('groupParams', struct('nClusters', 15, 'groupByTPSinsteadOfIndivCounts', true, 'minFreq', 10, 'minTPS', 50, 'maxTPS', 1520));
    [groupedPagesFlushed Lg groupedCounts dMg] = applyGroupingPolicy(test_config, mv.dbmsFlushedPages, mv.clientTransLatency, mv.clientTotalSubmittedTrans, dM);
 
    
    if 1==1
        opt = optimset('MaxIter', 400, 'MaxFunEvals', 400, 'TolFun', 0.000000000001, 'DiffMinChange', 1, 'DiffMaxChange', 100);
        bmm = 1:3;
        bestC = lsqcurvefit(@cfFlushRateApproxTPCCWrapper, ...
            [2008080/20 300 50],...
            groupedCounts(bmm,:), groupedPagesFlushed(bmm,:), ...
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
    
    
    %predictedFlushRate = expectedFlushRate(bestC, dbmsCommittedCommands); 
    %MAE = mae(predictedFlushRate, dbmsFlushedPages);
    %MRE = mre(predictedFlushRate, dbmsFlushedPages);
    %fprintf(1, 'FlushRate prediction: Pointwise error:MAE=%f, MRE=%f\n', MAE, MRE);

%    [predictedGroupedFlush dpol] = expectedFlushRate(bestC, groupedCounts);   
%    predictedGroupedFlush = mcFlushRate(bestC, groupedCounts);   
    predictedGroupedFlush = cfFlushRateApproxTPCCWrapper(bestC, groupedCounts);
    MAE = mae(predictedGroupedFlush, groupedPagesFlushed);
    MRE = mre(predictedGroupedFlush, groupedPagesFlushed);
    fprintf(1, 'FlushRate prediction: expected value error:MAE=%f, MRE=%f\n', MAE, MRE);    
    
    %asymp = dpol .* (groupedCounts ./ max_log_capacity) ./ ((groupedCounts ./ max_log_capacity)-1)
    %plot(groupedCounts(:,tranA) ./ sum(groupedCounts,2), [groupedPagesFlushed predictedGroupedFlush],'*');
    plot(sum(groupedCounts,2), [groupedPagesFlushed predictedGroupedFlush],'*');

    
    title(horzcat('Flush rate prediction ', num2str(MRE),'%% '));
    xlabel('Average TPS');
    ylabel('# of pages flushed');
    legend('Actual flush rate', 'Predicted flush rate');

    grid on;
    nextPlot=nextPlot+1;
end

elapsed = toc(overallTime);
fprintf(1,'Total load_and_plot time=%f\n',elapsed);

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
