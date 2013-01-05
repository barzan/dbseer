function predictionConsole(taskDesc, test_config, train_configs)
overallTime = tic;
header_aligned;
%%%%%%%

%init_pred_configs

%% initialize the tasks!
%all_tasks = {'CountsToCpu', 'CountsToIO', 'CountsToLatency', 'BlownCountsToCpu', 'BlownCountsToIO', 'CountsWaitTimeToLatency', ...
%    'IdealFeaturesToLatency', 'RealFeaturesToLatency', 'FlushRatePrediction', 'MaxThroughputPrediction', 'LinearPrediction', ...
%    'PhysicalReadPrediction', 'LockPrediction'};

taskDesc.taskName

tranTypes = test_config.tranTypes;
tranLabels = tranTypes;

[Mtest Ltest Ctest dMtest] = load3(test_config.dir, test_config.signature, test_config.startIdx, test_config.endIdx);
%dMtest is the diffed version of Mtest

    cmdLine = ['predictionConsole(' valueToString(taskDesc) ', ' valueToString(test_config) ',' valueToString(train_configs) ');'];
   
    tps = sum(Ctest(:,tranTypes),2);
    testSummary = [test_config.signature ':' num2str(min(tps)) '-' num2str(max(tps))];

M = [];
L = [];
C = [];
dM = [];
trainSummary = '{';
howManyTrain = length(train_configs);
for i=1:howManyTrain
    train_i_conf = train_configs{i};
    [Mi Li Ci dMi] = load3(train_i_conf.dir, train_i_conf.signature, train_i_conf.startIdx, train_i_conf.endIdx);
    M = [M; Mi];
    L = [L; Li];
    C = [C; Ci];
    dM = [dM; dMi];
    tps = sum(C(:,tranTypes),2);
    trainSummary = [trainSummary train_i_conf.signature ':' num2str(min(tps)) '-' num2str(max(tps))];
    if i<length(train_configs)
        trainSummary = [trainSummary ',' ];
    end
end


%% %%%%%%
cpu_usr_indexes = [cpu1_usr cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr]; % cpu9_usr cpu10_usr cpu11_usr cpu12_usr cpu13_usr cpu14_usr cpu15_usr cpu16_usr];


%% Before the grouping!
UGtrainC = C(:,tranTypes);
UGtrainP = CpuUserAvg(M);
UGtrainIO = M(:,dsk_writ);
UGtrainW = dM(:,Innodb_row_lock_time) / 1000; %to turn it into seconds
UGtrainLocksBeingWaitedFor=M(:,Innodb_row_lock_current_waits);
UGtrainNumOfWaitsDueToLocks=dM(:,Innodb_row_lock_waits);
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
UGtestW = dMtest(:,Innodb_row_lock_time) / 1000; %to turn it into seconds
UGtestLocksBeingWaitedFor=Mtest(:,Innodb_row_lock_current_waits);
UGtestNumOfWaitsDueToLocks=dMtest(:,Innodb_row_lock_waits);
UGtestL = Ltest(:,tranTypes);
UGtestTPS = sum(UGtestC,2);
UGtestRowsChanged = sum(dMtest(:,[Innodb_rows_deleted Innodb_rows_updated Innodb_rows_inserted]),2);
UGtestPagesFlushed = dMtest(:, Innodb_buffer_pool_pages_flushed);
    idx = find(UGtestTPS>0);
    ratios = UGtestC(idx,:) ./ repmat(UGtestTPS(idx),1,size(UGtestC,2));
UGtestMixture = mean(ratios);

