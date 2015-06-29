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

function linfitCPU(tranTypes, testSignature, testStartIdx, testEndIdx, varargin)
overallTime = tic;
header_aligned;

%%%%%%%
cmdLine = horzcat('linfitCPU([', num2str(tranTypes),'],''', testSignature, ''',', num2str(testStartIdx), ',', num2str(testEndIdx)); 
screen_size = get(0, 'ScreenSize');
fh = figure('Name',horzcat(pwd, ' [', num2str(tranTypes),'],', testSignature,',',num2str(testStartIdx),',',num2str(testEndIdx)),...
     'Color',[1 1 1]);
set(fh, 'Position', [0 0 screen_size(3) screen_size(4)]);
fontsize=40; %for paper use 40
linewidth=6.5; %for paper use 6.5

dim1 = 1;
dim2 = 1;


if 1==0
    CountsToCpu=1;
    CountsToIO=1;
    CountsToLatency=1;
    BlownCountsToCpu=0;
    BlownCountsToIO=0;
    CountsWaitTimeToLatency=1;
    IdealFeaturesToLatency=0;
    RealFeaturesToLatency=1;
    FlushRatePrediction=0;
    MaxThrouputPrediction=1;
    LinearPrediction=0;
else
    CountsToCpu=0;
    CountsToIO=0;
    CountsToLatency=0;
    BlownCountsToCpu=0;
    BlownCountsToIO=0;
    CountsWaitTimeToLatency=0;
    IdealFeaturesToLatency=0;
    RealFeaturesToLatency=0;
    FlushRatePrediction=0;
    MaxThrouputPrediction=0;
    LinearPrediction=1;
    PhysicalReadPrediction=0;
end

nextPlot=1;
%%%%%%%%
cpu_usr_indexes = [cpu1_usr cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr]; % cpu9_usr cpu10_usr cpu11_usr cpu12_usr cpu13_usr cpu14_usr cpu15_usr cpu16_usr];

tranTypes = tranTypes + 1;
tranLabels = tranTypes - 1; 

[Mtest Ltest Ctest dMtest] = load3('.', testSignature, testStartIdx, testEndIdx);

remainder = mod(size(varargin,2),3);
if remainder ~= 0
    fprintf('Error: Format: testFile, startIdx, endIdx\n');
    return
end

howManyTrain = size(varargin,2)/3;
if howManyTrain<1
    fprintf('Error! you need at least 1 training data. You enetered %d\n', howManyTrain);
    return
end

tps = sum(Ctest(:,tranTypes),2);
testSummary = horzcat(testSignature,':',num2str(min(tps)),'-',num2str(max(tps)));

M = [];
L = [];
C = [];
dM = [];
trainSummary = '';
for i=1:3:howManyTrain*3
    [Mi Li Ci dMi] = load3('.', varargin{i}, varargin{i+1}, varargin{i+2});
    M = [M; Mi];
    L = [L; Li];
    C = [C; Ci];
    dM = [dM; dMi];
    tps = sum(C(:,tranTypes),2);
    trainSummary = horzcat(trainSummary,',',varargin{i},':',num2str(min(tps)),'-',num2str(max(tps)));
    cmdLine = horzcat(cmdLine,',''',varargin{i},''',', num2str(varargin{i+1}), ',', num2str(varargin{i+2}));
end
cmdLine = horzcat(cmdLine, ');');

%%% Before the grouping!
UGtrainC = C(:,tranTypes);
UGtrainP = CpuUserAvg(M);
UGtrainIO = M(:,dsk_writ);
UGtrainW = dM(:,Innodb_row_lock_time);
UGtrainL = L(:,tranTypes);
UGtrainTPS = sum(UGtrainC,2);
UGtrainRowsChanged = sum(dM(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]),2);
UGtrainPagesFlushed = dM(:, Innodb_buffer_pool_pages_flushed);
    idx = find(UGtrainTPS>0);
    ratios = UGtrainC(idx,:) ./ repmat(UGtrainTPS(idx),1,size(UGtrainC,2));
UGtrainMixture = mean(ratios);

UGtestC = Ctest(:,tranTypes);
UGtestP = CpuUserAvg(Mtest);
UGtestIO = Mtest(:,dsk_writ);
UGtestW = dMtest(:,Innodb_row_lock_time);
UGtestL = Ltest(:,tranTypes);
UGtestTPS = sum(UGtestC,2);
UGtestRowsChanged = sum(dMtest(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]),2);
UGtestPagesFlushed = dMtest(:, Innodb_buffer_pool_pages_flushed);
    idx = find(UGtestTPS>0);
    ratios = UGtestC(idx,:) ./ repmat(UGtestTPS(idx),1,size(UGtestC,2));
UGtestMixture = mean(ratios);

if 1==1
[testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(UGtestTPS);
[trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(UGtrainTPS);
end

%%%%%%%%%%%% Aggregation
allowedRelativeDiff=0.3;
minFreq = 30;
minTPS = 10;
maxTPS= 950;

if 1==1
    load('grouping.mat');
end

%whether to group by the test data
if 1==1
    temp = [Mtest Ltest Ctest dMtest];
    n1 = size(Mtest,2); n2 = size(Ltest, 2); n3 = size(Ctest, 2); n4 = size(dMtest, 2);
    
    %% group by total TPS
    %allowedRelativeDiff=0.01;
    %minFreq = 1000;
    %[ok freqs] = GroupByAvg([sum(Ctest(:,tranTypes),2) temp], 1, allowedRelativeDiff, minFreq, minTPS, maxTPS);
    %temp = ok(:,2:end);
    
    %% group by individual transaction counts
    %
    %use ncluster=9 for brk-100, brk-900 etc
    %use ncluster=7 for brk1
    %use ncluster=8 for wiki-io
    nClusters = 8;
    if 1==0 %group by TPS only!
        %[temp freqs] = BetterGroupByAvg([UGtestTPS temp], 1, nClusters, minFreq, minTPS, maxTPS);
        [temp freqs] = GroupByAvg([UGtestTPS temp], 1, allowedRelativeDiff, minFreq, minTPS, maxTPS);
        temp = temp(:,2:end);
    else
        %[temp freqs] = BetterGroupByAvg(temp, (n1+n2)+tranTypes, nClusters, minFreq, minTPS, maxTPS);
        %allowedRelativeDiff=0.2; minFreq=70;
        [temp freqs] = GroupByAvg(temp, (n1+n2)+tranTypes, allowedRelativeDiff, minFreq, minTPS, maxTPS);
    end
%grouped = zeros(6, size(temp,2));
%grouped(1,:) = mean(temp(2250:3900,:));
%grouped(2,:) = mean(temp(5250:6900,:));
%grouped(3,:) = mean(temp(8250:9900,:));
%grouped(4,:) = mean(temp(11240:12900,:));
%grouped(5,:) = mean(temp(14240:15940,:));
%grouped(6,:) = mean(temp(17240:17730,:));
%temp = grouped;

if 1==0 %wiki-dist-100 and wiki-dist-900
    grouped = zeros(10, size(temp,2));
    grouped(1,:) = mean(temp(1250:2900,:));
    grouped(2,:) = mean(temp(4250:5900,:));
    grouped(3,:) = mean(temp(7250:8900,:));
    grouped(4,:) = mean(temp(10250:11900,:));
    grouped(5,:) = mean(temp(13250:14900,:));
    grouped(6,:) = mean(temp(16250:17900,:));
    grouped(7,:) = mean(temp(19250:20900,:));
    grouped(8,:) = mean(temp(22250:23900,:));
    grouped(9,:) = mean(temp(25250:26900,:));
    grouped(10,:) = mean(temp(28250:29900,:));
    temp = grouped;
end
    
    %% group by number of transaction type 3
    %tranX=1; 
    %[temp freqs] = GroupByAvg(temp, (n1+n2)+tranX, allowedRelativeDiff, minFreq, minTPS, maxTPS);
    
    Mtest = temp(:,1:n1);
    Ltest = temp(:,(n1+1):(n1+n2));
    Ctest = temp(:,(n1+n2+1):(n1+n2+n3));
    dMtest = temp(:,(n1+n2+n3+1):(n1+n2+n3+n4));

    %whether to group by the training data too!
    if 1==0
        temp = [M L C dM];
        n1 = size(M,2); n2 = size(L, 2); n3 = size(C, 2); n4 = size(dM, 2);
        [temp freqs] = GroupByAvg(temp, (n1+n2)+tranTypes, allowedRelativeDiff, minFreq, minTPS, maxTPS);
        M = temp(:,1:n1);
        L = temp(:,(n1+1):(n1+n2));
        C = temp(:,(n1+n2+1):(n1+n2+n3));
        dM = temp(:,(n1+n2+n3+1):(n1+n2+n3+n4));
    end
end

%%%%%%%%%%%% Auxiliary variables

trainC = C(:,tranTypes);
trainP = CpuUserAvg(M);
trainIO = M(:,dsk_writ);
trainW = dM(:,Innodb_row_lock_time);
trainL = L(:,tranTypes);
trainTPS = sum(trainC,2);
trainRowsChanged = sum(dM(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]),2);
trainPagesFlushed = dM(:, Innodb_buffer_pool_pages_flushed);
    idx = find(trainTPS>0);
    ratios = trainC(idx,:) ./ repmat(trainTPS(idx),1,size(trainC,2));
trainMixture = mean(ratios);
trainLogicalReads = dM(:, Innodb_buffer_pool_read_requests);
trainPhysicalReads = dM(:, Innodb_buffer_pool_reads);
trainPhysicalReadsMB = dM(:, Innodb_data_read) / 1024 / 1024; 
trainNetworkSendKB=(M(:,net0_send)+M(:,net1_send)) ./1024;
trainNetworkRecvKB=(M(:,net0_recv)+M(:,net1_recv))./1024;
trainLogIOw=dM(:,Innodb_os_log_written)./1024./1024; %MB


testC = Ctest(:,tranTypes);
testP = CpuUserAvg(Mtest);
testIO = Mtest(:,dsk_writ);
testW = dMtest(:,Innodb_row_lock_time);
testL = Ltest(:,tranTypes);
testTPS = sum(testC,2);
testRowsChanged = sum(dMtest(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]),2);
testPagesFlushed = dMtest(:, Innodb_buffer_pool_pages_flushed);
    idx = find(testTPS>0);
    ratios = testC(idx,:) ./ repmat(testTPS(idx),1,size(testC,2));
testMixture = mean(ratios);
testLogicalReads = dMtest(:, Innodb_buffer_pool_read_requests);
testPhysicalReads = dMtest(:, Innodb_buffer_pool_reads);
testPhysicalReadsMB = dMtest(:, Innodb_data_read) / 1024 / 1024; 
testNetworkSendKB=(Mtest(:,net0_send)+Mtest(:,net1_send)) ./1024;
testNetworkRecvKB=(Mtest(:,net0_recv)+Mtest(:,net1_recv))./1024;    
testLogIOw=dMtest(:,Innodb_os_log_written)./1024./1024; %MB


%%%%%%%%%%%% Linear modeling

modelP = barzanLinSolve(trainP, trainC)

modelIO = barzanLinSolve(trainIO, trainC)

modelL = barzanLinSolve(trainL, trainC)

modelLw = barzanLinSolve(trainL, [trainC trainW])
    
modelRowsChanged = barzanLinSolve(trainRowsChanged, trainC)

modelFlushRate = barzanLinSolve(trainPagesFlushed, trainC);

predictionsP  = barzanLinInvoke(modelP, testC); 
predictionsIO = barzanLinInvoke(modelIO, testC);
predictionsL  = barzanLinInvoke(modelL, testC);
predictionsLw  = barzanLinInvoke(modelLw, [testC testW]);
predictionsRowsChanged = barzanLinInvoke(modelRowsChanged, testC);
predictionsPagesFlushed = barzanLinInvoke(modelFlushRate, testC);

[MRE_p MAE_p rel_diff_p discrete_rel_error_p weka_rel_err_p] = myerr(predictionsP,testP);
MRE_p = MRE_p*100;
[MRE_io MAE_io rel_diff_io discrete_rel_error_io weka_rel_err_io] = myerr(predictionsIO,testIO);
MRE_io = MRE_io * 100;
[MRE_l MAE_l rel_diff discrete_rel_error weka_rel_err] = myerr(predictionsL,testL);
MRE_l = MRE_l * 100;
[MRE_lw MAE_lw rel_diff_lw discrete_rel_error_lw weka_rel_err_lw] = myerr(predictionsLw,testL);
MRE_lw = MRE_lw * 100;
[MRE_rc MAE_rc rel_diff_rc discrete_rel_error_rc weka_rel_err_rc] = myerr(predictionsRowsChanged, testRowsChanged);
MRE_rc = MRE_rc * 100;
%errperf(testP, predictions, 're')

%%%%%%%%%%% BLOWN DATA %%%%%%%%%%%%

range=1:1:size(trainC,2);
combs = combnk(range, 2);
comb1 = combs(:,1);
comb2 = combs(:,2);

blownTrainC = [trainC trainC.*trainC trainC(:, comb1).*trainC(:, comb2)];
blownTestC = [testC testC.*testC testC(:, comb1).*testC(:, comb2)];

blownModelP = barzanLinSolve(trainP, blownTrainC);
blownModelIO = barzanLinSolve(trainIO, blownTrainC);

blownPredictionsP = barzanLinInvoke(blownModelP, blownTestC);
blownPredictionsIO = barzanLinInvoke(blownModelIO, blownTestC); 

blownMAE_p=mean(abs(blownPredictionsP-testP));
blownMRE_p=mean(abs((blownPredictionsP-testP) ./ testP) .* 100);
blownMAE_io=mean(abs(blownPredictionsIO-testIO));
blownMRE_io=mean(abs((blownPredictionsIO-testIO) ./ testIO) .* 100);

%%%%%%%%%% Some handy variables

%%%%%%%%%% Visualization

if CountsToCpu==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    if exist('trainMaxThroughputIdx') %TODO: here I assume that trainP does not have any warmup period in it! (manually)
        idx=1:trainMaxThroughputIdx;
    else
        idx=1:size(trainP,1);
    end
    myModelP = barzanLinSolve(trainP(idx,:), trainC(idx,:));
    
    myCpuPred = barzanLinInvoke(myModelP, testC);
    
    if 1==1
        xValuesTest = testTPS;
        xValuesTrain = trainTPS;
    else
        xValuesTest = testC(:,tranTypes(1)) ./ testTPS;
        xValuesTrain = trainC(:,tranTypes(1)) ./ trainTPS;
    end

temp = [xValuesTest testP predictionsP myCpuPred];
temp = sortrows(temp, 1);
ph1 = plot(temp(:,1), temp(:,2),'bo', temp(:,1), temp(:,3), 'm-', temp(:,1), temp(:,4), 'r-');
hold on;
ph2 = plot(xValuesTrain, trainP, 'gx');

if exist('testMaxThroughput')
    %drawLine('v', 'k-', testMaxThroughput);
end
if exist('trainMaxThroughput')
    %drawLine('v', 'c-', trainMaxThroughput);
end

xlabel('TPS or percentage of first trans');
text(min(xValuesTest),max(predictionsP), horzcat(showErr('LR', predictionsP, testP), showErr('LRnoise', myCpuPred, testP), ...
 'mean(trainTPS)= ',num2str(mean(trainTPS)), ' mean(trainP)=', num2str(mean(trainP)) ));

if exist('testMaxThroughput')
    legend('Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data', 'testBreakPoint', 'trainBreakPoint'); 
else
    legend('Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data', 'trainBreakPoint');
end
    
    title('Linear model: Avg CPU');
    ylabel('Avg CPU (%)');

    %grid on;
    nextPlot=nextPlot+1;
end

if CountsToIO==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(testTPS, [testIO predictionsIO]./1024 ./1024, ':*');
    title('Linear model: Avg Physical Writes');
    legend('actually written', 'predicted written');
    ylabel('written data (MB)');
    text(5,max(predictionsIO)./1024 ./1024, horzcat('MAE(MB)=',num2str(MAE_io./1024 ./1024), ', MRE(%)=', num2str(MRE_io)));
    grid on;
    nextPlot=nextPlot+1;
end

if CountsToLatency==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(testL, '*');
    hold on;
    plot(predictionsL, ':');
    hold off;
    title('Linear model (counts only): Latency');
    %legend('actual latency(*)', 'predicted latency(...)');
    ylabel('time (sec)');
    for i=1:length(MAE_l)
        msg = horzcat('MAE(sec) type ', num2str(tranLabels(i)), '=',num2str(MAE_l(i)), ', MRE(%)=', num2str(MRE_l(i)))
        text(5,(i/2)*max(max(predictionsL)), msg);
    end
    grid on;
    nextPlot=nextPlot+1;
end

if CountsWaitTimeToLatency==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    fprintf(1,'mean(actual latency)=%10.10f\n',mean(testL));
    ok=[testC testL];
    tempActual = [testC(:,1) testL];
    tempPred = [testC(:,1) predictionsLw];
    tempActual = sortrows(tempActual, 1);
    tempPred = sortrows(tempPred, 1);
    plot(tempActual(:,1),tempActual(:,2:end), '*');
    hold on;
    plot(tempPred(:,1),tempPred(:,2:end), ':');
    hold off;
    title('Linear model (counts + waiting time): Latency');
    legend('actual latency(:)', 'predicted latency(-)');
    xlabel(horzcat('# of trans type ',num2str(tranTypes(1)-1))); %,'/',num2str(tranTypes(2)-1)));
    ylabel('time (sec)');
    for i=1:length(MAE_lw)
        msg = horzcat('MAE(sec) type ', num2str(i), '=',num2str(MAE_lw(i)), ', MRE(%)=', num2str(MRE_lw(i)))
        text(5,(i/2)*max(max(predictionsLw)), msg);
    end
    grid on;
    nextPlot=nextPlot+1;
end

if BlownCountsToCpu==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot([testP blownPredictionsP]);
    title('Quadratic model: Avg CPU');
    legend('actual CPU usage', 'predicted CPU');
    ylabel('Avg CPU (%)');
    text(5,max(blownPredictionsP), horzcat('MAE=',num2str(blownMAE_p), ', MRE(%)=', num2str(blownMRE_p)));
    grid on;
    nextPlot=nextPlot+1;
end

if BlownCountsToIO==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot([testIO blownPredictionsIO]./1024 ./1024);
    title('Quadratic model: Avg Physical Writes');
    legend('actually written', 'predicted written');
    ylabel('written data (MB)');
    text(5,max(blownPredictionsIO./1024 ./1024), horzcat('MAE(MB)=',num2str(blownMAE_io./1024 ./1024), ', MRE(%)=', num2str(blownMRE_io)));
    grid on;
    nextPlot=nextPlot+1;
end

if LinearPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    metricName = 'Log Writes (MB)'; %'Network Receive (KB)' ; %'Network Send (KB)' ; %'Logical Reads';
    trainY = trainLogIOw; %trainNetworkRecvKB; %trainLogicalReads;
    testY = testLogIOw; %testNetworkRecvKB; %testLogicalReads;
    
    model = barzanLinSolve(trainY, trainC);
    pred = barzanLinInvoke(model, testC);
           
    err_1 = mre(pred, testY, true);

    [rel_err_1 abs_err_1 rel_diff_1 discrete_rel_error_1 weka_rel_err] = myerr(pred, testY);

    if 1==1
        xValuesTest = testC(:,1)./testTPS;
        xValuesTrain = trainC(:,1)./trainTPS; 
    else
        xValuesTest = testTPS;
        xValuesTrain = trainTPS;
    end
    temp = [xValuesTest testY  pred];
    temp = sortrows(temp, 1);
    ph1 = plot(temp(:,1), temp(:,2), 'bo:', 'MarkerSize',15); hold on;
    ph2 = plot(temp(:,1), temp(:,3), '-', 'Color', [0 0.5 0]);
    ph3 = plot(xValuesTrain, trainY, 'g.');
    title(horzcat('MRE=',num2str(err_1), ' '));
    legend('Actual', 'Predicted', 'Training Data');
    ylabel(metricName);
    xlabel('TPS or Ratio of trans type 1');

    grid on;
    nextPlot=nextPlot+1;    
end

if PhysicalReadPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    cacheMissRate = testLogicalReads ./ testPhysicalReads; 
    normalization = mean(testPhysicalReadsMB) / mean(cacheMissRate);
    
    if 1==0
        xValuesTest = testC(:,1)./testTPS;
    else
        xValuesTest = testTPS;
    end
    temp = [xValuesTest testPhysicalReadsMB cacheMissRate*normalization];
    temp = sortrows(temp, 1);
    ph1 = plot(temp(:,1), temp(:,2), 'ko-'); hold on;
    ph2 = plot(temp(:,1), temp(:,3), 'bs--');    
    title('Physical read volume and cache miss rate');
    legend('Physical Read Volume', 'Cache Miss Rate');
    ylabel('Data Read (MB per sec)');
    xlabel('TPS or Ratio of trans type 1');

    grid on;
    nextPlot=nextPlot+1;        
end


if FlushRatePrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    naiveLinModel = barzanLinSolve(trainPagesFlushed, trainTPS);
    naivePred = barzanLinInvoke(naiveLinModel, testTPS);
    
    betterLinModel = barzanLinSolve(trainPagesFlushed, trainC);
    betterPred = barzanLinInvoke(betterLinModel, testC);
       
    io_conf = [2008080/1.89 1000 2.04];
    io_conf = 1.0e+06 * [1.787598357501278   0.001000000000000   0.000015586694996] + 1.0e+06 * [1.263249408464585   0.001000000000000   0.000001626100026] + 1.0e+06 * [2.564844265633735   0.001000000000000   0.000020000000000];
    io_conf = io_conf / 3;
    io_conf = [214108.8703             1000      1.577432854]; % bestC
    io_conf = [94099 300 119];
    io_conf = [50202 300 150];
    
        bestC100 = [115050 300 27];
        bestC900 = [67950  300 39];
        bestC_100k_io = [94099 300 119];
        io_conf = (bestC100 + bestC900) / 2;
        io_conf = bestC_100k_io;
    load('bestC.mat');
    io_conf = bestC;
    bestPred1 = cfFlushRateApprox(io_conf, testC);
           
    %bestC2 = [2008080   216 1305461];
    %bestPred2 = cfFlushRateApprox(bestC2, testC);
    
    err_1 = mre(naivePred, testPagesFlushed, true);
    err_2 = mre(betterPred, testPagesFlushed, true);
    err_3 = mre(bestPred1, testPagesFlushed, true);
    err_4 = 0; % mre(bestPred2, testPagesFlushed);
    
    [rel_err_1 abs_err_1 rel_diff_1 discrete_rel_error_1 weka_rel_err] = myerr(naivePred, testPagesFlushed);
    [rel_err_2 abs_err_2 rel_diff_2 discrete_rel_error_2 weka_rel_err] = myerr(betterPred, testPagesFlushed);
    [rel_err_3 abs_err_3 rel_diff_3 discrete_rel_error_3 weka_rel_err] = myerr(bestPred1, testPagesFlushed);
    
    temp = [testC(:,1)./testTPS testPagesFlushed naivePred betterPred bestPred1];
    %temp = [testTPS testPagesFlushed naivePred betterPred bestPred1];
    temp = sortrows(temp, 1);
    
    ph1 = plot(temp(:,1), temp(:,2), 'b.-'); hold on;
    ph2 = plot(temp(:,1), temp(:,3), 'ms--');
    ph3 = plot(temp(:,1), temp(:,4), 'k-.');
    ph4 = plot(temp(:,1), temp(:,5), 'gp:'); hold off;
    title(horzcat('Flush rate prediction #test points=', num2str(size(testC,1)),' '));    
    legend('Actual', 'LR', 'LR+classification', 'Our model', 'cf 2');
    ylabel('Average # of page flush per sec');
    xlabel('Ratio of trans type 1');
    text(0.1,max(naivePred), horzcat('MRE(lin TPS)=',num2str(err_1), ...
        ', MRE(lin types)=',num2str(err_2), ', MRE(cf 1)=',num2str(err_3), ', MRE(cf 2)=',num2str(err_4)));
    
    if 1==0
        fid = fopen('../FlushRatePrediction.txt', 'a'); 
        fprintf(fid, '%s\t%.0f\t%s\t%.0f\t', trainSummary, mean(trainTPS(trainTPS>5)), testSummary, mean(testTPS(testTPS>5)));
        fprintf(fid, '%.0f\t%.0f\t%.0f\t%.0f\t%.0f\t%.0f\t%.0f\t%.0f\t%.0f\t%s\t%s\n', ...
            abs_err_1, 100*rel_err_1, 100*discrete_rel_error_1, ...
            abs_err_2, 100*rel_err_2, 100*discrete_rel_error_2, ...
            abs_err_3, 100*rel_err_3, 100*discrete_rel_error_3, ...
            cmdLine, horzcat('io_conf=[', num2str(io_conf),']; allowedRelativeDiff=', num2str(allowedRelativeDiff), '; minFreq=', num2str(minFreq), ';'));
        fclose(fid);
    end
    
    
    grid on;
    nextPlot=nextPlot+1;    
end

if MaxThrouputPrediction==1
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    actualThr = testMaxThroughput;
    realCPU = mean(UGtestP(testMaxThroughputIdx-10:testMaxThroughputIdx+10,:));
    realPageFlushed = mean(UGtestPagesFlushed(testMaxThroughputIdx-10:testMaxThroughputIdx+10,:));
    
    range = (1:15000)';
    maxFlushRate = 1000;
       
    %CPU-based throughput with classification
    cpuC = barzanLinInvoke(modelP, range*testMixture);
    cpuCLThoughput = find(cpuC>88 & cpuC<90, 1, 'last');
    cpuCUThoughput = find(cpuC>98 & cpuC<100, 1, 'last');
    
    %CPU-based without classification
    cpuTModel = barzanLinSolve(trainP, trainTPS);
    cpuT = barzanLinInvoke(cpuTModel, range);
    cpuTLThoughput = find(cpuT>88 & cpuT<90, 1, 'last');
    cpuTUThoughput = find(cpuT>98 & cpuT<100, 1, 'last');
    
    %CPU-based with classification+noise removal %TODO: I need to also
    %automtically remove the first warm-up phase!
    if exist('trainMaxThroughputIdx')
        idx=1:trainMaxThroughputIdx;
    else
        idx=1:size(trainP,1);
    end
    myModelP = barzanLinSolve(trainP(idx,:), trainC(idx,:));
    myCpuC = barzanLinInvoke(myModelP, range*testMixture);
    myCpuCLThoughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
    myCpuCUThoughput = find(myCpuC>98 & myCpuC<100, 1, 'last');
    
    %Our IO-based throughput
        %bestC = [2008080   216 0.8e6]; %wiki100k-io
    bestC_io_old = [2008080/2   maxFlushRate+100 1000]; %tpcc4-redo old ones
    bestC_io_new = [2008080/2   maxFlushRate+100 10]; %tpcc4-redo new ones
    bestC_io_new2 = [2008080/3   maxFlushRate+100 20]; %tpcc4-redo new ones IO-bound
    io_conf = bestC_io_new;
    load('io_conf.mat');    
    myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*testMixture, maxFlushRate, io_conf);

    %Linear IO-based throughput
    linFlushRate = barzanLinInvoke(modelFlushRate, range*testMixture);
    linFlushRateThoughput = find(linFlushRate<maxFlushRate, 1, 'last');
    if isempty(linFlushRateThoughput); linFlushRateThoughput=0; end
    
    %Lock-based throughput
    bestC_lock_old = [0.1250000000/2.2 0.0001000000*1 1*2 0.4*2]; %0.0100000000]; % good for older datasets
    bestC_lock_old2 = [0.1250000000/20 0.0001000000*1 1*2 0.4*2]; %0.0100000000]; % good for older datasets including t35
    %bestC_lock_new = [0.1250000000/1.4 0.0001000000*1 1*2 0.4*2]; %0.0100000000]; % good for -00, -b0-orig, and b1-b4
    bestC_lock_new = [0.1250000000/1.55 0.0001000000*1 1*2 0.4*2]; %0.0100000000]; % good for -00, -b0-orig, and b1-b4
    lock_conf = bestC_lock_new;
    load('lock_conf.mat');
    concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*testMixture, 160, lock_conf);

    %my final prediction
    [myMaxThroughput1 PredReasonIdx1] = min([myCpuCLThoughput myFlushRateThroughput concurrencyThroughput]);
    [myMaxThroughput2 PredReasonIdx2] = min([myCpuCUThoughput myFlushRateThroughput concurrencyThroughput]);
    
    ph1=plot(testTPS);
    ph2=drawLine('h', 'm+', actualThr);
    ph3=drawLine('h', 'ko', cpuCLThoughput);    
    ph4=drawLine('h', 'kx', cpuCUThoughput);    
    ph5=drawLine('h', 'ks', cpuTLThoughput);    
    ph6=drawLine('h', 'kd', cpuTUThoughput);
    ph7=drawLine('h', 'rp', myFlushRateThroughput);
    ph8=drawLine('h', 'b^', linFlushRateThoughput);
    ph9=drawLine('h', 'kv', concurrencyThroughput);

    reasons={'CPU-bound', 'IO-bound', 'Lock-bound'};
    e1 = 100*(cpuTUThoughput-actualThr)/actualThr;
    e2 = 100*(cpuTLThoughput-actualThr)/actualThr;
    e3 = 100*(cpuCUThoughput-actualThr)/actualThr;
    e4 = 100*(cpuCLThoughput-actualThr)/actualThr;
    e5 = 100*(linFlushRateThoughput-actualThr)/actualThr;
    e6 = 100*(myCpuCLThoughput-actualThr)/actualThr;
    e7 = 100*(myFlushRateThroughput-actualThr)/actualThr;
    e8 = 100*(concurrencyThroughput-actualThr)/actualThr;
    e9 = 100*(myMaxThroughput1-actualThr)/actualThr;
    [tempMT realReasonIdx] = min(abs([e6 e7 e8]));
    
    othersMT = [e1 e2 e3 e4 e5];
    minOP = min(othersMT(othersMT>=0)); %if isempty(minOP); minOP=0; end
    maxOP = max(othersMT(othersMT>=0)); %if isempty(maxOP); maxOP=0; end
    minUP = min(abs(othersMT(othersMT<0))); %if isempty(minUP); minUP=0; end
    maxUP = max(abs(othersMT(othersMT<0))); %if isempty(maxUP); maxUP=0; end

    if e9>=0
        minOP = minOP - e9;
        maxOP = maxOP - e9;
    else
        minUP = minUP - abs(e9);
        maxUP = maxUP - abs(e9);
    end
    
    if 1==1
        fid = fopen('maxThroughput.txt', 'a');
        fprintf(fid, '%s\t%.0f~%.0f\t%s\t%.0f~%.0f\t%.0f\t%.0f\t%.0f\t', trainSummary, min(trainTPS),max(trainTPS), testSummary, min(testTPS), max(testTPS), ...
            actualThr, realCPU, realPageFlushed);
        fprintf(fid, '%.0f (%.0f%%)\t%.0f (%.0f%%)\t%.2f (%.0f%%)\t%.2f (%.0f%%)\t%.2f (%.0f%%)\t%.2f (%.0f%%)\t%.2f (%.0f%%)\t%.2f (%.0f%%)\t%.2f (%.0f%%)\t%.2f~%.2f%%\t%.2f~%.2f%%', ...
            cpuTUThoughput, e1, ...
            cpuTLThoughput, e2, ...
            cpuCUThoughput, e3, ...
            cpuCLThoughput, e4, ...
            linFlushRateThoughput, e5, ...
            myCpuCLThoughput, e6, ...
            myFlushRateThroughput, e7, ...
            concurrencyThroughput, e8, ...
            myMaxThroughput1, e9, ...
            minUP, maxUP, minOP, maxOP);
        fprintf(fid, '\t%s\t%s\t%s\t%s\n', char(reasons(realReasonIdx)), char(reasons(PredReasonIdx1)), cmdLine, horzcat('io_conf=[', num2str(io_conf),']; lock_conf=[', num2str(lock_conf),'];'));
        fclose(fid);
    end
    title(horzcat('Max Throughput: realCPU=',num2str(realCPU),' realPF=', num2str(realPageFlushed), ' our maxPF=', num2str(maxFlushRate),' '));
    legend('Original signal', 'Actual MT ', ...
        horzcat('MT based on adjusted LR for CPU+classification, error: ', num2str(100*(cpuCLThoughput-actualThr)/actualThr) ,'%'), ...
        horzcat('MT based on LR for CPU+classification, error: ', num2str(100*(cpuCUThoughput-actualThr)/actualThr), '%'), ...
        horzcat('MT based on adjusted LR for CPU, error: ', num2str(100*(cpuTLThoughput-actualThr)/actualThr), '%'), ...
        horzcat('MT based on LR for CPU, error: ', num2str(100*(cpuTUThoughput-actualThr)/actualThr), '%'), ...
        horzcat('MT based on our flushrate model, error: ', num2str(100*(myFlushRateThroughput-actualThr)/actualThr), '%'), ...
        horzcat('MT based on LR for flushrate, error: ', num2str(100*(linFlushRateThoughput-actualThr)/actualThr), '%'), ...
        horzcat('MT based on our contention model, error: ', num2str(100*(concurrencyThroughput-actualThr)/actualThr), '%'), ...
        'Location', 'SouthEast');

    ylabel(horzcat('TPS my error:', num2str(100*(myMaxThroughput1-actualThr)/actualThr),'% or ',num2str(100*(myMaxThroughput2-actualThr)/actualThr),'% '));
    xlabel(horzcat('Time. train mix=',num2str(trainMixture), ' min=', num2str(min(trainTPS)), ' max=', num2str(max(trainTPS)), ...
        'test mix=',num2str(testMixture), ' min=', num2str(min(testTPS)), ' max=', num2str(max(testTPS)), ' '));
%    text(0.1,max(naivePred), horzcat('MRE(lin TPS)=',num2str(err_1), ...
%        ', MRE(lin types)=',num2str(err_2), ', MRE(cf 1)=',num2str(err_3), ', MRE(cf 2)=',num2str(err_4)));
    grid on;
    nextPlot=nextPlot+1;
end



%%%%%%%%%%%% More modeling + producing Weka files


%%% Initializing the Train features
Tr_rowsChanged = sum(diff(M(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]))')';
Tr_pagesFlushed = diff(M(:, Innodb_buffer_pool_pages_flushed));
Tr_currentPagesDirty = M(2:end, Innodb_buffer_pool_pages_dirty);
Tr_pagesDirtied = diff(M(:, Innodb_buffer_pool_pages_dirty));
Tr_pagesWithData = M(2:end, Innodb_buffer_pool_pages_data);
Tr_pagesFree = M(2:end, Innodb_buffer_pool_pages_free);
Tr_pagesTotal = M(2:end, Innodb_buffer_pool_pages_total); 

Tr_mysqlTotalIOw=diff(M(:,Innodb_data_written))./1024./1024; %MB
Tr_mysqlLogIOw=diff(M(:,Innodb_os_log_written))./1024./1024; %MB
Tr_mysqlPagesWrittenMB=diff(M(:,Innodb_pages_written)).*2.*16./1024; % to account for double write buffering
Tr_mysqlPagesDblWrittenMB=diff(M(:,Innodb_dblwr_pages_written)).*2.*16./1024; % to account for double write buffering
Tr_sysPhysicalIOw=M(2:end,dsk_writ)./1024./1024; %MB

Tr_ComCommit=diff(M(:,Com_commit));
Tr_ComRollback=diff(M(:,Com_rollback));
Tr_HandlerRollback=diff(M(:,Handler_rollback));

[Tr_AvgCpuUser Tr_AvgCpuSys Tr_AvgCpuIdle] = CpuUserAvg(M(2:end,:));
Tr_CoreVariance = var(M(2:end,cpu_usr_indexes)')';

Tr_NetworkSendKB=(M(2:end,net0_send)+M(2:end,net1_send))./1024;
Tr_NetworkRecvKB=(M(2:end,net0_recv)+M(2:end,net1_recv))./1024;

Tr_ContextSwitches = M(2:end,csw); % No need for a diff as dstat has already done the diff for us!

Tr_LockWaitTimes = diff(M(:, Innodb_row_lock_time));

Tr_trainC = trainC(2:end,:);
Tr_blownTrainC = blownTrainC(2:end,length(tranLabels)+1:end);
Tr_trainL = trainL(2:end,:);

%%% Initializing the Test features
Ts_rowsChanged = sum(diff(Mtest(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]))')';
Ts_pagesFlushed = diff(Mtest(:, Innodb_buffer_pool_pages_flushed));
Ts_currentPagesDirty = Mtest(2:end, Innodb_buffer_pool_pages_dirty);
Ts_pagesDirtied = diff(Mtest(:, Innodb_buffer_pool_pages_dirty));
Ts_pagesWithData = Mtest(2:end, Innodb_buffer_pool_pages_data);
Ts_pagesFree = Mtest(2:end, Innodb_buffer_pool_pages_free);
Ts_pagesTotal = Mtest(2:end, Innodb_buffer_pool_pages_total); 

Ts_mysqlTotalIOw=diff(Mtest(:,Innodb_data_written))./1024./1024; %MB
Ts_mysqlLogIOw=diff(Mtest(:,Innodb_os_log_written))./1024./1024; %MB
Ts_mysqlPagesWrittenMB=diff(Mtest(:,Innodb_pages_written)).*2.*16./1024; % to account for double write buffering
Ts_mysqlPagesDblWrittenMB=diff(Mtest(:,Innodb_dblwr_pages_written)).*2.*16./1024; % to account for double write buffering
Ts_sysPhysicalIOw=Mtest(2:end,dsk_writ)./1024./1024; %MB

Ts_ComCommit=diff(Mtest(:,Com_commit));
Ts_ComRollback=diff(Mtest(:,Com_rollback));
Ts_HandlerRollback=diff(Mtest(:,Handler_rollback));

[Ts_AvgCpuUser Ts_AvgCpuSys Ts_AvgCpuIdle] = CpuUserAvg(Mtest(2:end,:));
Ts_CoreVariance = var(Mtest(2:end,cpu_usr_indexes)')';

Ts_NetworkSendKB=(Mtest(2:end,net0_send)+Mtest(2:end,net1_send))./1024;
Ts_NetworkRecvKB=(Mtest(2:end,net0_recv)+Mtest(2:end,net1_recv))./1024;

Ts_ContextSwitches = Mtest(2:end,csw); % No need for a diff as dstat has already done the diff for us!

Ts_LockWaitTimes = diff(Mtest(:, Innodb_row_lock_time));

Ts_testC = testC(2:end,:);
Ts_blownTestC = blownTestC(2:end,length(tranLabels)+1:end);
Ts_testL = testL(2:end,:);

%%% Producing the variable length header names for counts, blownCounts and
%%% latency. also producing the signature of the Weka files
countHeads=horzcat('C', num2str(tranLabels(1)));
blownCountHeads=horzcat('C', num2str(tranLabels(1)),'-',num2str(tranLabels(1)));
latencyHeads=horzcat('L', num2str(tranLabels(1)));
RHeads='R1,R2,R3,R4,R5';
PconHeads='Pcon1,Pcon2,Pcon3,Pcon4,Pcon5';
totalWaitsHeads='totalWaits1,totalWaits2,totalWaits3,totalWaits4,totalWaits5';
timeSpentWaitingHeads='timeSpentWaiting1,timeSpentWaiting2,timeSpentWaiting3,timeSpentWaiting4,timeSpentWaiting5';
sign=num2str(tranLabels(1));
for i=2:length(tranLabels)
    countHeads = horzcat(countHeads, ',C', num2str(tranLabels(i)));
    blownCountHeads = horzcat(blownCountHeads, ',C', num2str(tranLabels(i)), '-',num2str(tranLabels(i)));
    latencyHeads = horzcat(latencyHeads, ',L', num2str(tranLabels(i)));
    sign = horzcat(sign, '-', num2str(tranLabels(i)));
end

%for i=2:5
%    RHeads = horzcat(RHeads, ',R', num2str(tranLabels(i)));
%    PconHeads = horzcat(PconHeads, ',Pcon', num2str(tranLabels(i)));
%    totalWaitsHeads = horzcat(totalWaitsHeads, ',totalWaits', num2str(tranLabels(i)));
%    timeSpentWaitingHeads = horzcat(timeSpentWaitingHeads, ',timeSpentWaiting', num2str(tranLabels(i)));    
%end

for i=1:size(combs,1)
    blownCountHeads = horzcat(blownCountHeads,',C', num2str(tranLabels(combs(i,1))), '-', num2str(tranLabels(combs(i,2))) );
end

%%%%%%%% Choosing the features!
%all models
blownCounts=1;

%Ideal Model
AvgCpuUser=1;
AvgCpuSys=1;
AvgCpuIdle=1;
rowsChanged=1;
pagesFlushed=1;
currentPagesDirty=1;
pagesDirtied=1;
pagesWithData=1;
pagesFree=1;
pagesTotal=1;
mysqlTotalIOw=1;
mysqlLogIOw=1;
mysqlPagesWrittenMB=1;
mysqlPagesDblWrittenMB=1;
sysPhysicalIOw=1;
ComCommit=1;
ComRollback=1;
HandlerRollback=1;
CoreVariance=1;
NetworkSendKB=1;
NetworkRecvKB=1;
ContextSwitches=1;
LockWaitTimes=1;
Latency=0;

%Realistic model
estR=1;
estT_total=1;
estM_total=1;
estVp=1;
estV=1;
estW=1;
estPcon=1;
esttotalWaits=1;
estTimeSpentWaiting=1;

%%%%%%%% Producing the ideal set based on user's input
testIdealFeatures=Ts_testC;
trainIdealFeatures=Tr_trainC;
IdealHead=countHeads;

testRealFeatures=Ts_testC;
trainRealFeatures=Tr_trainC;
RealHead=countHeads;

if blownCounts==1
	testIdealFeatures=[testIdealFeatures Ts_blownTestC];
	trainIdealFeatures=[trainIdealFeatures Tr_blownTrainC];
	IdealHead=horzcat(IdealHead,',',blownCountHeads);
    
    testRealFeatures=[testRealFeatures Ts_blownTestC];
	trainRealFeatures=[trainRealFeatures Tr_blownTrainC];
	RealHead=horzcat(RealHead,',',blownCountHeads);    
end
if AvgCpuUser==1
	testIdealFeatures=[testIdealFeatures Ts_AvgCpuUser];
	trainIdealFeatures=[trainIdealFeatures Tr_AvgCpuUser];
	IdealHead=horzcat(IdealHead,',AvgCpuUser');
end
if AvgCpuSys==1
	testIdealFeatures=[testIdealFeatures Ts_AvgCpuSys];
	trainIdealFeatures=[trainIdealFeatures Tr_AvgCpuSys];
	IdealHead=horzcat(IdealHead,',AvgCpuSys');
end
if AvgCpuIdle==1
	testIdealFeatures=[testIdealFeatures Ts_AvgCpuIdle];
	trainIdealFeatures=[trainIdealFeatures Tr_AvgCpuIdle];
	IdealHead=horzcat(IdealHead,',AvgCpuIdle');
end
if rowsChanged==1
	testIdealFeatures=[testIdealFeatures Ts_rowsChanged];
	trainIdealFeatures=[trainIdealFeatures Tr_rowsChanged];
	IdealHead=horzcat(IdealHead,',rowsChanged');
    
    %TempReal
    testRealFeatures=[testRealFeatures Ts_rowsChanged];
    trainRealFeatures=[trainRealFeatures Tr_rowsChanged];
    RealHead=horzcat(RealHead,',rowsChanged');
end
if pagesFlushed==1
	testIdealFeatures=[testIdealFeatures Ts_pagesFlushed];
	trainIdealFeatures=[trainIdealFeatures Tr_pagesFlushed];
	IdealHead=horzcat(IdealHead,',pagesFlushed');
end
if currentPagesDirty==1
	testIdealFeatures=[testIdealFeatures Ts_currentPagesDirty];
	trainIdealFeatures=[trainIdealFeatures Tr_currentPagesDirty];
	IdealHead=horzcat(IdealHead,',currentPagesDirty');
    
    %TempReal
	testRealFeatures=[testRealFeatures Ts_currentPagesDirty];
	trainRealFeatures=[trainRealFeatures Tr_currentPagesDirty];
	RealHead=horzcat(RealHead,',currentPagesDirty');    
end
if pagesDirtied==1
	testIdealFeatures=[testIdealFeatures Ts_pagesDirtied];
	trainIdealFeatures=[trainIdealFeatures Tr_pagesDirtied];
	IdealHead=horzcat(IdealHead,',pagesDirtied');
end
if pagesWithData==1
	testIdealFeatures=[testIdealFeatures Ts_pagesWithData];
	trainIdealFeatures=[trainIdealFeatures Tr_pagesWithData];
	IdealHead=horzcat(IdealHead,',pagesWithData');
end
if pagesFree==1
	testIdealFeatures=[testIdealFeatures Ts_pagesFree];
	trainIdealFeatures=[trainIdealFeatures Tr_pagesFree];
	IdealHead=horzcat(IdealHead,',pagesFree');
end
if pagesTotal==1
	testIdealFeatures=[testIdealFeatures Ts_pagesTotal];
	trainIdealFeatures=[trainIdealFeatures Tr_pagesTotal];
	IdealHead=horzcat(IdealHead,',pagesTotal');
end
if mysqlTotalIOw==1
	testIdealFeatures=[testIdealFeatures Ts_mysqlTotalIOw];
	trainIdealFeatures=[trainIdealFeatures Tr_mysqlTotalIOw];
	IdealHead=horzcat(IdealHead,',mysqlTotalIOw');
end
if mysqlLogIOw==1
	testIdealFeatures=[testIdealFeatures Ts_mysqlLogIOw];
	trainIdealFeatures=[trainIdealFeatures Tr_mysqlLogIOw];
	IdealHead=horzcat(IdealHead,',mysqlLogIOw');
end
if mysqlPagesWrittenMB==1
	testIdealFeatures=[testIdealFeatures Ts_mysqlPagesWrittenMB];
	trainIdealFeatures=[trainIdealFeatures Tr_mysqlPagesWrittenMB];
	IdealHead=horzcat(IdealHead,',mysqlPagesWrittenMB');
end
if mysqlPagesDblWrittenMB==1
	testIdealFeatures=[testIdealFeatures Ts_mysqlPagesDblWrittenMB];
	trainIdealFeatures=[trainIdealFeatures Tr_mysqlPagesDblWrittenMB];
	IdealHead=horzcat(IdealHead,',mysqlPagesDblWrittenMB');
    
    %TempReal
    testRealFeatures=[testRealFeatures Ts_mysqlPagesDblWrittenMB];
    trainRealFeatures=[trainRealFeatures Tr_mysqlPagesDblWrittenMB];
    RealHead=horzcat(RealHead,',mysqlPagesDblWrittenMB');
end
if sysPhysicalIOw==1
	testIdealFeatures=[testIdealFeatures Ts_sysPhysicalIOw];
	trainIdealFeatures=[trainIdealFeatures Tr_sysPhysicalIOw];
	IdealHead=horzcat(IdealHead,',sysPhysicalIOw');
end
if ComCommit==1
	testIdealFeatures=[testIdealFeatures Ts_ComCommit];
	trainIdealFeatures=[trainIdealFeatures Tr_ComCommit];
	IdealHead=horzcat(IdealHead,',ComCommit');
end
if ComRollback==1
	testIdealFeatures=[testIdealFeatures Ts_ComRollback];
	trainIdealFeatures=[trainIdealFeatures Tr_ComRollback];
	IdealHead=horzcat(IdealHead,',ComRollback');
    
    %TempReal
    testRealFeatures=[testRealFeatures Ts_ComRollback];
    trainRealFeatures=[trainRealFeatures Tr_ComRollback];
	RealHead=horzcat(RealHead,',ComRollback');    
end
if HandlerRollback==1
	testIdealFeatures=[testIdealFeatures Ts_HandlerRollback];
	trainIdealFeatures=[trainIdealFeatures Tr_HandlerRollback];
	IdealHead=horzcat(IdealHead,',HandlerRollback');
    
    %TempReal
    testRealFeatures=[testRealFeatures Ts_HandlerRollback];
	trainRealFeatures=[trainRealFeatures Tr_HandlerRollback];
	RealHead=horzcat(RealHead,',HandlerRollback');
end
if CoreVariance==1
	testIdealFeatures=[testIdealFeatures Ts_CoreVariance];
	trainIdealFeatures=[trainIdealFeatures Tr_CoreVariance];
	IdealHead=horzcat(IdealHead,',CoreVariance');
end
if NetworkSendKB==1
	testIdealFeatures=[testIdealFeatures Ts_NetworkSendKB];
	trainIdealFeatures=[trainIdealFeatures Tr_NetworkSendKB];
	IdealHead=horzcat(IdealHead,',NetworkSendKB');
end
if NetworkRecvKB==1
	testIdealFeatures=[testIdealFeatures Ts_NetworkRecvKB];
	trainIdealFeatures=[trainIdealFeatures Tr_NetworkRecvKB];
	IdealHead=horzcat(IdealHead,',NetworkRecvKB');
end
if ContextSwitches==1
	testIdealFeatures=[testIdealFeatures Ts_ContextSwitches];
	trainIdealFeatures=[trainIdealFeatures Tr_ContextSwitches];
	IdealHead=horzcat(IdealHead,',ContextSwitches');
    
    %TempReal
	testRealFeatures=[testRealFeatures Ts_ContextSwitches];
	trainRealFeatures=[trainRealFeatures Tr_ContextSwitches];
	RealHead=horzcat(RealHead,',ContextSwitches');
end
if LockWaitTimes==1
	testIdealFeatures=[testIdealFeatures Ts_LockWaitTimes];
	trainIdealFeatures=[trainIdealFeatures Tr_LockWaitTimes];
	IdealHead=horzcat(IdealHead,',LockWaitTimes');
end
if Latency==1
	testIdealFeatures=[testIdealFeatures Ts_testL];
	trainIdealFeatures=[trainIdealFeatures Tr_trainL];
	IdealHead=horzcat(IdealHead,',',latencyHeads);
end

if RealFeaturesToLatency==1
    bestC = [0.1250000000 0.0001000000 1 0.4];
    completeTrainCounts = zeros(size(Tr_trainC,1),5);
    completeTestCounts = zeros(size(Ts_testC,1),5);
    completeTrainCounts(:, tranLabels) = Tr_trainC;
    completeTestCounts(:, tranLabels) = Ts_testC;
    
    [estTrainR estTrainT_total estTrainM_total estTrainVp estTrainV estTrainW estTrainPcon estTraintotalWaits estTrainTimeSpentWaiting] = useLockModel(bestC, completeTrainCounts);
    [estTestR estTestT_total estTestM_total estTestVp estTestV estTestW estTestPcon estTesttotalWaits estTestTimeSpentWaiting] = useLockModel(bestC, completeTestCounts);

    if estR==1   
        testRealFeatures=[testRealFeatures estTestR];        
        trainRealFeatures=[trainRealFeatures estTrainR];        
        RealHead=horzcat(RealHead,',',RHeads);    
    end
    if estT_total==1        
        testRealFeatures=[testRealFeatures estTestT_total];        
        trainRealFeatures=[trainRealFeatures estTrainT_total];        
        RealHead=horzcat(RealHead,',','T_total');
    end
    if estM_total==1        
        testRealFeatures=[testRealFeatures estTestM_total];        
        trainRealFeatures=[trainRealFeatures estTrainM_total];        
        RealHead=horzcat(RealHead,',','M_total');
    end 
    if estVp==1        
        testRealFeatures=[testRealFeatures estTestVp];
        trainRealFeatures=[trainRealFeatures estTrainVp];
        RealHead=horzcat(RealHead,',','Vp1,Vp2,Vp3,Vp4,Vp5,Vp6,Vp7,Vp8,Vp9');
    end
    if estV==1
        testRealFeatures=[testRealFeatures estTestV];
        trainRealFeatures=[trainRealFeatures estTrainV];
        RealHead=horzcat(RealHead,',','V1,V2,V3,V4,V5,V6,V7,V8,V9');
    end
    if estW==1
        testRealFeatures=[testRealFeatures estTestW];
        trainRealFeatures=[trainRealFeatures estTrainW];
        RealHead=horzcat(RealHead,',','W');
    end
    if estPcon==1
        testRealFeatures=[testRealFeatures estTestPcon];
        trainRealFeatures=[trainRealFeatures estTrainPcon];
        RealHead=horzcat(RealHead,',',PconHeads);
    end
    if esttotalWaits==1
        testRealFeatures=[testRealFeatures estTesttotalWaits];
        trainRealFeatures=[trainRealFeatures estTraintotalWaits];
        RealHead=horzcat(RealHead,',',totalWaitsHeads);
    end
    if estTimeSpentWaiting==1
        testRealFeatures=[testRealFeatures estTestTimeSpentWaiting];
        trainRealFeatures=[trainRealFeatures estTrainTimeSpentWaiting];
        RealHead=horzcat(RealHead,',',timeSpentWaitingHeads);
    end
end
%%%%%%%% Modeling
%testIdealFeatures = [Ts_testC Ts_blownTestC Ts_AvgCpuUser Ts_AvgCpuSys Ts_AvgCpuIdle Ts_rowsChanged Ts_pagesFlushed Ts_currentPagesDirty Ts_pagesDirtied Ts_pagesWithData Ts_pagesFree Ts_pagesTotal Ts_mysqlTotalIOw Ts_mysqlLogIOw Ts_mysqlPagesWrittenMB Ts_mysqlPagesDblWrittenMB Ts_sysPhysicalIOw Ts_ComCommit Ts_ComRollback Ts_HandlerRollback Ts_CoreVariance Ts_NetworkSendKB Ts_NetworkRecvKB Ts_ContextSwitches Ts_LockWaitTimes];
%trainIdealFeatures = [Tr_trainC Tr_blownTrainC Tr_AvgCpuUser Tr_AvgCpuSys Tr_AvgCpuIdle Tr_rowsChanged Tr_pagesFlushed Tr_currentPagesDirty Tr_pagesDirtied Tr_pagesWithData Tr_pagesFree Tr_pagesTotal Tr_mysqlTotalIOw Tr_mysqlLogIOw Tr_mysqlPagesWrittenMB Tr_mysqlPagesDblWrittenMB Tr_sysPhysicalIOw Tr_ComCommit Tr_ComRollback Tr_HandlerRollback Tr_CoreVariance Tr_NetworkSendKB Tr_NetworkRecvKB Tr_ContextSwitches Tr_LockWaitTimes];

if IdealFeaturesToLatency==1
    modelIdeal = barzanLinSolve(Tr_trainL, trainIdealFeatures)

    predictionsIdeal  = barzanLinInvoke(modelIdeal, testIdealFeatures);
    MRE_ideal=mre(predictionsIdeal,Ts_testL) * 100;
    MAE_ideal=mae(predictionsIdeal,Ts_testL);

    
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    tempActual = testL;
    tempPred = predictionsIdeal;
    plot(tempActual, '*');
    hold on;
    plot(tempPred, ':');
    hold off;
    title('Linear model (counts + ideal features): Latency');
    legend('actual latency(:)', 'predicted latency(-)');
    xlabel('time');
    ylabel('time (sec)');
    for i=1:length(MAE_lw)
        msg = horzcat('MAE(sec) type ', num2str(tranLabels(i)), '=',num2str(MAE_ideal(i)), ', MRE(%)=', num2str(MRE_ideal(i)))
        text(5,(i/2)*max(max(predictionsIdeal)), msg);
    end
    grid on;
    nextPlot=nextPlot+1;
end
%%%%%%%%%% Producing the Weka files!
if 1==0
    produceWekaFile(horzcat(countHeads,',',latencyHeads),[Tr_trainC Tr_trainL], horzcat('trainCountLatency-',sign,'.csv'));
    produceWekaFile(horzcat(countHeads,',',latencyHeads),[Ts_testC Ts_testL], horzcat('testCountLatency-',sign,'.csv'));
    %produceWekaFile(horzcat(countHeads,',  AvgCpuUser,AvgCpuSys,AvgCpuIdle,ContextSwitches')  ,[Ts_testC Ts_AvgCpuUser Ts_AvgCpuSys Ts_AvgCpuIdle  Ts_ContextSwitches], horzcat('CountCpuCSW-',sign,'.csv'));
    %produceWekaFile(horzcat(countHeads,',  ContextSwitches,',latencyHeads),[Ts_testC Ts_ContextSwitches Ts_testL], horzcat('CountCSWLatency-',sign,'.csv'));
    produceWekaFile(horzcat(IdealHead,',',latencyHeads), [trainIdealFeatures Tr_trainL], horzcat('trainIdeal-',sign,'.csv'));
    produceWekaFile(horzcat(IdealHead,',',latencyHeads), [testIdealFeatures Ts_testL], horzcat('testIdeal-',sign,'.csv'));
end

if RealFeaturesToLatency==1
    produceWekaFile(horzcat(RealHead,',',latencyHeads), [trainRealFeatures Tr_trainL], horzcat('trainReal-',sign,'.csv'));
    produceWekaFile(horzcat(RealHead,',',latencyHeads), [testRealFeatures Ts_testL], horzcat('testReal-',sign,'.csv'));
end

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
if exist('ph7')
    set(ph7, 'LineWidth', linewidth);
end
if exist('ph8')
    set(ph8, 'LineWidth', linewidth);
end
if exist('ph9')
    set(ph9, 'LineWidth', linewidth);
end
if exist('ph10')
    set(ph10, 'LineWidth', linewidth);
end
if exist('ph11')
    set(ph11, 'LineWidth', linewidth);
end

elapsed = toc(overallTime);
fprintf(1,'Overall elapsed time=%f\n', elapsed);

