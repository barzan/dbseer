
init_pred_configs;

% all experiments from previous paper!
if 1==0 
    train_config1 = Dwiki_dist_100_0_107_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy'); 
    test_config = Dwiki_dist_900_0_1074_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    % different than our original!
    
    train_config1 = Dwiki_dist_900_0_1074_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy'); 
    test_config = Dwiki_dist_100_0_107_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dwiki100k_io_0_2044_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');    
    test_config = Dwiki100k_io_0_2044_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byTPS');
    predictionConsole(flushRateJob, test_config, {train_config1});

    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_500_0_784_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_600_0_1215_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_700_0_2217_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_800_0_1484_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_900_0_1498_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    train_config2 = Dt12345_brk_200_0_230_conf;
    train_config2 = rmfield_safe(train_config2, 'groupingStrategy');
    test_config = Dt12345_brk_700_0_2217_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', (train_config1.io_conf+train_config2.io_conf)/2, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1, train_config2});
    
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    train_config2 = Dt12345_brk_200_0_230_conf;
    train_config2 = rmfield_safe(train_config2, 'groupingStrategy');
    test_config = Dt12345_brk_900_0_1498_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', (train_config1.io_conf+train_config2.io_conf)/2, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1, train_config2});
    
    train_config1 = Dt12345_brk_500_0_784_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dt12345_brk_600_0_1215_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_800_0_1484_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_200_0_230_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_900_0_1498_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_200_0_230_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_500_0_784_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_400_0_2248_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
end

if 1==0
%sigmod experiments!
    train_config1 = Dt12345_brk_600_0_1215_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});

    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_900_0_1498_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', false, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_900_0_1498_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_500_0_784_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_400_0_2248_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});

    train_config1 = Dwiki_dist_100_0_107_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy'); 
    test_config = Dwiki_dist_900_0_1074_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    % different than our original!
    
    train_config1 = Dwiki_dist_900_0_1074_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy'); 
    test_config = Dwiki_dist_100_0_107_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dwiki100k_io_0_2044_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');    
    test_config = Dwiki100k_io_0_2044_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byTPS');
    predictionConsole(flushRateJob, test_config, {train_config1});
end

% sampling experiments
    train_config1 = Dt12345_brk_100_0_121_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_100_0_121_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', false, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_900_0_1498_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_900_0_1498_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
        
    train_config1 = Dt12345_brk_400_0_2248_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dt12345_brk_400_0_2248_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});

    train_config1 = Dwiki_dist_100_0_107_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dwiki_dist_100_0_107_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    % different than our original!
    
    train_config1 = Dwiki_dist_900_0_1074_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');
    test_config = Dwiki_dist_900_0_1074_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byCounts', 'whichTransToPlot', 1);
    predictionConsole(flushRateJob, test_config, {train_config1});
    
    train_config1 = Dwiki100k_io_0_2044_conf;
    train_config1 = rmfield_safe(train_config1, 'groupingStrategy');    
    test_config = Dwiki100k_io_0_2044_conf;
    flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'WIKI', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', true, 'plotX', 'byTPS');
    predictionConsole(flushRateJob, test_config, {train_config1});

