classdef PredictionCenter < handle

    properties (Constant = true)
        TEST_MODE_DATASET = 0;
        TEST_MODE_MIXTURE_TPS = 1;
        NUM_TPS_SAMPLES = 10;
    end

    properties
        testVar
        taskName
        workloadName
        lockType
        learnLock
        whichTransactionToPlot
        ioConf
        lockConf

        trainConfig
        testMode
        testConfig
        testMixture
        testMinTPS
        testMaxTPS

        testSampleTPS
        testSampleTransactionCount
    end
    
    methods
        function calculateTestTPSMixture(this)
            this.testSampleTPS = (this.testMaxTPS - this.testMinTPS) .* rand(this.NUM_TPS_SAMPLES, 1) + this.testMinTPS;
            this.testSampleTPS(1) = this.testMinTPS;
            this.testSampleTPS(2) = this.testMaxTPS;
            for i=1:this.NUM_TPS_SAMPLES
                this.testSampleTransactionCount(i,:) = this.testMixture .* this.testSampleTPS(i);
            end
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = performPrediction(this)

            if ~this.trainConfig.isInitialized
                this.trainConfig.initialize;
            end
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                if ~this.testConfig.isInitialized
                    this.testConfig.initialize;
                end
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                this.calculateTestTPSMixture;
            end

            if strcmp(this.taskName, 'FlushRatePredictionByTPS')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.flushRatePredictionByTPS;
            elseif strcmp(this.taskName, 'FlushRatePredictionByCounts')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.flushRatePredictionByCounts;
            elseif strcmp(this.taskName, 'MaxThroughputPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.maxThroughputPrediction;
            elseif strcmp(this.taskName, 'TransactionCountsToCpuByTPS')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.transactionCountsToCpuByTPS;
            elseif strcmp(this.taskName, 'TransactionCountsToCpuByCounts')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.transactionCountsToCpuByCounts;
            elseif strcmp(this.taskName, 'TransactionCountsToIO')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.transactionCountsToIO;
            elseif strcmp(this.taskName, 'TransactionCountsToLatency')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.transactionCountsToLatency;
            elseif strcmp(this.taskName, 'TransactionCountsWaitTimeToLatency')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.transactionCountsWaitTimeToLatency;
            elseif strcmp(this.taskName, 'BlownTransactionCountsToCpu')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.blownTransactionCountsToCpu;
            elseif strcmp(this.taskName, 'BlownTransactionCountsToIO')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.blownTransactionCountsToIO;
            elseif strcmp(this.taskName, 'LinearPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.linearPrediction;
            elseif strcmp(this.taskName, 'PhysicalReadPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.physicalReadPrediction;
            elseif strcmp(this.taskName, 'LockPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = this.lockPrediction;
            else
                error(strcat('Unsupported task name: ', this.taskName));
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = flushRatePredictionByTPS(this)
            meanAbsError = {};
            meanRelError = {};
            
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                mv = this.testConfig.mv;

                treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                treePred = barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount);
                
                naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
                linPred = barzanLinInvoke(naiveLinModel, this.testConfig.TPS);

                betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                classLinPred = barzanLinInvoke(betterLinModel, this.testConfig.transactionCount);

                kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.testConfig.transactionType, 'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                %[emp1 emp2 kccaTrainC kccaTrainCainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                %kccaPred = barzanKccaInvoke(kccaModel, testC);

                nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                nnPred = barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount);
                
                %config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);

                temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                temp = [this.testConfig.TPS temp];

                temp = sortrows(temp,1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6) temp(:,7)]};

                for i=3:7
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end

                legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};

                title = horzcat('Flush rate prediction with # test points = ', num2str(size(this.testConfig.TPS,1)));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = 'TPS';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                % testTPS = [this.testMinTPS (this.testMaxTPS+this.testMinTPS)/2 this.testMaxTPS];
                % testTransactionCount(1,:) = this.testMixture * this.testMinTPS;
                % testTransactionCount(2,:) = this.testMixture * ((this.testMinTPS + this.testMaxTPS) / 2);
                % testTransactionCount(3,:) = this.testMixture * this.testMaxTPS;
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                treePred = barzanRegressTreeInvoke(treeModel, testTransactionCount);
                
                naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
                linPred = barzanLinInvoke(naiveLinModel, testTPS);

                betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                classLinPred = barzanLinInvoke(betterLinModel, testTransactionCount);

                % kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.testConfig.transactionType, 'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                %[emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                %kccaPred = barzanKccaInvoke(kccaModel, testC);

                nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                nnPred = barzanNeuralNetInvoke(nnModel, testTransactionCount);

                %config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myPred = cfFlushRateApprox(config, testTransactionCount);

                temp = [linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                temp = [testTPS temp];

                temp = sortrows(temp,1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};

                legends = {'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};

                title = horzcat('Flush rate prediction with transaction mixture = ', mat2str(this.testMixture), ', Min TPS = ', num2str(this.testMinTPS), ', Max TPS = ', num2str(this.testMaxTPS));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = 'TPS';
            end
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = flushRatePredictionByCounts(this)
            
            meanAbsError = {};
            meanRelError = {};

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                mv = this.testConfig.mv;

                treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                treePred = barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount);
                
                naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
                linPred = barzanLinInvoke(naiveLinModel, this.testConfig.TPS);

                betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                classLinPred = barzanLinInvoke(betterLinModel, this.testConfig.transactionCount);

                kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.trainConfig.transactionType,  'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                [emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                %kccaPred = barzanKccaInvoke(kccaModel, testC);

                nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                nnPred = barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount);

                %config = struct('io_conf', this.ioConf, 'workloadName', this.workloadName);
                config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');

                myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);

                temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                temp = [this.testConfig.transactionCount(:,this.whichTransactionToPlot)./this.testConfig.TPS temp];

                temp = sortrows(temp,1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6) temp(:,7)]};

                for i=3:7
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end

                legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};

                title = horzcat('Flush rate prediction with # test points = ', num2str(size(this.testConfig.transactionCount,1)));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = ['Ratio of transaction ' num2str(this.whichTransactionToPlot)];
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                % testTPS = [this.testMinTPS:(this.testMaxTPS-this.testMinTPS)/(this.NUM_TPS_VALUES-1):this.testMaxTPS];
                % testTransactionCount(1,:) = this.testMixture * this.testMinTPS;
                % testTransactionCount(2,:) = this.testMixture * ((this.testMinTPS + this.testMaxTPS) / 2);
                % testTransactionCount(3,:) = this.testMixture * this.testMaxTPS;
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                treePred = barzanRegressTreeInvoke(treeModel, testTransactionCount);
                
                naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
                linPred = barzanLinInvoke(naiveLinModel, testTPS);

                betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                classLinPred = barzanLinInvoke(betterLinModel, testTransactionCount);

                % kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.trainConfig.transactionType,  'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                % emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                % [emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                %kccaPred = barzanKccaInvoke(kccaModel, testC);

                nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                nnPred = barzanNeuralNetInvoke(nnModel, testTransactionCount);

                %config = struct('io_conf', this.ioConf, 'workloadName', this.workloadName);
                config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');

                myPred = cfFlushRateApprox(config, testTransactionCount);

                temp = [linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                temp = [testTransactionCount(:,this.whichTransactionToPlot) temp];

                temp = sortrows(temp,1);

                this.testVar = temp;

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};

                legends = {'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};

                title = horzcat('Flush rate prediction with transaction mixture = ', mat2str(this.testMixture), ', Min TPS = ', num2str(this.testMinTPS), ', Max TPS = ', num2str(this.testMaxTPS));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = ['# of transaction ' num2str(this.whichTransactionToPlot)];
                
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = maxThroughputPrediction(this)

            meanAbsError = {};
            meanRelError = {};

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                range = (1:15000)';
                maxFlushRate = 1000;
                
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);
                modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
                
                [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped);
                [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);

                actualThr = testMaxThroughput;
                
                if exist('trainMaxThroughputIdx') && ~isempty(trainMaxThroughputIdx)
                    idx=1:trainMaxThroughputIdx;
                else
                    idx=1:size(this.trainConfig.averageCpuUsage,1);
                end

                %CPU-based throughput with classification
                cpuC = barzanLinInvoke(modelP, range*this.testConfig.transactionMixture);
                cpuCLThroughput = find(cpuC>88 & cpuC<90, 1, 'last');
                cpuCUThroughput = find(cpuC>98 & cpuC<100, 1, 'last');
                
                %CPU-based without classification
                cpuTModel = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.TPS);
                cpuT = barzanLinInvoke(cpuTModel, range);
                cpuTLThroughput = find(cpuT>88 & cpuT<90, 1, 'last');
                cpuTUThroughput = find(cpuT>98 & cpuT<100, 1, 'last');

                myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                myCpuC = barzanLinInvoke(myModelP, range*this.testConfig.transactionMixture);
                myCpuCLThroughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
                myCpuCUThroughput = find(myCpuC>98 & myCpuC<100, 1, 'last');
        
                myCpuCLThroughput = find(myCpuC>44 & myCpuC<45, 1, 'last');
                myCpuCUThroughput = find(myCpuC>59 & myCpuC<50, 1, 'last');

                %Our IO-based throughput
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);
                
                %Lock-based throughput
                % getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
                getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
                concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*this.testConfig.transactionMixture, 160, getConcurrencyLebel_conf);

                %Linear IO-based throughput
                modelFlushRate = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testConfig.transactionMixture);
                linFlushRateThroughput = find(linFlushRate<maxFlushRate, 1, 'last');
                if isempty(linFlushRateThroughput); linFlushRateThroughput=0; end
                
                [myMaxThroughput1 PredReasonIdx1] = min([myCpuCLThroughput myFlushRateThroughput concurrencyThroughput]);
                [myMaxThroughput2 PredReasonIdx2] = min([myCpuCUThroughput myFlushRateThroughput concurrencyThroughput]);

                Xdata = {[1:size(this.testConfig.TPS, 1)]'};
                Ydata = {this.testConfig.TPS};
                legends = {'Original Signal'};

                num_row = size(Xdata{1}, 1);
                num_col = size(Xdata{1}, 2);

                if ~isempty(actualThr)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(actualThr, num_row, num_col);
                    legends{end+1} = 'Actual Max Throughput';
                end
                if ~isempty(cpuCLThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuCLThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on adjusted LR for CPU+classification';
                    meanAbsError{end+1} = mae(cpuCLThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuCLThroughput, actualThr);
                end
                if ~isempty(cpuCUThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuCUThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput on LR for CPU+classification';
                    meanAbsError{end+1} = mae(cpuCUThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuCUThroughput, actualThr);
                end
                if ~isempty(cpuTLThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuTLThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput on adjusted LR for CPU';
                    meanAbsError{end+1} = mae(cpuTLThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuTLThroughput, actualThr);
                end
                if ~isempty(cpuTUThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuTUThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on LR for CPU';
                    meanAbsError{end+1} = mae(cpuTUThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuTUThroughput, actualThr);
                end
                if ~isempty(myFlushRateThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(myFlushRateThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on our flush rate model';
                    meanAbsError{end+1} = mae(myFlushRateThroughput, actualThr);
                    meanRelError{end+1} = mre(myFlushRateThroughput, actualThr);
                end
                if ~isempty(linFlushRateThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(linFlushRateThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on LR for flush rate'; 
                    meanAbsError{end+1} = mae(linFlushRateThroughput, actualThr);
                    meanRelError{end+1} = mre(linFlushRateThroughput, actualThr);
                end
                if ~isempty(concurrencyThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(concurrencyThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on our contention model';
                    meanAbsError{end+1} = mae(concurrencyThroughput, actualThr);
                    meanRelError{end+1} = mre(concurrencyThroughput, actualThr);
                end

                title = 'Max Throughput Prediction';
                Ylabel = 'TPS';
                Xlabel = 'Time';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                % testTPS = [this.testMinTPS:(this.testMaxTPS-this.testMinTPS)/(this.NUM_TPS_VALUES-1):this.testMaxTPS];
                % testTransactionCount(1,:) = this.testMixture * this.testMinTPS;
                % testTransactionCount(2,:) = this.testMixture * ((this.testMinTPS + this.testMaxTPS) / 2);
                % testTransactionCount(3,:) = this.testMixture * this.testMaxTPS;

                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                range = (1:15000)';
                maxFlushRate = 1000;
                
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);
                modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
                
                [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
                [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);

                actualThr = testMaxThroughput;
                
                if exist('trainMaxThroughputIdx') && ~isempty(trainMaxThroughputIdx)
                    idx=1:trainMaxThroughputIdx;
                else
                    idx=1:size(this.trainConfig.averageCpuUsage,1);
                end

                %CPU-based throughput with classification
                cpuC = barzanLinInvoke(modelP, range*this.testMixture);
                cpuCLThroughput = find(cpuC>88 & cpuC<90, 1, 'last');
                cpuCUThroughput = find(cpuC>98 & cpuC<100, 1, 'last');
                
                %CPU-based without classification
                cpuTModel = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.TPS);
                cpuT = barzanLinInvoke(cpuTModel, range);
                cpuTLThroughput = find(cpuT>88 & cpuT<90, 1, 'last');
                cpuTUThroughput = find(cpuT>98 & cpuT<100, 1, 'last');

                myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                myCpuC = barzanLinInvoke(myModelP, range*this.testMixture);
                myCpuCLThroughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
                myCpuCUThroughput = find(myCpuC>98 & myCpuC<100, 1, 'last');
        
                myCpuCLThroughput = find(myCpuC>44 & myCpuC<45, 1, 'last');
                myCpuCUThroughput = find(myCpuC>59 & myCpuC<50, 1, 'last');

                %Our IO-based throughput
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);
                
                %Lock-based throughput
                % getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
                getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
                concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*this.testMixture, 160, getConcurrencyLebel_conf);

                %Linear IO-based throughput
                modelFlushRate = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testMixture);
                linFlushRateThroughput = find(linFlushRate<maxFlushRate, 1, 'last');
                if isempty(linFlushRateThroughput); linFlushRateThroughput=0; end
                
                [myMaxThroughput1 PredReasonIdx1] = min([myCpuCLThroughput myFlushRateThroughput concurrencyThroughput]);
                [myMaxThroughput2 PredReasonIdx2] = min([myCpuCUThroughput myFlushRateThroughput concurrencyThroughput]);

                Xdata = {[1:size(testTPS, 1)]'};
                Ydata = {testTPS};
                legends = {'Signal Generated From User Input'};

                num_row = size(Xdata{1}, 1);
                num_col = size(Xdata{1}, 2);

                if ~isempty(actualThr)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(actualThr, num_row, num_col);
                    legends{end+1} = 'Actual Max Throughput';
                end
                if ~isempty(cpuCLThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(cpuCLThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on adjusted LR for CPU+classification';
                end
                if ~isempty(cpuCUThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(cpuCUThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput on LR for CPU+classification';
                end
                if ~isempty(cpuTLThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(cpuTLThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput on adjusted LR for CPU';
                end
                if ~isempty(cpuTUThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(cpuTUThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on LR for CPU';
                end
                if ~isempty(myFlushRateThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(myFlushRateThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on our flush rate model';
                end
                if ~isempty(linFlushRateThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(linFlushRateThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on LR for flush rate'; 
                end
                if ~isempty(concurrencyThroughput)
                    Xdata{end+1} = [1:size(testTPS, 1)]';
                    Ydata{end+1} = repmat(concurrencyThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on our contention model';
                end

                title = 'Max Throughput Prediction';
                Ylabel = 'TPS';
                Xlabel = 'Time';
            end

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = lockPrediction(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                if strcmp(this.lockType, 'waitTime')
                    my_train_lock = this.trainConfig.lockWaitTime;
                    my_test_lock = this.testConfig.lockWaitTime;
                elseif strcmp(this.lockType, 'numberOfLocks')
                    my_train_lock = this.trainConfig.currentLockWait;
                    my_test_lock = this.testConfig.currentLockWait;
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    my_train_lock = this.trainConfig.lockWaitTime;
                    my_test_lock = this.testConfig.lockWaitTime;
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                if this.learnLock == true % re-learn it!
                    if strcmp(this.lockType, 'waitTime')
                        % f = @(conf2, data)(getfield(useLockModel([0.125 0.0001 conf2], data, this.workloadName), 'TimeSpentWaiting'));
                        f = @(conf2, data)(getfield(useLockModel([0.125 0.0001 conf2], data, 'TPCC'), 'TimeSpentWaiting'));
                    elseif strcmp(this.lockType, 'numberOfLocks')
                        f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').LocksBeingHeld'));
                    elseif strcmp(this.lockType, 'numberOfConflicts')
                        f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').totalWaits'));
                    else
                        error(['Invalid lockType:' this.lockType]);
                    end
                    
                    % taskDesc.emIters is hard-coded as 5 for now.
                    domain_cost = barzanCurveFit(f, this.trainConfig.transactionCount, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);
                    lock_conf = [0.125 0.0001 domain_cost];
                elseif ~isempty(this.lockConf)
                    lock_conf = this.lockConf;
                else
                    error('You should either let us re-learn or should give us the lock_conf to use!');
                end
             
                % allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, this.workloadName);
                allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, 'TPCC');
                if strcmp(this.lockType, 'waitTime')
                    myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);        
                elseif strcmp(this.lockType, 'numberOfLocks')
                    myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    myPredictedLock = sum(allPreds.totalWaits, 2);
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount);
                classifierLinPredictions = barzanLinInvoke(classifierLinModel, this.testConfig.transactionCount);

                range=1:1:size(this.trainConfig.transactionCount,2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
                blownTestC = [this.testConfig.transactionCount this.testConfig.transactionCount.*this.testConfig.transactionCount this.testConfig.transactionCount(:, comb1).*this.testConfig.transactionCount(:, comb2)];
                classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
                classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);
                
                treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS);
                treePredictions = barzanRegressTreeInvoke(treeModel, this.testConfig.TPS);

                % kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
                % kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);
                
                % allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, this.workloadName);
                allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, 'TPCC');

                if strcmp(this.lockType, 'waitTime')
                    thomasianPreds = sum(allPreds.TimeSpentWaiting, 2);        
                elseif strcmp(this.lockType, 'numberOfLocks')
                    thomasianPreds = sum(allPreds.LocksBeingHeld, 2);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    thomasianPreds = sum(allPreds.totalWaits, 2);
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                temp = [my_test_lock myPredictedLock classifierLinPredictions classQuadPredictions treePredictions thomasianPreds]; % kccaPredictions omitted.

                % by TPS only for now
                temp = [this.testConfig.TPS temp];

                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {temp(:,2:end)};

                for i=3:7
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end

                Xlabel = 'TPS';
                Ylabel = 'Total time spent acquiring row locks (seconds)';
                legends = {'Actual', 'Our contention model', 'LR+class', 'quad+class', 'Dec. tree regression', 'Orig. Thomasian'};
                title = 'Lock Prediction';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                if strcmp(this.lockType, 'waitTime')
                    my_train_lock = this.trainConfig.lockWaitTime;
                elseif strcmp(this.lockType, 'numberOfLocks')
                    my_train_lock = this.trainConfig.currentLockWait;
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    my_train_lock = this.trainConfig.lockWaitTime;
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                if this.learnLock == true % re-learn it!
                    if strcmp(this.lockType, 'waitTime')
                        % f = @(conf2, data)(getfield(useLockModel([0.125 0.0001 conf2], data, this.workloadName), 'TimeSpentWaiting'));
                        f = @(conf2, data)(getfield(useLockModel([0.125 0.0001 conf2], data, 'TPCC'), 'TimeSpentWaiting'));
                    elseif strcmp(this.lockType, 'numberOfLocks')
                        f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').LocksBeingHeld'));
                    elseif strcmp(this.lockType, 'numberOfConflicts')
                        f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').totalWaits'));
                    else
                        error(['Invalid lockType:' this.lockType]);
                    end
                    
                    % taskDesc.emIters is hard-coded as 5 for now.
                    domain_cost = barzanCurveFit(f, this.trainConfig.transactionCount, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);
                    lock_conf = [0.125 0.0001 domain_cost];
                elseif ~isempty(this.lockConf)
                    lock_conf = this.lockConf;
                else
                    error('You should either let us re-learn or should give us the lock_conf to use!');
                end
             
                % allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, this.workloadName);
                allPreds = useLockModel(lock_conf, testTransactionCount, 'TPCC');
                if strcmp(this.lockType, 'waitTime')
                    myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);        
                elseif strcmp(this.lockType, 'numberOfLocks')
                    myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    myPredictedLock = sum(allPreds.totalWaits, 2);
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount);
                classifierLinPredictions = barzanLinInvoke(classifierLinModel, testTransactionCount);

                range=1:1:size(this.trainConfig.transactionCount,2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
                blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount(:, comb1).*testTransactionCount(:, comb2)];
                classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
                classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);
                
                treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS);
                treePredictions = barzanRegressTreeInvoke(treeModel, testTPS);

                % kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
                % kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);
                
                % allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, this.workloadName);
                allPreds = useLockModel([1 1 1 1], testTransactionCount, 'TPCC');

                if strcmp(this.lockType, 'waitTime')
                    thomasianPreds = sum(allPreds.TimeSpentWaiting, 2);        
                elseif strcmp(this.lockType, 'numberOfLocks')
                    thomasianPreds = sum(allPreds.LocksBeingHeld, 2);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    thomasianPreds = sum(allPreds.totalWaits, 2);
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                temp = [myPredictedLock classifierLinPredictions classQuadPredictions treePredictions thomasianPreds]; % kccaPredictions omitted.

                % by TPS only for now
                temp = [testTPS temp];

                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {temp(:,2:end)};

                Xlabel = 'TPS';
                Ylabel = 'Total time spent acquiring row locks (seconds)';
                legends = {'Our contention model', 'LR+class', 'quad+class', 'Dec. tree regression', 'Orig. Thomasian'};
                title = 'Lock Prediction';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = transactionCountsToCpuByTPS(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped);
                idx=1:trainMaxThroughputIdx;
                myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                myCpuPred = barzanLinInvoke(myModelP, this.testConfig.transactionCount);
        
                xValuesTest = this.testConfig.TPS;
                xValuesTrain = this.trainConfig.TPS;
                Xlabel = 'TPS';
                
                modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
                predictionsP  = barzanLinInvoke(modelP, this.testConfig.transactionCount);
                
                temp = [xValuesTest this.testConfig.averageCpuUsage predictionsP myCpuPred];
                
                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4)]}
                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = this.trainConfig.averageCpuUsage;

                for i=3:4
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end
                
                legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
                idx=1:trainMaxThroughputIdx;
                myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                myCpuPred = barzanLinInvoke(myModelP, testTransactionCount);
        
                xValuesTest = testTPS;
                xValuesTrain = this.trainConfig.TPS;
                Xlabel = 'TPS';
                
                modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
                predictionsP  = barzanLinInvoke(modelP, testTransactionCount);
                
                temp = [xValuesTest predictionsP myCpuPred];
                
                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};
                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = this.trainConfig.averageCpuUsage;
                
                legends = {'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = transactionCountsToCpuByCounts(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped);
                idx=1:trainMaxThroughputIdx;
                myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                myCpuPred = barzanLinInvoke(myModelP, this.testConfig.transactionCount);
        
                xValuesTest = this.testConfig.transactionCount(:,this.whichTransactionToPlot) ./ this.testConfig.TPS;
                xValuesTrain = this.trainConfig.transactionCount(:,this.whichTransactionToPlot) ./ this.trainConfig.TPS;
                Xlabel = ['Fraction of transaction ' num2str(this.whichTransactionToPlot)];
                
                modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
                predictionsP  = barzanLinInvoke(modelP, this.testConfig.transactionCount);
                
                temp = [xValuesTest this.testConfig.averageCpuUsage predictionsP myCpuPred];
                
                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4)]}
                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = this.trainConfig.averageCpuUsage;

                for i=3:4
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end
                
                legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data'}; 
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);

                idx=1:trainMaxThroughputIdx;
                myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                myCpuPred = barzanLinInvoke(myModelP, testTransactionCount);
        
                xValuesTest = testTransactionCount(:,this.whichTransactionToPlot);
                xValuesTrain = this.trainConfig.transactionCount(:,this.whichTransactionToPlot);
                Xlabel = ['Counts of Transaction ' num2str(this.whichTransactionToPlot)];
                
                modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
                predictionsP  = barzanLinInvoke(modelP, testTransactionCount);
                
                temp = [xValuesTest predictionsP myCpuPred];
                
                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};
                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = this.trainConfig.averageCpuUsage;
                
                legends = {'LR Predictions', 'LR+noise removal Predictions', 'Training data'}; 
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = transactionCountsToIO(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                modelIO = barzanLinSolve(this.trainConfig.diskWrite, this.trainConfig.transactionCount);
                predictionsIO = barzanLinInvoke(modelIO, this.testConfig.transactionCount);

                Xdata = {this.testConfig.TPS};
                Ydata = {[this.testConfig.diskWrite predictionsIO]};
                title = 'Linear model: Avg Physical Writes';
                legends = {'actual writes', 'predicted writes'};
                meanAbsError{1} = mae(predictionsIO, this.testConfig.diskWrite);
                meanRelError{1} = mre(predictionsIO, this.testConfig.diskWrite);
                Ylabel = 'Written data (MB)';
                Xlabel = 'TPS';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                modelIO = barzanLinSolve(this.trainConfig.diskWrite, this.trainConfig.transactionCount);
                predictionsIO = barzanLinInvoke(modelIO, testTransactionCount);

                Xdata = {testTPS};
                Ydata = {[predictionsIO]};
                title = 'Linear model: Avg Physical Writes';
                legends = {'predicted writes'};
                Ylabel = 'Written data (MB)';
                Xlabel = 'TPS';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = transactionCountsToLatency(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                modelL = barzanLinSolve(this.trainConfig.transactionLatency, this.trainConfig.transactionCount);
                predictionsL  = barzanLinInvoke(modelL, this.testConfig.transactionCount);
                
                Xdata = {[1:size(this.testConfig.transactionLatency, 1)]'};
                Ydata = {[this.testConfig.transactionLatency predictionsL]};
                title = 'Linear model (counts only): Latency';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};
                for i = 1:size(this.testConfig.transactionLatency, 2)
                    legends{end+1} = ['actual latency of transaction ' num2str(i)];
                end
                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['predicted latency of transaction ' num2str(i)];
                    meanAbsError{i} = mae(predictionsL(:,i), this.testConfig.transactionLatency(:,i));
                    meanRelError{i} = mre(predictionsL(:,i), this.testConfig.transactionLatency(:,i));
                end
                Ylabel = 'Time (seconds)';
                Xlabel = '';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                modelL = barzanLinSolve(this.trainConfig.transactionLatency, this.trainConfig.transactionCount);
                predictionsL  = barzanLinInvoke(modelL, testTransactionCount);
                
                Xdata = {[1:size(predictionsL, 1)]'};
                Ydata = {[predictionsL]};
                title = 'Linear model (counts only): Latency';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};
                
                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['predicted latency of transaction ' num2str(i)];
                end
                Ylabel = 'Time (seconds)';
                Xlabel = '';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = transactionCountsWaitTimeToLatency(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                modelLw = barzanLinSolve(this.trainConfig.transactionLatency, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                predictionsLw  = barzanLinInvoke(modelLw, [this.testConfig.transactionCount this.testConfig.lockWaitTime]);
                
                ok=[this.testConfig.transactionCount this.testConfig.transactionLatency];
                tempActual = [this.testConfig.transactionCount(:,this.whichTransactionToPlot) this.testConfig.transactionLatency];
                tempPred = [this.testConfig.transactionCount(:,this.whichTransactionToPlot) predictionsLw];
                tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);
                
                Xdata = {tempActual(:,1)};
                Ydata = {tempActual(:,2:end)};
                Xdata{end+1} = tempPred(:,1);
                Ydata{end+1} = tempPred(:,2:end);
                title = 'Linear model (counts + waiting time): Latency';
                legends = {};
                for i = 2:size(tempActual, 2)
                    legends{end+1} = ['actual latency of transaction ' num2str(i-1)];
                end
                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['predicted latency of transaction ' num2str(i-1)];
                    meanAbsError{i-1} = mae(tempPred(:,i), tempActual(:,i));
                    meanRelError{i-1} = mre(tempPred(:,i), tempActual(:,i));
                end

                Xlabel = horzcat('Transaction counts of type ', num2str(this.whichTransactionToPlot));
                Ylabel = 'Time (seconds)';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                
                % Sample again just for this prediction.
                this.testSampleTPS = (this.testMaxTPS - this.testMinTPS) .* rand(size(this.trainConfig.lockWaitTime, 1), 1) + this.testMinTPS;
                this.testSampleTPS(1) = this.testMinTPS;
                this.testSampleTPS(2) = this.testMaxTPS;
                for i=1:size(this.trainConfig.lockWaitTime, 1)
                    this.testSampleTransactionCount(i,:) = this.testMixture .* this.testSampleTPS(i);
                end

                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;    

                modelLw = barzanLinSolve(this.trainConfig.transactionLatency, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                predictionsLw  = barzanLinInvoke(modelLw, [testTransactionCount this.trainConfig.lockWaitTime]); % use lockWaitTime from trainConfig
                
                ok=[testTransactionCount this.trainConfig.transactionLatency];
                % tempActual = [this.testConfig.transactionCount(:,1) this.testConfig.transactionLatency];
                tempPred = [testTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                % tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);
                
                Xdata = {tempPred(:,1)};
                Ydata = {tempPred(:,2:end)};
                
                title = 'Linear model (counts + waiting time): Latency';
                legends = {};
                
                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['predicted latency of transaction ' num2str(i-1)];
                end

                Xlabel = ['Transaction counts of type ' num2str(this.whichTransactionToPlot)];
                Ylabel = 'Time (seconds)';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = blownTransactionCountsToCpu(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                range=1:1:size(this.trainConfig.transactionCount, 2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);
                
                blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
                blownTestC = [this.testConfig.transactionCount this.testConfig.transactionCount.*this.testConfig.transactionCount this.testConfig.transactionCount(:, comb1).*this.testConfig.transactionCount(:, comb2)];

                blownModelP = barzanLinSolve(this.trainConfig.averageCpuUsage, blownTrainC);
                blownPredictionsP = barzanLinInvoke(blownModelP, blownTestC);
                
                Xdata = {[1:size(this.testConfig.averageCpuUsage, 1)]'};
                Ydata = {[this.testConfig.averageCpuUsage blownPredictionsP]};
                legends = {'Actual CPU usage', 'Predicted CPU usage'};
                title = 'Quadratic Model: Average CPU';
                meanAbsError{1} = mae(blownPredictionsP, this.testConfig.averageCpuUsage);
                meanRelError{1} = mre(blownPredictionsP, this.testConfig.averageCpuUsage);
                Xlabel = 'Time';
                Ylabel = 'Average CPU (%)';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;    

                range=1:1:size(this.trainConfig.transactionCount, 2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);
                
                blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
                blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount(:, comb1).*testTransactionCount(:, comb2)];

                blownModelP = barzanLinSolve(this.trainConfig.averageCpuUsage, blownTrainC);
                blownPredictionsP = barzanLinInvoke(blownModelP, blownTestC);
                
                Xdata = {[1:size(blownPredictionsP, 1)]'};
                Ydata = {[blownPredictionsP]};
                legends = {'Predicted CPU usage]'};
                title = 'Quadratic Model: Average CPU';
                Xlabel = 'Time';
                Ylabel = 'Average CPU (%)';
            end
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = blownTransactionCountsToIO(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                range=1:1:size(this.trainConfig.transactionCount,2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
                blownTestC = [this.testConfig.transactionCount this.testConfig.transactionCount.*this.testConfig.transactionCount this.testConfig.transactionCount(:, comb1).*this.testConfig.transactionCount(:, comb2)];

                blownModelIO = barzanLinSolve(this.trainConfig.diskWrite, blownTrainC);
                blownPredictionsIO = barzanLinInvoke(blownModelIO, blownTestC); 

                Xdata = {[1:size(this.testConfig.diskWrite, 1)]'};
                Ydata = {[this.testConfig.diskWrite blownPredictionsIO] ./ 1024 ./ 1024};

                meanAbsError{1} = mae(blownPredictionsIO, this.testConfig.diskWrite) ./ 1024 ./ 1024;
                meanRelError{1} = mre(blownPredictionsIO, this.testConfig.diskWrite);

                title = 'Quadratic Model: Average Physical Writes';
                legends = {'Actual Writes', 'Predicted Writes'};
                Ylabel = 'Written Data (MB)';
                Xlabel = '';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;    

                range=1:1:size(this.trainConfig.transactionCount,2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
                blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount(:, comb1).*testTransactionCount(:, comb2)];

                blownModelIO = barzanLinSolve(this.trainConfig.diskWrite, blownTrainC);
                blownPredictionsIO = barzanLinInvoke(blownModelIO, blownTestC); 

                Xdata = {[1:size(blownPredictionsIO, 1)]'};
                Ydata = {[blownPredictionsIO] ./ 1024 ./ 1024};

                title = 'Quadratic Model: Average Physical Writes';
                legends = {'Predicted Writes'};
                Ylabel = 'Written Data (MB)';
                Xlabel = '';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = linearPrediction(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                trainY = this.trainConfig.logWriteMB;
                testY = this.testConfig.logWriteMB;

                model = barzanLinSolve(trainY, this.trainConfig.transactionCount);
                pred = barzanLinInvoke(model, this.testConfig.transactionCount);

                % xValuesTest = this.testConfig.transactionCount(:,this.whichTransactionToPlot)./ this.testConfig.TPS;
                % xValuesTrain = this.trainConfig.transactionCount(:,this.whichTransactionToPlot)./ this.trainConfig.TPS; 

                xValuesTest = this.testConfig.transactionCount(:,this.whichTransactionToPlot);
                xValuesTrain = this.trainConfig.transactionCount(:,this.whichTransactionToPlot);

                temp = [xValuesTest testY pred];
                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};

                meanAbsError{1} = mae(pred, testY);
                meanRelError{1} = mre(pred, testY);

                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = trainY;

                Ylabel = 'Log Writes (MB)';
                Xlabel = ['Counts of transaction type ' num2str(this.whichTransactionToPlot)];
                legends = {'Actual', 'Predicted', 'Training Data'};
                title = 'Linear Prediction';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;    

                trainY = this.trainConfig.logWriteMB;

                model = barzanLinSolve(trainY, this.trainConfig.transactionCount);
                pred = barzanLinInvoke(model, testTransactionCount);

                xValuesTest = testTransactionCount(:,this.whichTransactionToPlot);
                xValuesTrain = this.trainConfig.transactionCount(:,this.whichTransactionToPlot);

                temp = [xValuesTest pred];
                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2)]};

                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = trainY;

                Ylabel = 'Log Writes (MB)';
                Xlabel = ['Counts of transaction type ' num2str(this.whichTransactionToPlot)];
                legends = {'Predicted', 'Training Data'};
                title = 'Linear Prediction';
            end

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError] = physicalReadPrediction(this)
            meanAbsError = {};
            meanRelError = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                cacheMissRate = this.testConfig.logicalReads ./ this.testConfig.physicalReads;
                normalization = mean(this.testConfig.physicalReadsMB) ./ mean(cacheMissRate);

                xValuesTest = this.testConfig.TPS;

                temp = [xValuesTest this.testConfig.physicalReadsMB cacheMissRate*normalization];
                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};

                meanAbsError{1} = mae(cacheMissRate*normalization, this.testConfig.physicalReadsMB);
                meanRelError{1} = mre(cacheMissRate*normalization, this.testConfig.physicalReadsMB);

                title = 'Physical read volume and cache miss rate';
                legends = {'Physical Read Volume', 'Cache Miss Rate'};
                Ylabel = 'Data Read (MB per sec)';
                Xlabel = 'TPS';
            end
        end % end function
        
    end % end methods

end % end classdef