if strcmp(taskDesc.taskName,'MaxThroughputPrediction')
    [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(UGtestTPS);
    [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(UGtrainTPS);
end

%% whether to group the data
[Mtest Ltest Ctest dMtest] = applyGroupingPolicy(test_config, Mtest, Ltest, Ctest, dMtest);

M = [];
L = [];
C = [];
dM = [];
for i=1:howManyTrain
    train_i_conf = train_configs{i};
    [Mi Li Ci dMi] = load3(train_i_conf.dir, train_i_conf.signature, train_i_conf.startIdx, train_i_conf.endIdx);
    [Mi Li Ci dMi] = applyGroupingPolicy(train_i_conf, Mi, Li, Ci, dMi);
    M = [M; Mi];
    L = [L; Li];
    C = [C; Ci];
    dM = [dM; dMi];
end


%% %%%%%%%%%% Auxiliary variables

trainC = C(:,tranTypes);
trainP = CpuUserAvg(M);
trainIO = M(:,dsk_writ);
trainW = dM(:,Innodb_row_lock_time) / 1000; %to turn it into seconds
trainLocksBeingWaitedFor=M(:,Innodb_row_lock_current_waits);
traintNumOfWaitsDueToLocks=dM(:,Innodb_row_lock_waits);
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
testW = dMtest(:,Innodb_row_lock_time) / 1000; %to turn it into seconds
testLocksBeingWaitedFor=Mtest(:,Innodb_row_lock_current_waits);
testNumOfWaitsDueToLocks=dMtest(:,Innodb_row_lock_waits);
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


%% %%%%%%%%%% Linear modeling

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

%% %%%%%%%%% BLOWN DATA %%%%%%%%%%%%

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


%% %%%%%%%% Visualization
screen_size = get(0, 'ScreenSize');
fh = figure('Name',horzcat(pwd, ' [', num2str(tranTypes),'],', test_config.signature,',',num2str(test_config.startIdx),',',num2str(test_config.endIdx)),...
     'Color',[1 1 1]);
set(fh, 'Position', [0 0 screen_size(3) screen_size(4)]);
fontsize=30; %for paper use 40
linewidth=6.5; %for paper use 6.5

dim1 = 1;
dim2 = 1;

nextPlot=1;
if strcmp(taskDesc.taskName, 'CountsToCpu')
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

if strcmp(taskDesc.taskName, 'CountsToIO')
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot(testTPS, [testIO predictionsIO]./1024 ./1024, ':*');
    title('Linear model: Avg Physical Writes');
    legend('actually written', 'predicted written');
    ylabel('written data (MB)');
    text(5,max(predictionsIO)./1024 ./1024, horzcat('MAE(MB)=',num2str(MAE_io./1024 ./1024), ', MRE(%)=', num2str(MRE_io)));
    grid on;
    nextPlot=nextPlot+1;
end

if strcmp(taskDesc.taskName, 'CountsToLatency')
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

if strcmp(taskDesc.taskName, 'CountsWaitTimeToLatency')
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

if strcmp(taskDesc.taskName, 'BlownCountsToCpu')
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot([testP blownPredictionsP]);
    title('Quadratic model: Avg CPU');
    legend('actual CPU usage', 'predicted CPU');
    ylabel('Avg CPU (%)');
    text(5,max(blownPredictionsP), horzcat('MAE=',num2str(blownMAE_p), ', MRE(%)=', num2str(blownMRE_p)));
    grid on;
    nextPlot=nextPlot+1;
end

if strcmp(taskDesc.taskName, 'BlownCountsToIO')
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    plot([testIO blownPredictionsIO]./1024 ./1024);
    title('Quadratic model: Avg Physical Writes');
    legend('actually written', 'predicted written');
    ylabel('written data (MB)');
    text(5,max(blownPredictionsIO./1024 ./1024), horzcat('MAE(MB)=',num2str(blownMAE_io./1024 ./1024), ', MRE(%)=', num2str(blownMRE_io)));
    grid on;
    nextPlot=nextPlot+1;
end

if strcmp(taskDesc.taskName, 'LinearPrediction')
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

if strcmp(taskDesc.taskName, 'PhysicalReadPrediction')
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


if strcmp(taskDesc.taskName, 'FlushRatePrediction')
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
        
    if 1==1
    treeModel = barzanRegressTreeLearn(trainPagesFlushed, trainC);
    treePred = barzanRegressTreeInvoke(treeModel, testC);
    
    naiveLinModel = barzanLinSolve(trainPagesFlushed, trainTPS);
    linPred = barzanLinInvoke(naiveLinModel, testTPS);
    
    betterLinModel = barzanLinSolve(trainPagesFlushed, trainC);
    classLinPred = barzanLinInvoke(betterLinModel, testC);
       
    cfFlushRateApprox_conf = struct('io_conf', taskDesc.io_conf, 'workloadName', taskDesc.workloadName);
    myPred = cfFlushRateApprox(cfFlushRateApprox_conf, testC);
    
    kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', train_configs{1}.tranTypes,  'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
    emp = zeros(size(trainC,1), 0);
    [emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, trainC, trainPagesFlushed);
    kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
    kccaPred = barzanKccaInvoke(kccaModel, testC);
    nnModel = barzanNeuralNetLearn(trainPagesFlushed, trainC);
    nnPred = barzanNeuralNetInvoke(nnModel, testC);

    else
        treePred = testPagesFlushed;
        linPred = testPagesFlushed;
        classLinPred = testPagesFlushed;
        myPred = testPagesFlushed;
        kccaPred = testPagesFlushed;
        nnPred = testPagesFlushed;
    end
    err_1 = mre(linPred, testPagesFlushed, true);
    err_2 = mre(classLinPred, testPagesFlushed, true);
    err_3 = mre(myPred, testPagesFlushed, true);
    err_4 = mre(treePred, testPagesFlushed);
    err_5 = mre(kccaPred, testPagesFlushed);
    err_6 = mre(nnPred, testPagesFlushed);

    [rel_err_1 abs_err_1 rel_diff_1 discrete_rel_error_1 weka_rel_err] = myerr(linPred, testPagesFlushed);
    [rel_err_2 abs_err_2 rel_diff_2 discrete_rel_error_2 weka_rel_err] = myerr(classLinPred, testPagesFlushed);
    [rel_err_3 abs_err_3 rel_diff_3 discrete_rel_error_3 weka_rel_err] = myerr(myPred, testPagesFlushed);
    [rel_err_4 abs_err_4 rel_diff_4 discrete_rel_error_4 weka_rel_err] = myerr(treePred, testPagesFlushed);
    [rel_err_5 abs_err_5 rel_diff_5 discrete_rel_error_5 weka_rel_err] = myerr(kccaPred, testPagesFlushed);
    [rel_err_6 abs_err_6 rel_diff_6 discrete_rel_error_6 weka_rel_err] = myerr(nnPred, testPagesFlushed);

    %%%%%%
    temp = [testPagesFlushed linPred classLinPred myPred treePred kccaPred nnPred];
    if strcmp(taskDesc.plotX, 'byTPS')
        temp = [testTPS temp];
    elseif strcmp(taskDesc.plotX, 'byCounts')
        assert(length(taskDesc.whichTransToPlot)==1, 'You can only specify one transaction to use for sorting');
        temp = [testC(:,taskDesc.whichTransToPlot)./testTPS temp];
    else
        error(['taskDesc.plotX cannot be: ' taskDesc.plotx]);
    end
    temp = sortrows(temp,1);
    
    ph1 = plot(temp(:,1), temp(:,2), 'b.-'); 
    hold on;
    ph2 = plot(temp(:,1), temp(:,3), 'ms--');
    ph3 = plot(temp(:,1), temp(:,4), 'k-.');
    ph4 = plot(temp(:,1), temp(:,5), 'gp:'); 
    ph5 = plot(temp(:,1), temp(:,6), 'rp:'); 
    ph6 = plot(temp(:,1), temp(:,7), 'yp:'); 
    ph7 = plot(temp(:,1), temp(:,8), 'cp:'); 
    
    hold off;
    title(horzcat('Flush rate prediction #test points=', num2str(size(testC,1)),' '));    
    legend('Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'KCCA', 'NeuralNet');
    ylabel('Average # of page flush per sec');

    if strcmp(taskDesc.plotX, 'byTPS')
        xlabel('TPS');
    elseif strcmp(taskDesc.plotX, 'byCounts')
        xlabel(['Ratio of trans ' num2str(taskDesc.whichTransToPlot)]);
    else
        error(['taskDesc.plotX cannot be: ' taskDesc.plotx]);
    end
    
    text(0.1,max(linPred), horzcat('MRE(lin TPS)=',num2str(err_1), ...
        ', MRE(lin types)=',num2str(err_2), ', MRE(cf 1)=',num2str(err_3), ', MRE(cf 2)=',num2str(err_4)));
    
    if isfield(taskDesc, 'resultsFile')
        if taskDesc.appendToFile == true
            fid = fopen(taskDesc.resultsFile, 'a');
        else
            fid = fopen(taskDesc.resultsFile, 'w');
            fprintf(fid, 'trainSummary\tAvg Train TPS\ttestSummary\tAvg Test TPS');
            fprintf(fid, '\tLR abs err\tLR rel err%%\tLR dis rel%%');
            fprintf(fid, '\tclsLR abs err\tclsLR rel err%%\tclsLR dis rel%%');
            fprintf(fid, '\tour abs err\tour rel err%%\tour dis rel%%');        
            fprintf(fid, '\ttree abs err\ttree rel err%%\ttree dis rel%%');        
            fprintf(fid, '\tkcca abs err\tkcca rel err%%\tkcca dis rel%%');        
            fprintf(fid, '\tnnet abs err\tnnet rel err%%\tnnet dis rel%%');        
            fprintf(fid, '\tcmdLine\tParams');
            fprintf(fid, '\n');            
        end
        fprintf(fid, '%s\t%.0f\t%s\t%.0f', trainSummary, mean(trainTPS(trainTPS>5)), testSummary, mean(testTPS(testTPS>5)));
        fprintf(fid, '\t%.0f\t%.0f\t%.0f', abs_err_1, 100*rel_err_1, 100*discrete_rel_error_1);
        fprintf(fid, '\t%.0f\t%.0f\t%.0f', abs_err_2, 100*rel_err_2, 100*discrete_rel_error_2);
        fprintf(fid, '\t%.0f\t%.0f\t%.0f', abs_err_3, 100*rel_err_3, 100*discrete_rel_error_3);        
        fprintf(fid, '\t%.0f\t%.0f\t%.0f', abs_err_4, 100*rel_err_4, 100*discrete_rel_error_4);        
        fprintf(fid, '\t%.0f\t%.0f\t%.0f', abs_err_5, 100*rel_err_5, 100*discrete_rel_error_5);        
        fprintf(fid, '\t%.0f\t%.0f\t%.0f', abs_err_6, 100*rel_err_6, 100*discrete_rel_error_6);  
        
        fprintf(fid, '\t%s\t%s', cmdLine, ['io_conf=[', num2str(taskDesc.io_conf),'];']);

        fprintf(fid, '\n');
        fclose(fid);
    end
    
    
    grid on;
    nextPlot=nextPlot+1;    
end

if strcmp(taskDesc.taskName, 'MaxThroughputPrediction')
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
    if exist('trainMaxThroughputIdx') && ~isempty(trainMaxThroughputIdx)
        idx=1:trainMaxThroughputIdx;
    else
        idx=1:size(trainP,1);
    end
    myModelP = barzanLinSolve(trainP(idx,:), trainC(idx,:));
    myCpuC = barzanLinInvoke(myModelP, range*testMixture);
    myCpuCLThoughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
    myCpuCUThoughput = find(myCpuC>98 & myCpuC<100, 1, 'last');
    
    %Our IO-based throughput
    cfFlushRateApprox_conf = struct('io_conf', taskDesc.io_conf, 'workloadName', taskDesc.workloadName);
    
    myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*testMixture, maxFlushRate, cfFlushRateApprox_conf);

    %Linear IO-based throughput
    linFlushRate = barzanLinInvoke(modelFlushRate, range*testMixture);
    linFlushRateThoughput = find(linFlushRate<maxFlushRate, 1, 'last');
    if isempty(linFlushRateThoughput); linFlushRateThoughput=0; end
    
    %Decision-tree-based throughput
    treeModel = barzanRegressTreeLearn(trainPagesFlushed, trainC);
    treeFlushRate = barzanRegressTreeInvoke(treeModel, range*testMixture);
    treeFlushRateThoughput = find(treeFlushRate<maxFlushRate, 1, 'last');
    if isempty(treeFlushRateThoughput); treeFlushRateThoughput=0; end
    

    %Lock-based throughput
    getConcurrencyLebel_conf = struct('lock_conf', taskDesc.lock_conf, 'workloadName', taskDesc.workloadName);
    concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*testMixture, 160, getConcurrencyLebel_conf);

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
    e10 = 100*(treeFlushRateThoughput-actualThr)/actualThr;
    
    othersMT = [e1 e2 e3 e4 e5 e10];
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
    
    if isfield(taskDesc, 'resultsFile')
        if taskDesc.appendToFile == true
            fid = fopen(taskDesc.resultsFile, 'a');
        else
            fid = fopen(taskDesc.resultsFile, 'w');
            fprintf(fid, ['training\t' ...
        'training TPS range\t' ...
        'testing\t' ...
        'testing TPS range\t' ...
        'actual max throughput\t' ...  
        'CPU at max throughput\t' ...  
        '#PF at max throughput\t' ...     
        'LR for CPU\t' 'LR for CPU (err%%)\t'  ...      
        'Adjusted LR for CPU\t' 'Adjusted LR for CPU (err%%)\t'  ...
        'LR + clasification for CPU\t' 'LR + clasification for CPU (err%%)\t'  ... 
        'Adjusted LR + classification for CPU\t' 'Adjusted LR + classification for CPU (err%%)\t'  ... 
        'LR for #PF\t' 'LR for #PF (err%%)\t'  ...   
        'Dec. Tree for #PF\t' 'Dec. Tree for #PF (err%%)\t'  ...           
        'Our model for CPU\t' 'Our model for CPU (err%%)\t'  ... 
        'Our model for #PF\t' 'Our model for #PF (err%%)\t'  ...
        'Our model for lock contention\t' 'Our model for lock contention (err%%)\t'  ...
        'Our combined model\t' 'Our combined model (err%%)\t'  ...
        'Min under-provisioning our model saved\t' 'Max under-provisioning our model saved\t' ...
        'Min over-provisioing our model saved\t' 'Max over-provisioing our model saved\t' ...
        'Real Bottleneck Resource\t' ...
        'Predicted Bottleneck Resource\t' ...
        'Command\t' ...
        'Parameters' ...
        '\n']);
        end
        fprintf(fid, '%s\t%.0f~%.0f\t%s\t%.0f~%.0f\t%.0f\t%.0f\t%.0f', trainSummary, min(trainTPS),max(trainTPS), testSummary, min(testTPS), max(testTPS), ...
            actualThr, realCPU, realPageFlushed);
        fprintf(fid, '\t%.2f\t%.0f', cpuTUThoughput, e1);
        fprintf(fid, '\t%.2f\t%.0f', cpuTLThoughput, e2);
        fprintf(fid, '\t%.2f\t%.0f', cpuCUThoughput, e3);
        fprintf(fid, '\t%.2f\t%.0f', cpuCLThoughput, e4);
        fprintf(fid, '\t%.2f\t%.0f', linFlushRateThoughput, e5);
        fprintf(fid, '\t%.2f\t%.0f', treeFlushRateThoughput, e10);        
        fprintf(fid, '\t%.2f\t%.0f', myCpuCLThoughput, e6);
        fprintf(fid, '\t%.2f\t%.0f', myFlushRateThroughput, e7);
        fprintf(fid, '\t%.2f\t%.0f', concurrencyThroughput, e8);
        fprintf(fid, '\t%.2f\t%.0f', myMaxThroughput1, e9);
        fprintf(fid, '\t%.2f\t%.2f\t%.2f\t%.2f', minUP, maxUP, minOP, maxOP);
        fprintf(fid, '\t%s\t%s\t%s\t%s\n', char(reasons(realReasonIdx)), char(reasons(PredReasonIdx1)), cmdLine, horzcat('io_conf=[', num2str(taskDesc.io_conf),']; lock_conf=[', num2str(taskDesc.lock_conf),'];'));
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
%    text(0.1,max(linPred), horzcat('MRE(lin TPS)=',num2str(err_1), ...
%        ', MRE(lin types)=',num2str(err_2), ', MRE(cf 1)=',num2str(err_3), ', MRE(cf 2)=',num2str(err_4)));
    grid on;
    nextPlot=nextPlot+1;
end

if strcmp(taskDesc.taskName, 'LockPrediction')
    subplot(dim1,dim2,nextPlot,'FontSize',fontsize);
    
    if ~isfield(taskDesc, 'learnLock') || ~isfield(taskDesc, 'lockType')
        error('If you''re doing lock prediction, you need to explicitly specify where to relearn the lock_conf or to rely on the one that comes with the dataset');
    end
    
    if strcmp(taskDesc.lockType, 'waitTime')
        my_train_lock = trainW;
        my_test_lock = testW;
    elseif strcmp(taskDesc.lockType, 'numberOfLocks')
        my_train_lock = trainLocksBeingWaitedFor;
        my_test_lock = testLocksBeingWaitedFor;
    elseif strcmp(taskDesc.lockType, 'numberOfConflicts')
        my_train_lock = traintNumOfWaitsDueToLocks;
        my_test_lock = testNumOfWaitsDueToLocks;
    else
        error(['Invalid lockType:' taskDesc.lockType]);
    end
    
    if taskDesc.learnLock % re-learn it!
        tic;
        if strcmp(taskDesc.lockType, 'waitTime')
            f = @(conf2, data)(getfield(useLockModel([0.125 0.0001 conf2], data, taskDesc.workloadName), 'TimeSpentWaiting'));
        elseif strcmp(taskDesc.lockType, 'numberOfLocks')
            f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').LocksBeingHeld'));
        elseif strcmp(taskDesc.lockType, 'numberOfConflicts')
            f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').totalWaits'));
        else
            error(['Invalid lockType:' taskDesc.lockType]);
        end
        
        %opt = optimset('MaxIter', 10000, 'MaxFunEvals', 10000, 'TolFun', 0.00001);
        %domain_cost = lsqcurvefit(f, [100 0.000004], trainC, ... %let's just domain multiplier and cost multiplier
        %my_train_lock, ...
        % [0.1 0.0000000001], [1000000 5],... %lower and upper bounds
        % opt); % curve fitting params!
        domain_cost = barzanCurveFit(f, trainC, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [taskDesc.emIters taskDesc.emIters]);
        fprintf(1,'training time for learning the lock_conf=');
        lock_conf = [0.125 0.0001 domain_cost];
        toc;
    elseif isfield(taskDesc, 'lock_conf')
        lock_conf = taskDesc.lock_conf;
    else
        error('You should either let us re-learn or should give us the lock_conf to use!');
    end
    tic;
    fprintf(1,'We will use lock_conf=%s\n', valueToString(lock_conf));
    allPreds = useLockModel(lock_conf, testC, taskDesc.workloadName);
    if strcmp(taskDesc.lockType, 'waitTime')
        myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);        
    elseif strcmp(taskDesc.lockType, 'numberOfLocks')
        myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
    elseif strcmp(taskDesc.lockType, 'numberOfConflicts')
        myPredictedLock = sum(allPreds.totalWaits, 2);
    else
        error(['Invalid lockType:' taskDesc.lockType]);
    end

    fprintf(1,'testing time for lock prediction=');
    toc;
    
    %mean(predictedLatencies)
    %NOTE: Uncomment the following two lines for getting a baseline accuracy!
    %av = mean(AllLatencies(trainSt:trainEnd,:));
    %predictedLatencies = repmat(av, testEnd-testSt+1, 1);
    
    classifierLinModel = barzanLinSolve(my_train_lock, trainC);
    classifierLinPredictions = barzanLinInvoke(classifierLinModel, testC);

    classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
    classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);
    
    treeModel = barzanRegressTreeLearn(my_train_lock, trainTPS);
    treePredictions = barzanRegressTreeInvoke(treeModel, testTPS);

    kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
    kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);
    
    allPreds = useLockModel([1 1 1 1], testC, taskDesc.workloadName);
    if strcmp(taskDesc.lockType, 'waitTime')
        thomasianPreds = sum(allPreds.TimeSpentWaiting, 2);        
    elseif strcmp(taskDesc.lockType, 'numberOfLocks')
        thomasianPreds = sum(allPreds.LocksBeingHeld, 2);
    elseif strcmp(taskDesc.lockType, 'numberOfConflicts')
        thomasianPreds = sum(allPreds.totalWaits, 2);
    else
        error(['Invalid lockType:' taskDesc.lockType]);
    end

    
    temp = [my_test_lock myPredictedLock classifierLinPredictions classQuadPredictions treePredictions kccaPredictions thomasianPreds];
    if strcmp(taskDesc.plotX, 'byTPS')
        temp = [testTPS temp];
    elseif strcmp(taskDesc.plotX, 'byCounts')
        assert(length(taskDesc.whichTransToPlot)==1, 'You can only specify one transaction to use for sorting');
        temp = [testC(:,taskDesc.whichTransToPlot) temp];
    else
        error(['taskDesc.plotX cannot be: ' taskDesc.plotx]);
    end
    temp = sortrows(temp,1);

    %uncomment to see the shape!
    %temp = [temp(:,1) normMatrix(temp(:,2:end))]; 
    
    ph1 = plot(temp(:,1), temp(:,2), 'b*');
    hold on;
    ph2 = plot(temp(:,1), temp(:,3:end), '-');
    %plot(trainTPS, my_train_lock, 'g');
    %plot(UGtestTPS, UGtestW, 'g:');
    %plot(UGtrainTPS, UGtrainW, 'bo');
    

    abs_err_1 = mae(temp(:,3), temp(:,2));
    rel_err_1 = mre(temp(:,3), temp(:,2));
        
    abs_err_2 = mae(temp(:,4), temp(:,2));
    rel_err_2 = mre(temp(:,4), temp(:,2));
    
    abs_err_3 = mae(temp(:,5), temp(:,2));
    rel_err_3 = mre(temp(:,5), temp(:,2));
    
    abs_err_4 = mae(temp(:,6), temp(:,2));
    rel_err_4 = mre(temp(:,6), temp(:,2));
    
    abs_err_5 = mae(temp(:,7), temp(:,2));
    rel_err_5 = mre(temp(:,7), temp(:,2));

    abs_err_6 = mae(temp(:,8), temp(:,2));
    rel_err_6 = mre(temp(:,8), temp(:,2));
    
