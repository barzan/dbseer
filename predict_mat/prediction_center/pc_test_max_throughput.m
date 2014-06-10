Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0-orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1549, 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());

train_config_desc = DataProfile;
test_config_desc = DataProfile;

train_config_desc.header_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/t12345_b0_orig_header.m';
train_config_desc.monitor_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/monitor-t12345-b0-orig';
train_config_desc.avg_latency_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/trans-t12345-b0-orig_avg_latency.al';
train_config_desc.percentile_latency_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/trans-t12345-b0-orig_prctile_latencies.mat';
train_config_desc.trans_count_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/trans-t12345-b0-orig_rough_trans_count.al';
train_config_desc.startIdx = 0;

test_config_desc.header_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/t12345_b1_header.m';
test_config_desc.monitor_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/monitor-t12345-b1';
test_config_desc.avg_latency_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/trans-t12345-b1_avg_latency.al';
test_config_desc.percentile_latency_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/trans-t12345-b1_prctile_latencies.mat';
test_config_desc.trans_count_path = '/Users/dyoon/Work/dbseer/example_data/mysql5/tpcc4-redo/trans-t12345-b1_rough_trans_count.al';
test_config_desc.startIdx = 0;

% train_config_desc.setStruct(Dt12345_b0_orig_0_2128_conf);
% test_config_desc.setStruct(Dt12345_b1_1_2175_conf);

train_config_desc.loadStatistics;
test_config_desc.loadStatistics;

task = TaskDescription;
task.workloadName = 'TPCC';
task.taskName = 'MaxThroughputPrediction';
trainConfig = PredictionConfig;
testConfig = PredictionConfig;
trainConfig.addProfile(train_config_desc);
testConfig.addProfile(test_config_desc);
trainConfig.setTransactionType([1 2 3 4 5]);
testConfig.setTransactionType([1 2 3 4 5]);
trainConfig.io_conf = [1004040     1100       10];
testConfig.io_conf =  [1004040     1100       10];
trainConfig.lock_conf = [0.080645      0.0001           2         0.8];
testConfig.lock_conf = [0.080645      0.0001           2         0.8];
trainConfig.initialize;
testConfig.initialize;
pc = PredictionCenter;
pc.taskDescription = task;
pc.testConfig = testConfig;
pc.trainConfig = trainConfig;
res = pc.performPrediction;