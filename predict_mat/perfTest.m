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
