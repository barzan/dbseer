
init_pred_configs;

    train_config1 = Dt12345_b0_orig_0_615_conf;
    test_config = Dt12345_b0_orig_55_2128_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', false);
    predictionConsole(maxTPJob, test_config, {train_config1});
    
    train_config1 = Dt3_230_4066_conf;
    train_config2 = Dt5_212_4066_conf;
    test_config = Dt35_0_6202_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1, train_config2});

    train_config1 = Dt3_230_4066_conf;
    train_config2 = Dt35_225_4066_conf;
    test_config = Dt5_0_3469_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1, train_config2});

    train_config1 = D256m_t35_1_7628_conf;
    train_config2 = D256m_t3_1_7628_conf;
    test_config = D256m_t5_1_3563_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1, train_config2});
    
    train_config1 = Dt12345_b0_orig_0_2128_conf;
    test_config = Dt12345_b1_1_2175_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b0_orig_0_2128_conf;
    test_config = Dt12345_b2_0_2233_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    
    train_config1 = Dt12345_b0_orig_0_2128_conf;
    test_config = Dt12345_b3_1_2239_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b0_orig_0_2128_conf;
    test_config = Dt12345_b5_1_2057_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b1_1_2175_conf;
    test_config = Dt12345_00_1_2224_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b1_1_451_conf;
    test_config = Dt12345_b1_82_2175_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b1_1_2175_conf;
    test_config = Dt12345_b0_orig_0_2128_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b1_1_2175_conf;
    test_config = Dt12345_b2_0_2233_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b1_1_2175_conf;
    test_config = Dt12345_b3_1_2239_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b1_1_2175_conf;
    test_config = Dt12345_b5_1_2057_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});



    train_config1 = Dt12345_b4_0_1593_conf;
    test_config = Dt12345_00_1_2224_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b4_0_1593_conf;
    test_config = Dt12345_b1_1_2175_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


    train_config1 = Dt12345_b4_0_1593_conf;
    test_config = Dt12345_b2_0_2233_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b4_0_1593_conf;
    test_config = Dt12345_b3_1_2239_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b4_0_1593_conf;
    test_config = Dt12345_b0_orig_0_2128_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});

    train_config1 = Dt12345_b4_0_1593_conf;
    test_config = Dt12345_b5_1_2057_conf;
    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
    predictionConsole(maxTPJob, test_config, {train_config1});


%    train_config1 = ;
%    test_config = ;
%    maxTPJob = struct('taskName', 'MaxThroughputPrediction', 'lock_conf', test_config.lock_conf, 'io_conf', test_config.io_conf, 'workloadName', 'TPCC', 'resultsFile', 'maxThroughput.txt', 'appendToFile', true);
%    predictionConsole(maxTPJob, test_config, {train_config1});

