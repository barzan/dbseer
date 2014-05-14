init_pred_configs;

% set configs
train_configs = [Dt12345_brk_100_0_121_conf];
test_configs = [Dt12345_brk_100_0_121_conf];

echo off;

%For example the following are some choices
plotDesc = {'RowsChangedPerWriteMB','RowsChangedPerWriteNo','LockAnalysis','LatencyA','LatencyB','LatencyOverall','Network','CacheHit'};

dir = 'tpcc4-redo/';
signature = 't12';

startTime = tic;
load_and_plot(dir, signature, plotDesc);
elapsed = toc(startTime);
fprintf(1, 'MEASURE: load_and_plot time = %f\n', elapsed);

for i = 1:length(train_configs)
    train_config = train_configs(i);
    test_config = test_configs(i);
    train_config = rmfield_safe(train_config, 'groupingStrategy');    
    
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config.io_conf, 'workloadName', 'TPCC' ,  'plotX', 'byCounts', 'whichTransToPlot', 1);
    startTime = tic;
    predictionConsole(flushRateJob, test_config, {train_config});
    elapsed = toc(startTime);
    fprintf(1, 'MEASURE: FlushRatePrediction time = %f\n', elapsed);
end

train_config1 = Dt12345_b0_orig_0_2128_conf;
test_config = Dt12345_b1_1_2175_conf;
maxTPJob = struct('taskName', 'MaxThrouputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC');
startTime = tic;
predictionConsole(maxTPJob, test_config, {train_config1});
elapsed = toc(startTime);
fprintf(1, 'MEASURE: MaxThroughputPrediction time = %f\n', elapsed);
