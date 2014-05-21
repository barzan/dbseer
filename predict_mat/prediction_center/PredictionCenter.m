classdef PredictionCenter

    properties
        taskDescription
        trainConfigList
        testConfig
    end
    
    methods
        function result = performPrediction(this)
            if strcmp(this.taskDescription.taskName, 'FlushRatePrediction')
                result = this.flushRatePrediction;
            elseif strcmp(this.taskDescription.taskName, 'MaxThroughputPrediction')
                result = this.maxThroughputPrediction;
            else
                error(strcat('Unsupported task name: ', this.taskDescription.taskName));
            end
        end
        
        function [mvTrainGrouped mvTrainUngrouped trainSummary] = loadTrainConfigs(this)
            trainSummary = '{';
            howManyTrain = length(this.trainConfigList);
            for i=1:howManyTrain
                train_i_conf = this.trainConfigList{i}.getStruct;
                mvTrain_i = load_modeling_variables(train_i_conf.dir, train_i_conf.signature);
    
                if isfield(train_i_conf, 'groupingStrategy')
                    grp_mvTrain_i = load_modeling_variables(train_i_conf.dir, train_i_conf.signature, train_i_conf.groupingStrategy);
                else
                    grp_mvTrain_i = mvTrain_i;
                end
    
                if i==1
                    mvTrainUngrouped = mvTrain_i;
                    mvTrainGrouped = grp_mvTrain_i;
                else
                    mvTrainUngrouped = merge_structs(mvTrainUngrouped, mvTrain_i);
                    mvTrainGrouped = merge_structs(mvTrainGrouped, grp_mvTrain_i);
                end
    
                tps = sum(mvTrainUngrouped.clientIndividualSubmittedTrans(:,this.testConfig.tranTypes), 2);
                trainSummary = [trainSummary train_i_conf.signature ':' num2str(min(tps)) '-' num2str(max(tps))];
                if i < howManyTrain
                    trainSummary = [trainSummary ',' ];
                end
            end
        end
        
        function result = flushRatePrediction(this)
            if isempty(this.testConfig.groupingStrategy)
                mv = load_modeling_variables(this.testConfig.dir, this.testConfig.signature);
            else
                groupStrategy = struct('groupParams', this.testConfig.groupingStrategy.getStruct);
                mv = load_modeling_variables(this.testConfig.dir, this.testConfig.signature, groupStrategy);
            end
            transactionsForTest = mv.clientIndividualSubmittedTrans(:, this.testConfig.tranTypes);
            config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.taskDescription.workloadName);
            result = cfFlushRateApprox(config, transactionsForTest);
        end
        
        function result = maxThroughputPrediction(this)
            if isempty(this.testConfig.groupingStrategy)
                mvTestUngrouped = load_modeling_variables(this.testConfig.directory, this.testConfig.signature);
                mvTestGrouped = mvUngrouped;
            else
                if isempty(fieldnames(this.testConfig.groupingStrategy))
                    groupStrategy = struct();
                else
                    groupStrategy = struct('groupParams', this.testConfig.groupingStrategy.getStruct);
                end
                mvTestUngrouped = load_modeling_variables(this.testConfig.dir, this.testConfig.signature);
                mvTestGrouped = load_modeling_variables(this.testConfig.dir, this.testConfig.signature, groupStrategy);
            end
            
            tranTypes = this.testConfig.tranTypes;
            
            [mvTrainGrouped mvTrainUngrouped trainSummary] = this.loadTrainConfigs;
            UGtrainC = mvTrainUngrouped.clientIndividualSubmittedTrans(:,tranTypes);
            UGtrainP = mean(mvTrainUngrouped.cpu_usr,2);
            UGtrainIO = mvTrainUngrouped.osNumberOfSectorWrites;
            if isfield(mvTrainUngrouped, 'dbmsLockWaitTime')
                UGtrainW = mvTrainUngrouped.dbmsLockWaitTime;
                UGtrainLocksBeingWaitedFor=mvTrainUngrouped.dbmsCurrentLockWaits;
                UGtrainNumOfWaitsDueToLocks=mvTrainUngrouped.dbmsLockWaits;
            else
                clear 'UGtrainW';
                clear 'UGtrainLocksBeingWaitedFor';
                clear 'UGtrainNumOfWaitsDueToLocks';
            end

            UGtrainL = mvTrainUngrouped.clientTransLatency(:,tranTypes);
            UGtrainTPS = sum(UGtrainC,2);
            if isfield(mvTrainUngrouped, 'dbmsChangedRows')
                UGtrainRowsChanged = mvTrainUngrouped.dbmsChangedRows;
            else
                clear 'UGtrainRowsChanged';
            end
            UGtrainPagesFlushed = mvTrainUngrouped.dbmsFlushedPages;
            idx = find(UGtrainTPS>0);
            ratios = UGtrainC(idx,:) ./ repmat(UGtrainTPS(idx),1,size(UGtrainC,2));
            UGtrainMixture = mean(ratios);
            
            trainC = mvTrainGrouped.clientIndividualSubmittedTrans(:,tranTypes);
            trainP = mean(mvTrainGrouped.cpu_usr,2);
            trainIO = mvTrainGrouped.osNumberOfSectorWrites;
            if isfield(mvTrainGrouped, 'dbmsLockWaitTime')
                trainW = mvTrainGrouped.dbmsLockWaitTime;
                trainLocksBeingWaitedFor=mvTrainGrouped.dbmsCurrentLockWaits;
                trainNumOfWaitsDueToLocks=mvTrainGrouped.dbmsLockWaits;
            else
                clear 'trainW';
                clear 'trainLocksBeingWaitedFor';
                clear 'trainNumOfWaitsDueToLocks';
            end

            trainL = mvTrainGrouped.clientTransLatency(:,tranTypes);
            trainTPS = sum(trainC,2);
            if isfield(mvTrainGrouped, 'dbmsChangedRows')
                trainRowsChanged = mvTrainGrouped.dbmsChangedRows;
            else
                clear 'trainRowsChanged';
            end
            trainPagesFlushed = mvTrainGrouped.dbmsFlushedPages;
                idx = find(trainTPS>0);
                ratios = trainC(idx,:) ./ repmat(trainTPS(idx),1,size(trainC,2));
            trainMixture = mean(ratios);
            trainLogicalReads = mvTrainGrouped.dbmsReadRequests;
            trainPhysicalReads = mvTrainGrouped.dbmsReads;
            if isfield(mvTrainGrouped, 'dbmsNumberOfDataReads')
                trainPhysicalReadsMB = mvTrainGrouped.dbmsNumberOfDataReads / 1024 / 1024; 
            else
                clear 'trainPhysicalReadsMB';
            end
            trainNetworkSendKB=mvTrainGrouped.osNetworkSendKB;
            trainNetworkRecvKB=mvTrainGrouped.osNetworkRecvKB;
            trainLogIOw=mvTrainGrouped.dbmsLogWritesMB; %MB
            
            UGtestC = mvTestUngrouped.clientIndividualSubmittedTrans(:,tranTypes);
            UGtestP = mean(mvTestUngrouped.cpu_usr, 2);
            UGtestPagesFlushed = mvTestUngrouped.dbmsFlushedPages;
            UGtestTPS = sum(UGtestC,2);
            [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(UGtestTPS);
            [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(UGtrainTPS);
            actualThr = testMaxThroughput;
            realCPU = mean(UGtestP(testMaxThroughputIdx-10:testMaxThroughputIdx+10,:));
            realPageFlushed = mean(UGtestPagesFlushed(testMaxThroughputIdx-10:testMaxThroughputIdx+10,:));
            
            testC = mvTestGrouped.clientIndividualSubmittedTrans(:,tranTypes);
            testP = mean(mvTestGrouped.cpu_usr, 2);
            testIO = mvTestGrouped.osNumberOfSectorWrites;
            if isfield(mvTestGrouped, 'dbmsLockWaitTime')
                testW = mvTestGrouped.dbmsLockWaitTime;
                testLocksBeingWaitedFor=mvTestGrouped.dbmsCurrentLockWaits;
                testNumOfWaitsDueToLocks=mvTestGrouped.dbmsLockWaits;
            else
                clear 'testW';
                clear 'testLocksBeingWaitedFor';
                clear 'testNumOfWaitsDueToLocks';
            end
            testL = mvTestGrouped.clientTransLatency(:,tranTypes);
            testTPS = sum(testC,2);
            if isfield(mvTestGrouped, 'dbmsChangedRows')
                testRowsChanged = mvTestGrouped.dbmsChangedRows;
            else
                clear 'testRowsChanged';
            end
            
            testPagesFlushed = mvTestGrouped.dbmsFlushedPages;
            idx = find(testTPS>0);
            ratios = testC(idx,:) ./ repmat(testTPS(idx),1,size(testC,2));
            testMixture = mean(ratios);
            range = (1:15000)';
            maxFlushRate = 1000;
            
            cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.taskDescription.workloadName);
            myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*testMixture, maxFlushRate, cfFlushRateApprox_conf);
            modelP = barzanLinSolve(trainP, trainC);
            
            if exist('trainMaxThroughputIdx') && ~isempty(trainMaxThroughputIdx)
                idx=1:trainMaxThroughputIdx;
            else
                idx=1:size(trainP,1);
            end
            myModelP = barzanLinSolve(trainP(idx,:), trainC(idx,:));
            myCpuC = barzanLinInvoke(myModelP, range*testMixture);
            myCpuCLThroughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
            myCpuCUThroughput = find(myCpuC>98 & myCpuC<100, 1, 'last');
    
            myCpuCLThroughput = find(myCpuC>44 & myCpuC<45, 1, 'last');
            myCpuCUThroughput = find(myCpuC>59 & myCpuC<50, 1, 'last');
            
            %Lock-based throughput
            getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.taskDescription.workloadName);
            concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*testMixture, 160, getConcurrencyLebel_conf);
            
            [result(1) PredReasonIdx1] = min([myCpuCLThroughput myFlushRateThroughput concurrencyThroughput]);
            [result(2) PredReasonIdx2] = min([myCpuCUThroughput myFlushRateThroughput concurrencyThroughput]);
            
        end % end function
        
    end % end methods

end % end classdef