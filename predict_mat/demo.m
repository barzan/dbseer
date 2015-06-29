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



%% Visualization
%Choose some of the options below
plotDesc = {'IndividualCoreUsageUser','IndividualCoreUsageSys','InterCoreStandardDeviation','AvgCpuUsage','TPSCommitRollback','ContextSwitches', ...
    'DiskWriteMB','DiskWriteMB_friendly','DiskWriteNum','DiskWriteNum_friendly','FlushRate','DiskReadMB','DiskReadNum','RowsChangedOverTime', ... 
    'RowsChangedPerWriteMB','RowsChangedPerWriteNo','LockAnalysis','LatencyA','LatencyB','LatencyOverall','Network','CacheHit', ...
    'BarzanPrediction','StrangeFeatures1','StrangeFeatures2','AllStrangeFeatures','Interrupts','DirtyPagesPrediction','FlushRatePrediction', ... 
    'LatencyPrediction','LockConcurrencyPrediction','DirtyPagesOverTime','PagingInOut','CombinedAvgLatency','LatencyVersusCPU','Latency3D', ...
    'workingSetSize','workingSetSize2','LatencyPerTPS','LatencyPerLocktime'};

%plotDesc = {'IndividualCoreUsageUser','IndividualCoreUsageSys','InterCoreStandardDeviation','AvgCpuUsage'};
%plotDesc = {'DiskWriteMB','DiskWriteMB_friendly','DiskWriteNum','DiskWriteNum_friendly','FlushRate','DiskReadMB','DiskReadNum','RowsChangedOverTime'};

%For example the following are some choices
plotDesc = {'RowsChangedPerWriteMB','RowsChangedPerWriteNo','LockAnalysis','LatencyA','LatencyB','LatencyOverall','Network','CacheHit'};

dir = 'tpcc4-redo/';
signature = 't12';

load_and_plot(dir, signature, plotDesc);
%psql_load_and_plot(signature);


%% Predicting I/O
init_pred_configs;

    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
		flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC' ,  'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});

    train_config1 = Dt12345_brk_600_0_1215_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
		flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC' ,  'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});

    %nb
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_900_0_1498_conf;
		flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC' ,  'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_900_0_1498_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
		flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC' ,  'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});


%% Predicting Maximum Throughput
init_pred_configs;

    train_config1 = Dt12345_b0_orig_0_2128_conf;
    test_config = Dt12345_b1_1_2175_conf;
    maxTPJob = struct('taskName', 'MaxThrouputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC');
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b0_orig_0_615_conf;
    test_config = Dt12345_b0_orig_55_2128_conf;
    maxTPJob = struct('taskName', 'MaxThrouputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC');
    predictionConsole(maxTPJob, test_config, {train_config1});
    



