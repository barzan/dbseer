

init_pred_configs

if 1==0
    test_config = Dt12345_b0_orig_55_2128_conf;
    test_config.groupParams = struct('minFreq', 30, 'minTPS', 50, 'maxTPS', 2000, 'groupByTPSinsteadOfIndivCounts', true, 'allowedRelativeDiff', 0.1);

    train_config1 =  Dt12345_b0_orig_0_615_conf;
    train_config1.groupParams = struct('minFreq', 30, 'minTPS', 50, 'maxTPS', 2000, 'groupByTPSinsteadOfIndivCounts', true, 'allowedRelativeDiff', 0.1);

    lockJob = struct('taskName', 'LockPrediction', 'learnLock', false, 'lock_conf', [0.125 0.0001 11.3189 0.2762], 'lockType', 'waitTime', 'plotX', 'byTPS', 'workloadName', 'TPCC');
    predictionConsole(lockJob, test_config, {train_config1});
end

if 1==0
    % this works terribly as lock_conf is not a good one
    Dt1_memless_100_500_conf = struct('dir', './tpcc4-memless', 'signature', 't1-memless', 'tranTypes', [1 2 3 4 5], 'startIdx', 2000, 'endIdx', 4600);
    Dt1_memless_600_1000_conf = struct('dir', './tpcc4-memless', 'signature', 't1-memless', 'tranTypes', [1 2 3 4 5], 'startIdx', 4600, 'endIdx', 7000);

    test_config = Dt1_memless_600_1000_conf;
    test_config.groupParams = struct('minFreq', 30, 'minTPS', 50, 'maxTPS', 1100, 'groupByTPSinsteadOfIndivCounts', true, 'allowedRelativeDiff', 0.1);

    train_config1 =  Dt1_memless_100_500_conf;
    train_config1.groupParams = struct('minFreq', 30, 'minTPS', 50, 'maxTPS', 1100, 'groupByTPSinsteadOfIndivCounts', true, 'allowedRelativeDiff', 0.1);

    lock_conf = [0.1250000000/20 0.0001000000*1 1*2 0.4*2];
    lockJob = struct('taskName', 'LockPrediction', 'learnLock', false, 'lock_conf', lock_conf, 'lockType', 'waitTime', 'plotX', 'byTPS', 'workloadName', 'TPCC', 'resultsFile', 'LockPrediction.txt', 'appendToFile', true);
    predictionConsole(lockJob, test_config, {train_config1});
end

% for lock prediction!
groupParams = struct('allowedRelativeDiff', 0.05, 'minFreq', 10, 'minTPS', 10, 'maxTPS', 1000, 'groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', 1);
Dt1_1_conf = struct('dir', 'tpcc4-redo', 'signature', 't1', 'tranTypes', [1 2 3 4 5], 'startIdx', 1300, 'endIdx', 1550, 'groupParams', groupParams);
groupParams = struct('allowedRelativeDiff', 0.05, 'minFreq', 50, 'minTPS', 10, 'maxTPS', 1000, 'groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', 1);
Dt1_2_conf = struct('dir', 'tpcc4-redo', 'signature', 't1', 'tranTypes', [1 2 3 4 5], 'startIdx', 1550, 'endIdx', 2800, 'groupParams', groupParams);

% a what-if scenario
test_config = Dt1_2_conf;
train_config1 = Dt1_1_conf;
train_config1 = rmfield_safe(train_config1, 'groupParams');
lock_conf=[0.125         0.0001      7812.5992       0.546875];
lockJob = struct('taskName', 'LockPrediction', 'learnLock', false, 'lock_conf', lock_conf, 'lockType', 'waitTime', 'plotX', 'byTPS', 'workloadName', 'TPCC', 'resultsFile', 'LockPrediction.txt', 'appendToFile', false, 'emIters', 5);
predictionConsole(lockJob, test_config, {train_config1});


%train on 700, test on 800
test_config = Dt1_2_conf;
test_config.startIdx = test_config.startIdx+300;
train_config1 = Dt1_2_conf;
train_config1 = rmfield_safe(train_config1, 'groupParams');
train_config1.endIdx = train_config1.endIdx-300;
lock_conf=[0.125           0.0001      999984.7412      6.870117188];
lockJob = struct('taskName', 'LockPrediction', 'learnLock', false, 'lock_conf', lock_conf, 'lockType', 'waitTime', 'plotX', 'byTPS', 'workloadName', 'TPCC', 'resultsFile', 'LockPrediction.txt', 'appendToFile', true, 'emIters', 15);
predictionConsole(lockJob, test_config, {train_config1});


% a random-sample case
test_config = Dt1_2_conf;
train_config1 = Dt1_2_conf;
train_config1 = rmfield_safe(train_config1, 'groupParams');
lock_conf=[0.125 0.0001 999984.7412 8.9502 ; ]
lockJob = struct('taskName', 'LockPrediction', 'learnLock', false, 'lock_conf', lock_conf, 'lockType', 'waitTime', 'plotX', 'byTPS', 'workloadName', 'TPCC', 'resultsFile', 'LockPrediction.txt', 'appendToFile', true, 'emIters', 15);
predictionConsole(lockJob, test_config, {train_config1});