%    title(['MAE=' num2str(MAE) ' MRE=' num2str(MRE) '%, '...
 %       ,' tpsLin=(',num2str(MRE_tps) '%,' num2str(MAE_tps) ...
  %      ,'),clsLin=(',num2str(MRE_class) '%,' num2str(MAE_class) ...
   %     ,'),tpsQuad=(',num2str(MRE_tps_2) '%,' num2str(MAE_tps_2) ...
    %    ,'),clsQuad=(',num2str(MRE_class_2) '%,' num2str(MAE_class_2) ')' ...
     %   ]);
    ylabel('Total time spent acquiring row locks (sec)');
    legend('Actual', 'Our contention model', 'LR+class', 'quad+class', 'Dec. tree regression', 'KCCA', 'Orig. Thomasian');

    if strcmp(taskDesc.plotX, 'byTPS')
        xlabel('TPS');
    elseif strcmp(taskDesc.plotX, 'byCounts')
        xlabel(['# of trans ' num2str(taskDesc.whichTransToPlot)]);
    else
        error(['taskDesc.plotX cannot be: ' taskDesc.plotx]);
    end

    if isfield(taskDesc, 'resultsFile')
        if taskDesc.appendToFile == true
            fid = fopen(taskDesc.resultsFile, 'a');
        else
            fid = fopen(taskDesc.resultsFile, 'w');
            fprintf(fid, 'trainSummary\tAvg Train TPS\ttestSummary\tAvg Test TPS');
            fprintf(fid, '\tOur abs\tOur rel %%');
            fprintf(fid, '\tLR abs\tLR rel%%');
            fprintf(fid, '\tQuad abs\tQuad rel%%');        
            fprintf(fid, '\ttree abs\ttree rel%%');        
            fprintf(fid, '\tkcca abs\tkcca rel%%');        
            fprintf(fid, '\torigTomasian abs\torigTomasian rel%%');        
            fprintf(fid, '\tcmdLine\tParams');
            fprintf(fid, '\n');            
        end
        fprintf(fid, '%s\t%.0f\t%s\t%.0f', trainSummary, mean(trainTPS(trainTPS>5)), testSummary, mean(testTPS(testTPS>5)));
        fprintf(fid, '\t%.3f\t%.0f', abs_err_1, 100*rel_err_1);
        fprintf(fid, '\t%.3f\t%.0f', abs_err_2, 100*rel_err_2);
        fprintf(fid, '\t%.3f\t%.0f', abs_err_3, 100*rel_err_3);        
        fprintf(fid, '\t%.3f\t%.0f', abs_err_4, 100*rel_err_4);        
        fprintf(fid, '\t%.3f\t%.0f', abs_err_5, 100*rel_err_5);        
        fprintf(fid, '\t%.3f\t%.0f', abs_err_6, 100*rel_err_6);  
        
        fprintf(fid, '\t%s\t%s', cmdLine, ['lock_conf=[', num2str(lock_conf),'];']);

        fprintf(fid, '\n');
        fclose(fid);
    end
    
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
RealFeaturesToLatency=0;
IdealFeaturesToLatency=0;

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
    lock_conf = taskDesc.lock_conf;
    completeTrainCounts = zeros(size(Tr_trainC,1),5);
    completeTestCounts = zeros(size(Ts_testC,1),5);
    completeTrainCounts(:, tranLabels) = Tr_trainC;
    completeTestCounts(:, tranLabels) = Ts_testC;
    
    [estTrainR estTrainT_total estTrainM_total estTrainVp estTrainV estTrainW estTrainPcon estTraintotalWaits estTrainTimeSpentWaiting] = useLockModel(lock_conf, completeTrainCounts);
    [estTestR estTestT_total estTestM_total estTestVp estTestV estTestW estTestPcon estTesttotalWaits estTestTimeSpentWaiting] = useLockModel(lock_conf, completeTestCounts);

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

