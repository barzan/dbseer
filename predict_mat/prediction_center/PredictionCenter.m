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

classdef PredictionCenter < handle

    properties (Constant = true)
        TEST_MODE_MIXTURE_TPS = 0;
        TEST_MODE_DATASET = 1;
        NUM_TPS_SAMPLES = 100;
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
        testWorkloadRatio

        testSampleTPS
        testSampleTransactionCount

        throttleLatencyType
        throttleTargetLatency
        throttleTargetTransactionIndex
        throttlePenalty
        throttleIndividualTransactions = false;
    end

    methods
		function set.testMixture(this, value)
			this.testMixture = value ./ sum(value);
		end

        function calculateTestTPSMixture(this)
            this.testSampleTPS = (this.testMaxTPS - this.testMinTPS) .* rand(this.NUM_TPS_SAMPLES, 1) + this.testMinTPS;
            % this.testSampleTPS(1) = this.testMinTPS;
            % this.testSampleTPS(2) = this.testMaxTPS;
			this.testSampleTPS = sortrows(this.testSampleTPS, 1);
            for i=1:this.NUM_TPS_SAMPLES
                this.testSampleTransactionCount(i,:) = this.testMixture .* this.testSampleTPS(i);
            end
        end

        function initialize(this)
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
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = performPrediction(this)


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
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.flushRatePredictionByTPS;
            elseif strcmp(this.taskName, 'FlushRatePredictionByCounts')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.flushRatePredictionByCounts;
            elseif strcmp(this.taskName, 'MaxThroughputPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.maxThroughputPrediction;
            elseif strcmp(this.taskName, 'TransactionCountsToCpuByTPS')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsToCpuByTPS;
            elseif strcmp(this.taskName, 'TransactionCountsToCpuByCounts')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsToCpuByCounts;
            elseif strcmp(this.taskName, 'TransactionCountsToIO')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsToIO;
            elseif strcmp(this.taskName, 'TransactionCountsToLatency')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsToLatency;
            elseif strcmp(this.taskName, 'TransactionCountsToLatency99')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsToLatency99;
            elseif strcmp(this.taskName, 'TransactionCountsToLatencyMedian')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsToLatencyMedian;
            elseif strcmp(this.taskName, 'TransactionCountsWaitTimeToLatency')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsWaitTimeToLatency;
            elseif strcmp(this.taskName, 'TransactionCountsWaitTimeToLatency99')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsWaitTimeToLatency99;
            elseif strcmp(this.taskName, 'TransactionCountsWaitTimeToLatencyMedian')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.transactionCountsWaitTimeToLatencyMedian;
            elseif strcmp(this.taskName, 'BlownTransactionCountsToCpu')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.blownTransactionCountsToCpu;
            elseif strcmp(this.taskName, 'BlownTransactionCountsToIO')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.blownTransactionCountsToIO;
            elseif strcmp(this.taskName, 'LinearPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.linearPrediction;
            elseif strcmp(this.taskName, 'PhysicalReadPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.physicalReadPrediction;
            elseif strcmp(this.taskName, 'LockPrediction')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.lockPrediction;
            elseif strcmp(this.taskName, 'WhatIfAnalysisLatency')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.whatIfAnalysisLatency;
            elseif strcmp(this.taskName, 'WhatIfAnalysisCPU')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.whatIfAnalysisCPU;
            elseif strcmp(this.taskName, 'WhatIfAnalysisIO')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.whatIfAnalysisIO;
            elseif strcmp(this.taskName, 'WhatIfAnalysisFlushRate')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.whatIfAnalysisFlushRate;
            elseif strcmp(this.taskName, 'BottleneckAnalysisMaxThroughput')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.bottleneckAnalysisMaxThroughput;
            elseif strcmp(this.taskName, 'BottleneckAnalysisResource')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.bottleneckAnalysisResource;
            elseif strcmp(this.taskName, 'BlameAnalysisCPU')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.blameAnalysisCPU;
            elseif strcmp(this.taskName, 'BlameAnalysisIO')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.blameAnalysisIO;
            elseif strcmp(this.taskName, 'BlameAnalysisLock')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.blameAnalysisLock;
            elseif strcmp(this.taskName, 'ThrottlingAnalysis')
                [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = this.throttlingAnalysis;
            else
                error(strcat('Unsupported task name: ', this.taskName));
            end
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = flushRatePredictionByTPS(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            treePred = [];
            linPred = [];
            classLinPred = [];
            nnPred = [];
            myPred = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                mv = this.testConfig.mv;

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)

                    treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), sum(this.trainConfig.transactionCount{i}, 2));
                    %naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.TPS);
                    betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    if exist('fitnet') == 5
                        nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    end

                    for j=1:size(this.testConfig.mv.numOfTransType, 2)
                        treePred = horzcat(treePred, barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount{j}));
                        classLinPred = horzcat(classLinPred, barzanLinInvoke(betterLinModel, this.testConfig.transactionCount{j}));
                        linPred = horzcat(linPred, barzanLinInvoke(naiveLinModel, this.testConfig.TPS(:,j)));
                        if exist('fitnet') == 5
                            nnPred = horzcat(nnPred, barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount{j}));
                        end
                    end
                    %kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.testConfig.transactionType, 'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                    %emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                    %[emp1 emp2 kccaTrainC kccaTrainCainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                    %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                    %kccaPred = barzanKccaInvoke(kccaModel, testC);

                    %if exist('fitnet') == 5
                        %nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                        %nnPred = horzcat(nnPred, barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount));
                    %end
                end

                %config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                for j=1:size(this.testConfig.mv.numOfTransType, 2)
                    myPred = horzcat(myPred, cfFlushRateApprox(config, this.testConfig.transactionCount{j}));
                end

                %if exist('fitnet') == 5
                    %temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                %else
                    %temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred]; % kccaPred is not included for now.
                %end
                if exist('fitnet') == 5
                    temp = [sum(this.testConfig.pagesFlushed,2) sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2) sum(nnPred,2)]; % kccaPred is not included for now.
                else
                    temp = [sum(this.testConfig.pagesFlushed,2) sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)]; % kccaPred is not included for now.
                end
                temp = [sum(this.testConfig.TPS,2) temp];

                temp = sortrows(temp,1);

                Xdata = {temp(:,1)};
                if exist('fitnet') == 5
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6) temp(:,7)]};
                    legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};
    				errorHeader = legends(2:6);
                    for i=3:7
                        meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                        meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                    end
                else
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};
                    legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression'};
    				errorHeader = legends(2:5);
                    for i=3:6
                        meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                        meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                    end
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

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)

                    treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    treePred = horzcat(treePred, barzanRegressTreeInvoke(treeModel, testTransactionCount .* this.trainConfig.TPSRatio{i}));

                    naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.TPS(:,i));
                    linPred = horzcat(linPred, barzanLinInvoke(naiveLinModel, testTPS .* this.trainConfig.TPSRatio{i}));

                    betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    classLinPred = horzcat(classLinPred, barzanLinInvoke(betterLinModel, testTransactionCount .* this.trainConfig.TPSRatio{i}));

                    % kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.testConfig.transactionType, 'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                    %emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                    %[emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                    %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                    %kccaPred = barzanKccaInvoke(kccaModel, testC);

                    % check the availability of 'fitnet' function (Neural Network Toolbox).
                    if exist('fitnet') == 5
                        nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                        nnPred = horzcat(nnPred, barzanNeuralNetInvoke(nnModel, testTransactionCount .* this.trainConfig.TPSRatio{i}));
                    end

                    %config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                    config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                    myPred = horzcat(myPred, cfFlushRateApprox(config, testTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                if exist('fitnet') == 5
                    temp = [sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2) sum(nnPred,2)]; % kccaPred is not included for now.
                else
                    temp = [sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)]; % kccaPred is not included for now.
                end
                temp = [testTPS temp];

                temp = sortrows(temp,1);

                Xdata = {temp(:,1)};
                if exist('fitnet') == 5
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};
                    legends = {'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};
                else
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5)]};
                    legends = {'LR', 'LR+classification', 'Our model', 'Tree regression'};
                end

                title = horzcat('Flush rate prediction with transaction mixture = ', mat2str(this.testMixture), ', Min TPS = ', num2str(this.testMinTPS), ', Max TPS = ', num2str(this.testMaxTPS));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = 'TPS';
            end
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = flushRatePredictionByCounts(this)

            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            treePred = [];
            linPred = [];
            classLinPred = [];
            nnPred = [];
            myPred = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                mv = this.testConfig.mv;

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.TPS(:,i));
                    betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    if exist('fitnet') == 5
                        nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    end

                    for j=1:size(this.testConfig.mv.numOfTransType, 2)
                        treePred = horzcat(treePred, barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount{j}));
                        classLinPred = horzcat(classLinPred, barzanLinInvoke(betterLinModel, this.testConfig.transactionCount{j}));
                        linPred = horzcat(linPred, barzanLinInvoke(naiveLinModel, this.testConfig.TPS(:,j)));
                        if exist('fitnet') == 5
                            nnPred = horzcat(nnPred, barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount{j}));
                        end
                    end

                    %kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.trainConfig.transactionType,  'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                    %emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                    %[emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                    %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                    %kccaPred = barzanKccaInvoke(kccaModel, testC);

                    %if exist('fitnet') == 5
                        %nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
                        %nnPred = barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount);
                    %end
                end

                %config = struct('io_conf', this.ioConf, 'workloadName', this.workloadName);
                config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                %myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);
                totalTxCount = [];
                for j=1:size(this.testConfig.mv.numOfTransType, 2)
                    myPred = horzcat(myPred, cfFlushRateApprox(config, this.testConfig.transactionCount{j}));
                    totalTxCount = vertcat(totalTxCount, this.testConfig.transactionCount{j})
                end
                totalTPS = sum(this.testConfig.TPS, 2);

                %if exist('fitnet') == 5
                    %temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                %else
                    %temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred]; % kccaPred is not included for now.
                %end
                if exist('fitnet') == 5
                    temp = [sum(this.testConfig.pagesFlushed,2) sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2) sum(nnPred,2)]; % kccaPred is not included for now.
                else
                    temp = [sum(this.testConfig.pagesFlushed,2) sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)]; % kccaPred is not included for now.
                end
                %temp = [this.testConfig.transactionCount(:,this.whichTransactionToPlot)./this.testConfig.TPS temp];
                temp = [totalTxCount(:,this.whichTransactionToPlot)./totalTPS temp];

                temp = sortrows(temp,1);

                Xdata = {temp(:,1)};

                if exist('fitnet') == 5
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6) temp(:,7)]};

                    for i=3:7
                        meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                        meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                    end

                    legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};
    				errorHeader = legends(2:6);
                else
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};

                    for i=3:6
                        meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                        meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                    end

                    legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression'};
    				errorHeader = legends(2:5);
                end

                title = horzcat('Flush rate prediction with # test points = ', num2str(size(this.testConfig.transactionCount{1},1)));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = ['Ratio of transaction ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS

                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)

                    treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    treePred = horzcat(treePred, barzanRegressTreeInvoke(treeModel, testTransactionCount .* this.trainConfig.TPSRatio{i}));

                    naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.TPS(:,i));
                    linPred = horzcat(linPred, barzanLinInvoke(naiveLinModel, testTPS .* this.trainConfig.TPSRatio{i}));

                    betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    classLinPred = horzcat(classLinPred, barzanLinInvoke(betterLinModel, testTransactionCount .* this.trainConfig.TPSRatio{i}));

                    % kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.trainConfig.transactionType,  'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
                    % emp = zeros(size(this.trainConfig.transactionCount,1), 0);
                    % [emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

                    %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
                    %kccaPred = barzanKccaInvoke(kccaModel, testC);

                    % check the availability of 'fitnet' function (Neural Network Toolbox).
                    if exist('fitnet') == 5
                        nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                        nnPred = horzcat(nnPred, barzanNeuralNetInvoke(nnModel, testTransactionCount .* this.trainConfig.TPSRatio{i}));
                    end

                    %config = struct('io_conf', this.ioConf, 'workloadName', this.workloadName);
                    config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                    myPred = horzcat(myPred, cfFlushRateApprox(config, testTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                %if exist('fitnet') == 5
                    %temp = [linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
                %else
                    %temp = [linPred classLinPred myPred treePred]; % kccaPred is not included for now.
                %end
                if exist('fitnet') == 5
                    temp = [sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2) sum(nnPred,2)]; % kccaPred is not included for now.
                else
                    temp = [sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)]; % kccaPred is not included for now.
                end
                temp = [testTransactionCount(:,this.whichTransactionToPlot) temp];

                temp = sortrows(temp,1);

                this.testVar = temp;

                Xdata = {temp(:,1)};
                if exist('fitnet') == 5
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};

                    legends = {'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};
                else
                    Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5)]};

                    legends = {'LR', 'LR+classification', 'Our model', 'Tree regression'};
                end

                title = horzcat('Flush rate prediction with transaction mixture = ', mat2str(this.testMixture), ', Min TPS = ', num2str(this.testMinTPS), ', Max TPS = ', num2str(this.testMaxTPS));
                Ylabel = 'Average # of page flush per seconds';
                Xlabel = ['# of transaction ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];

            end
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = maxThroughputPrediction(this)

            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            actualThr = 0;
            cpuCLThroughput = 0;
            cpuCUThroughput = 0;
            myCpuCLThroughput = 0;
            myCpuCUThroughput = 0;

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                range = (1:15000)';
                maxFlushRate = 300; % DY: This is hard-coded... should we do something about this?

                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                %cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                %myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    modelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});

                    [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped(:,i));
                    [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped(:,i));

                    actual = testMaxThroughput;
                    if isempty(actual)
                        actual = max(sum(this.testConfig.TPSUngrouped, 2));
                    end
                    actualThr = actualThr + actual;

                    if exist('trainMaxThroughputIdx') && ~isempty(trainMaxThroughputIdx)
                        idx=1:trainMaxThroughputIdx;
                    else
                        idx=1:size(this.trainConfig.averageCpuUsage,1);
                    end

                    %CPU-based throughput with classification
                    cpuC = barzanLinInvoke(modelP, range*this.testConfig.transactionMixture);
                    cpuCLThroughput = cpuCLThroughput + find(cpuC>88 & cpuC<90, 1, 'last');
                    cpuCUThroughput = cpuCUThroughput + find(cpuC>98 & cpuC<100, 1, 'last');

                    %CPU-based without classification
                    cpuTModel = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.TPS(:,i));
                    cpuT = barzanLinInvoke(cpuTModel, range);
                    cpuTLThroughput = cpuTLThroughput + find(cpuT>88 & cpuT<90, 1, 'last');
                    cpuTUThroughput = cpuTUThroughput + find(cpuT>98 & cpuT<100, 1, 'last');

                    %myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,i), this.trainConfig.transactionCount(idx,i));
                    myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    myCpuC = barzanLinInvoke(myModelP, range*this.testConfig.transactionMixture);
                    myCpuCLThroughput = myCpuCLThroughput + find(myCpuC>88 & myCpuC<90, 1, 'last');
                    myCpuCUThroughput = myCpuCUThroughput + find(myCpuC>98 & myCpuC<100, 1, 'last');

                    %myCpuCLThroughput = find(myCpuC>44 & myCpuC<45, 1, 'last');
                    %myCpuCUThroughput = find(myCpuC>59 & myCpuC<50, 1, 'last');

                    %Linear IO-based throughput
                    modelFlushRate = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testConfig.transactionMixture);
                    linFlushRateThr = find(linFlushRate<maxFlushRate, 1, 'last');
                    if isempty(linFlushRateThr); linFlushRateThr=0; end

                    linFlushRateThr = linFlushRateThr * this.trainConfig.TPSRatio{i};
                    linFlushRateThroughput = linFlushRateThroughput + linFlushRateThr;
                end

                %Our IO-based throughput
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);

                %Lock-based throughput
                % getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
                getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
                concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*this.testConfig.transactionMixture, 160, getConcurrencyLebel_conf);

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
                    errorHeader{end+1} = 'Max Throughput based on adjusted LR for CPU+classification';
                    meanAbsError{end+1} = mae(cpuCLThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuCLThroughput, actualThr);
                end
                if ~isempty(cpuCUThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuCUThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput on LR for CPU+classification';
                    errorHeader{end+1} = 'Max Throughput on LR for CPU+classification';
                    meanAbsError{end+1} = mae(cpuCUThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuCUThroughput, actualThr);
                end
                if ~isempty(cpuTLThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuTLThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput on adjusted LR for CPU';
                    errorHeader{end+1} = 'Max Throughput on adjusted LR for CPU';
                    meanAbsError{end+1} = mae(cpuTLThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuTLThroughput, actualThr);
                end
                if ~isempty(cpuTUThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(cpuTUThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on LR for CPU';
                    errorHeader{end+1} = 'Max Throughput based on LR for CPU';
                    meanAbsError{end+1} = mae(cpuTUThroughput, actualThr);
                    meanRelError{end+1} = mre(cpuTUThroughput, actualThr);
                end
                if ~isempty(myFlushRateThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(myFlushRateThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on our flush rate model';
                    errorHeader{end+1} = 'Max Throughput based on our flush rate model';
                    meanAbsError{end+1} = mae(myFlushRateThroughput, actualThr);
                    meanRelError{end+1} = mre(myFlushRateThroughput, actualThr);
                end
                if ~isempty(linFlushRateThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(linFlushRateThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on LR for flush rate';
                    errorHeader{end+1} = 'Max Throughput based on LR for flush rate';
                    meanAbsError{end+1} = mae(linFlushRateThroughput, actualThr);
                    meanRelError{end+1} = mre(linFlushRateThroughput, actualThr);
                end
                if ~isempty(concurrencyThroughput)
                    Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                    Ydata{end+1} = repmat(concurrencyThroughput, num_row, num_col);
                    legends{end+1} = 'Max Throughput based on our contention model';
                    errorHeader{end+1} = 'Max Throughput based on our contention model';
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
                maxFlushRate = 6000;

                actualThr = [];
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                %cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                %myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);

                for i=size(this.trainConfig.mv.numOfTransType, 2)

                    modelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});

                    % [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
                    [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(sum(this.trainConfig.TPSUngrouped,2));


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
                    cpuTModel = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.TPS(:,i));
                    cpuT = barzanLinInvoke(cpuTModel, range);
                    cpuTLThroughput = find(cpuT>88 & cpuT<90, 1, 'last');
                    cpuTUThroughput = find(cpuT>98 & cpuT<100, 1, 'last');

                    %myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
                    myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    myCpuC = barzanLinInvoke(myModelP, range*this.testMixture);
                    myCpuCLThroughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
                    myCpuCUThroughput = find(myCpuC>98 & myCpuC<100, 1, 'last');

                    %myCpuCLThroughput = find(myCpuC>44 & myCpuC<45, 1, 'last');
                    %myCpuCUThroughput = find(myCpuC>59 & myCpuC<50, 1, 'last');

                    %Linear IO-based throughput
                    modelFlushRate = barzanLinSolve(this.trainConfig.pagesFlushed(:,i), this.trainConfig.transactionCount{i});
                    linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testMixture);
                    linFlushRateThr = find(linFlushRate<maxFlushRate, 1, 'last');
                    if isempty(linFlushRateThr); linFlushRateThr=0; end

                    linFlushRateThr = linFlushRateThr * this.trainConfig.TPSRatio{i};
                    linFlushRateThroughput = linFlushRateThroughput + linFlushRateThr;
                end

                %Our IO-based throughput
                % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
                cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
                myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);

                %Lock-based throughput
                % getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
                getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
                concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*this.testMixture, 160, getConcurrencyLebel_conf);

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

        % NOTE: currently only uses first server info from each test and train configs.
        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = lockPrediction(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                if strcmp(this.lockType, 'waitTime')
                    my_train_lock = this.trainConfig.lockWaitTime(:,1);
                    my_test_lock = this.testConfig.lockWaitTime(:,1);
                elseif strcmp(this.lockType, 'numberOfLocks')
                    my_train_lock = this.trainConfig.currentLockWait(:,1);
                    my_test_lock = this.testConfig.currentLockWait(:,1);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    my_train_lock = this.trainConfig.lockWaitTime(:,1);
                    my_test_lock = this.testConfig.lockWaitTime(:,1);
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
                    domain_cost = barzanCurveFit(f, this.trainConfig.transactionCount{1}, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);
                    lock_conf = [0.125 0.0001 domain_cost];
                elseif ~isempty(this.lockConf)
                    lock_conf = this.lockConf;
                else
                    error('You should either let us re-learn or should give us the lock_conf to use!');
                end

                % allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, this.workloadName);
                allPreds = useLockModel(lock_conf, this.testConfig.transactionCount{1}, 'TPCC');
                if strcmp(this.lockType, 'waitTime')
                    myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);
                elseif strcmp(this.lockType, 'numberOfLocks')
                    myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    myPredictedLock = sum(allPreds.totalWaits, 2);
                else
                    error(['Invalid lockType:' this.lockType]);
                end

                classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount{1});
                classifierLinPredictions = barzanLinInvoke(classifierLinModel, this.testConfig.transactionCount{1});

                range=1:1:size(this.trainConfig.transactionCount{1},2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.transactionCount{1} this.trainConfig.transactionCount{1}.*this.trainConfig.transactionCount{1} this.trainConfig.transactionCount{1}(:, comb1).*this.trainConfig.transactionCount{1}(:, comb2)];
                blownTestC = [this.testConfig.transactionCount{1} this.testConfig.transactionCount{1}.*this.testConfig.transactionCount{1} this.testConfig.transactionCount{1}(:, comb1).*this.testConfig.transactionCount{1}(:, comb2)];
                classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
                classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);

                treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS(:,1));
                treePredictions = barzanRegressTreeInvoke(treeModel, this.testConfig.TPS(:,1));

                % kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
                % kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);

                % allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, this.workloadName);
                allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount{1}, 'TPCC');

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
                temp = [this.testConfig.TPS(:,1) temp];

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
				errorHeader = legends(2:6);
                title = 'Lock Prediction';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                if strcmp(this.lockType, 'waitTime')
                    my_train_lock = this.trainConfig.lockWaitTime(:,1);
                elseif strcmp(this.lockType, 'numberOfLocks')
                    my_train_lock = this.trainConfig.currentLockWait(:,1);
                elseif strcmp(this.lockType, 'numberOfConflicts')
                    my_train_lock = this.trainConfig.lockWaitTime(:,1);
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
                    domain_cost = barzanCurveFit(f, this.trainConfig.transactionCount{1}, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);
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

                classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount{1});
                classifierLinPredictions = barzanLinInvoke(classifierLinModel, testTransactionCount);

                range=1:1:size(this.trainConfig.transactionCount{1},2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.transactionCount{1} this.trainConfig.transactionCount{1} .*this.trainConfig.transactionCount{1}  this.trainConfig.transactionCount{1}(:, comb1).*this.trainConfig.transactionCount{1}(:, comb2)];
                blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount(:, comb1).*testTransactionCount(:, comb2)];
                classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
                classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);

                treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS(:,1));
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

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsToCpuByTPS(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            myCpuPred = [];
            predictionsP = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                %[trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                %[testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped);
                %idx=1:trainMaxThroughputIdx;
                %idx=1:size(this.trainConfig.averageCpuUsage, 1);
                %myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));

                for i=size(this.trainConfig.mv.numOfTransType, 2)
                    myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    myCpuPred = horzcat(myCpuPred, barzanLinInvoke(myModelP, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i}));

                    modelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    predictionsP  = horzcat(predictionsP, barzanLinInvoke(modelP, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                xValuesTest = sum(this.testConfig.TPS, 2);
                xValuesTrain = sum(this.trainConfig.TPS, 2);
                Xlabel = 'TPS';

                temp = [xValuesTest mean(this.testConfig.averageCpuUsage,2) mean(predictionsP,2) mean(myCpuPred,2)];

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4)]};
				% temporary remove
                %Xdata{end+1} = xValuesTrain;
                %Ydata{end+1} = this.trainConfig.averageCpuUsage;

                for i=3:4
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end

                legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions'};
                %legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
                errorHeader = legends(2:3);
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                %[trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                %[testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
                %idx=1:trainMaxThroughputIdx;
                %idx=1:size(this.trainConfig.averageCpuUsage, 1);
                %myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));

                for i=size(this.trainConfig.mv.numOfTransType, 2)
                    myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    myCpuPred = horzcat(myCpuPred, barzanLinInvoke(myModelP, testTransactionCount .* this.trainConfig.TPSRatio{i}));

                    modelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    predictionsP = horzcat(predictionsP, barzanLinInvoke(modelP, testTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                xValuesTest = testTPS;
                xValuesTrain = sum(this.trainConfig.TPS, 2);
                Xlabel = 'TPS';

                temp = [xValuesTest mean(predictionsP,2) mean(myCpuPred,2)];

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};
                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = this.trainConfig.averageCpuUsage;

                legends = {'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsToCpuByCounts(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            myCpuPred = [];
            predictionsP = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                %[trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                %[testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped);
                %idx=1:trainMaxThroughputIdx;
                %idx=1:size(this.trainConfig.averageCpuUsage, 1);
                %myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));

                for i=size(this.trainConfig.mv.numOfTransType, 2)
                    myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    myCpuPred = horzcat(myCpuPred, barzanLinInvoke(myModelP, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i}));

                    modelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    predictionsP = horzcat(predictionsP, barzanLinInvoke(modelP, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                xValuesTest = this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) ./ sum(this.testConfig.TPS, 2);
                xValuesTrain = this.trainConfig.totalTransactionCount(:,this.whichTransactionToPlot) ./ sum(this.trainConfig.TPS, 2);
                Xlabel = ['Fraction of transaction ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];

                temp = [xValuesTest mean(this.testConfig.averageCpuUsage,2) mean(predictionsP,2) mean(myCpuPred,2)];
				temp = sortrows(temp, 1);

				temp2 = [xValuesTrain mean(this.trainConfig.averageCpuUsage,2)];
				temp2 = sortrows(temp2, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3) temp(:,4)]};
                %Xdata{end+1} = xValuesTrain;
                %Ydata{end+1} = this.trainConfig.averageCpuUsage;

				% temporary remove
                %Xdata{end+1} = temp2(:,1);
                %Ydata{end+1} = temp2(:,2);

                for i=3:4
                    meanAbsError{i-2} = mae(temp(:,i), temp(:,2));
                    meanRelError{i-2} = mre(temp(:,i), temp(:,2));
                end

                %legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
                legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions'};
				errorHeader = legends(2:3);
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                %[trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
                %[testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);

                %idx=1:trainMaxThroughputIdx;
                %idx=1:size(this.trainConfig.averageCpuUsage, 1);
                %myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));

                for i=size(this.trainConfig.mv.numOfTransType, 2)
                    myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    myCpuPred = horzcat(myCpuPred, barzanLinInvoke(myModelP, testTransactionCount .* this.trainConfig.TPSRatio{i}));

                    modelP = barzanLinSolve(this.trainConfig.averageCpuUsage(:,i), this.trainConfig.transactionCount{i});
                    predictionsP = horzcat(predictionsP, barzanLinInvoke(modelP, testTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                xValuesTest = testTransactionCount(:,this.whichTransactionToPlot);
                xValuesTrain = this.trainConfig.totalTransactionCount(:,this.whichTransactionToPlot);
                Xlabel = ['Counts of Transaction ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];

                temp = [xValuesTest mean(predictionsP,2) mean(myCpuPred,2)];

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};
                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = mean(this.trainConfig.averageCpuUsage,2);

                legends = {'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
                Ylabel = 'Average CPU (%)';
                title = 'Linear model: Avg CPU';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsToIO(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};
            predictionsIO = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET

                for i=size(this.trainConfig.mv.numOfTransType, 2)
                    modelIO = barzanLinSolve(this.trainConfig.diskWrite(:,i), this.trainConfig.transactionCount{i});
                    predictionsIO = horzcat(predictionsIO, barzanLinInvoke(modelIO, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i}));
                end

                Xdata = {sum(this.testConfig.TPS,2)};
                Ydata = {[sum(this.testConfig.diskWrite,2) sum(predictionsIO,2)]};
                title = 'Linear model: Avg Physical Writes';
                legends = {'Actual Writes', 'Predicted Writes'};
                meanAbsError{1} = mae(sum(predictionsIO,2), sum(this.testConfig.diskWrite,2));
                meanRelError{1} = mre(sum(predictionsIO,2), sum(this.testConfig.diskWrite,2));
				errorHeader(1) = legends(2);
                Ylabel = 'Written data (MB)';
                Xlabel = 'TPS';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                modelIO = barzanLinSolve(sum(this.trainConfig.diskWrite,2), this.trainConfig.totalTransactionCount);
                predictionsIO = barzanLinInvoke(modelIO, testTransactionCount);

                Xdata = {testTPS};
                Ydata = {[predictionsIO]};
                title = 'Linear model: Avg Physical Writes';
                legends = {'Predicted Writes'};
                Ylabel = 'Written data (MB)';
                Xlabel = 'TPS';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsToLatency(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsL = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    modelL = barzanLinSolve(this.trainConfig.transactionLatency{i}, this.trainConfig.transactionCount{i});
                    if isempty(predictionsL)
                        predictionsL = barzanLinInvoke(modelL, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i});
                    else
                        predicitonsL = predictionsL + barzanLinInvoke(modelL, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i});
                    end
                end
                predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);

                %Xdata = {[1:size(this.testConfig.transactionLatency, 1)]'};
                Xdata = {[1:size(this.testConfig.totalTransactionLatency, 1)]'};
                Ydata = {[this.testConfig.totalTransactionLatency predictionsL]};
                title = 'Linear model (counts only): Latency';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};
                for i = 1:size(this.testConfig.totalTransactionLatency, 2)
                    legends{end+1} = ['Actual Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction'];
                end
                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                    errorHeader{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                    meanAbsError{i} = mae(predictionsL(:,i), this.testConfig.transactionLatency(:,i));
                    meanRelError{i} = mre(predictionsL(:,i), this.testConfig.transactionLatency(:,i));
                end
                Ylabel = 'Latency (seconds)';
                Xlabel = 'Time (seconds)';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                %modelL = barzanLinSolve(this.trainConfig.transactionLatency, this.trainConfig.transactionCount);
                %predictionsL  = barzanLinInvoke(modelL, testTransactionCount);
                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    modelL = barzanLinSolve(this.trainConfig.transactionLatency{i}, this.trainConfig.transactionCount{i});
                    if isempty(predictionsL)
                        predictionsL = barzanLinInvoke(modelL, testTransactionCount .* this.trainConfig.TPSRatio{i});
                    else
                        predicitonsL = predictionsL + barzanLinInvoke(modelL, testTransactionCount .* this.trainConfig.TPSRatio{i});
                    end
                end
                predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);

                %Xdata = {[1:size(predictionsL, 1)]'};
                Xdata = {[testTPS]};
                Ydata = {[predictionsL]};
                title = 'Linear model (counts only): Latency';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};

                for i = 1:size(predictionsL, 2)
                    %legends{end+1} = ['predicted latency of transaction ' num2str(i)];
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                end
                Ylabel = 'Latency (seconds)';
                Xlabel = 'TPS';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsToLatency99(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsL = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET

                %modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount);
                %predictionsL  = barzanLinInvoke(modelL, this.testConfig.transactionCount);

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],7);
                    train_latencies(isnan(train_latencies)) = 0;

                    modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount{i});
                    if isempty(predictionsL)
                        predictionsL = barzanLinInvoke(modelL, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i});
                    else
                        predicitonsL = predictionsL + barzanLinInvoke(modelL, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i});
                    end
                end
                predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);

                test_latencies = [];
                for i=1:size(this.testConfig.mv.numOfTransType, 2)
                    if isempty(test_latencies)
                        test_latencies = this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],7);
                    else
                        test_latencies = test_latencies + this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],7);
                    end
                end
                test_latencies(isnan(test_latencies)) = 0;

                Xdata = {[1:size(train_latencies, 1)]'};
                Ydata = {[test_latencies predictionsL]};
                title = 'Linear model (counts only): Latency (99% Quantile)';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};
                for i = 1:size(train_latencies, 2)
                    %legends{end+1} = ['actual latency of transaction ' num2str(i)];
                    legends{end+1} = ['Actual Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction'];
                end
                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                    errorHeader{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                    meanAbsError{i} = mae(predictionsL(:,i), test_latencies(:,i));
                    meanRelError{i} = mre(predictionsL(:,i), test_latencies(:,i));
                end
                Ylabel = 'Latency (seconds)';
                Xlabel = 'Time (seconds)';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                %modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount);
                %predictionsL  = barzanLinInvoke(modelL, testTransactionCount);

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],7);
                    train_latencies(isnan(train_latencies)) = 0;
                    modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount{i});
                    if isempty(predictionsL)
                        predictionsL = barzanLinInvoke(modelL, testTransactionCount .* this.trainConfig.TPSRatio{i});
                    else
                        predicitonsL = predictionsL + barzanLinInvoke(modelL, testTransactionCount .* this.trainConfig.TPSRatio{i});
                    end
                end
                predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);

                %Xdata = {[1:size(predictionsL, 1)]'};
                Xdata = {[testTPS]};
                Ydata = {[predictionsL]};
                title = 'Linear model (counts only): Latency (99% Quantile)';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};

                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                end
                Ylabel = 'Latency (seconds)';
                Xlabel = 'TPS';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsToLatencyMedian(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsL = [];

			%train_latencies = this.trainConfig.transactionLatencyPercentile.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
			%train_latencies(isnan(train_latencies)) = 0;

            if this.testMode == PredictionCenter.TEST_MODE_DATASET

                %modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount);
                %predictionsL  = barzanLinInvoke(modelL, this.testConfig.transactionCount);

				%test_latencies = this.testConfig.transactionLatencyPercentile.latenciesPCtile(:,[this.testConfig.transactionType+1],3);
				%test_latencies(isnan(test_latencies)) = 0;

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
                    train_latencies(isnan(train_latencies)) = 0;

                    modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount{i});
                    if isempty(predictionsL)
                        predictionsL = barzanLinInvoke(modelL, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i});
                    else
                        predicitonsL = predictionsL + barzanLinInvoke(modelL, this.testConfig.totalTransactionCount .* this.trainConfig.TPSRatio{i});
                    end
                end
                predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);

                test_latencies = [];
                for i=1:size(this.testConfig.mv.numOfTransType, 2)
                    if isempty(test_latencies)
                        test_latencies = this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],3);
                    else
                        test_latencies = test_latencies + this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],3);
                    end
                end
                test_latencies(isnan(test_latencies)) = 0;

                Xdata = {[1:size(test_latencies, 1)]'};
                Ydata = {[test_latencies predictionsL]};
                title = 'Linear model (counts only): Latency (Median)';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};
                for i = 1:size(test_latencies, 2)
                    legends{end+1} = ['Actual Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction'];
                end
                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                    errorHeader{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                    meanAbsError{i} = mae(predictionsL(:,i), test_latencies(:,i));
                    meanRelError{i} = mre(predictionsL(:,i), test_latencies(:,i));
                end
                Ylabel = 'Latency (seconds)';
                Xlabel = 'Time (seconds)';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                %modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount);
                %predictionsL  = barzanLinInvoke(modelL, testTransactionCount);

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
                    train_latencies(isnan(train_latencies)) = 0;
                    modelL = barzanLinSolve(train_latencies, this.trainConfig.transactionCount{i});
                    if isempty(predictionsL)
                        predictionsL = barzanLinInvoke(modelL, testTransactionCount .* this.trainConfig.TPSRatio{i});
                    else
                        predicitonsL = predictionsL + barzanLinInvoke(modelL, testTransactionCount .* this.trainConfig.TPSRatio{i});
                    end
                end
                predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);

                %Xdata = {[1:size(predictionsL, 1)]'};
                Xdata = {[testTPS]};
                Ydata = {[predictionsL]};
                title = 'Linear model (counts only): Latency (Median)';
                % legends = {'actual latency', 'predicted latency'};
                legends = {};

                for i = 1:size(predictionsL, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
                end
                Ylabel = 'Latency (seconds)';
                Xlabel = 'TPS';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsWaitTimeToLatency(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsLw = [];

            if this.testMode == PredictionCenter.TEST_MODE_DATASET

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    modelLw = barzanLinSolve(this.trainConfig.transactionLatency{i}, [this.trainConfig.transactionCount{i} this.trainConfig.lockWaitTime(:,i)]);
                    if isempty(predictionsLw)
                        predictionsLw = barzanLinInvoke(modelLw, [this.testConfig.totalTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    else
                        predictionsLw = predictionsLw + barzanLinInvoke(modelLw, [this.testConfig.totalTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    end
                end
                predictionsLw = predictionsLw ./ size(this.trainConfig.mv.numOfTransType, 2);

                ok=[this.testConfig.totalTransactionCount this.testConfig.totalTransactionLatency];
                tempActual = [this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) this.testConfig.totalTransactionLatency];
                tempPred = [this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);

                Xdata = {tempActual(:,1)};
                Ydata = {tempActual(:,2:end)};
                Xdata{end+1} = tempPred(:,1);
                Ydata{end+1} = tempPred(:,2:end);
                title = 'Linear model (counts + waiting time): Latency';
                legends = {};
                for i = 2:size(tempActual, 2)
                    legends{end+1} = ['Actual Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction'];
                end
                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                    errorHeader{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                    meanAbsError{i-1} = mae(tempPred(:,i), tempActual(:,i));
                    meanRelError{i-1} = mre(tempPred(:,i), tempActual(:,i));
                end

                Xlabel = horzcat('Transaction Counts of Type ', num2str(this.trainConfig.transactionType(this.whichTransactionToPlot)));
                Ylabel = 'Latency (seconds)';
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

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    modelLw = barzanLinSolve(this.trainConfig.transactionLatency{i}, [this.trainConfig.transactionCount{i} this.trainConfig.lockWaitTime(:,i)]);
                    if isempty(predictionsLw)
                        predictionsLw = barzanLinInvoke(modelLw, [testTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    else
                        predictionsLw = predictionsLw + barzanLinInvoke(modelLw, [testTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    end
                end
                predictionsLw = predictionsLw ./ size(this.trainConfig.mv.numOfTransType, 2);

                %modelLw = barzanLinSolve(this.trainConfig.transactionLatency, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                %predictionsLw  = barzanLinInvoke(modelLw, [testTransactionCount this.trainConfig.lockWaitTime]); % use lockWaitTime from trainConfig

                ok=[testTransactionCount this.trainConfig.totalTransactionLatency];
                % tempActual = [this.testConfig.transactionCount(:,1) this.testConfig.transactionLatency];
                tempPred = [testTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                % tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);

                Xdata = {tempPred(:,1)};
                Ydata = {tempPred(:,2:end)};

                title = 'Linear model (counts + waiting time): Latency';
                legends = {};

                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                end

                Xlabel = ['Transaction Counts of Type ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];
                Ylabel = 'Latency (seconds)';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsWaitTimeToLatency99(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsLw = [];

            title = 'Linear model (counts + waiting time): Latency (99% Quantile)';

            if this.testMode == PredictionCenter.TEST_MODE_DATASET

                %test_latencies = this.testConfig.transactionLatencyPercentile.latenciesPCtile(:,[this.testConfig.transactionType+1],7);
                %test_latencies(isnan(test_latencies)) = 0;

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],7);
                    train_latencies(isnan(train_latencies)) = 0;

                    modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount{i} this.trainConfig.lockWaitTime(:,i)]);
                    if isempty(predictionsLw)
                        predictionsLw = barzanLinInvoke(modelLw, [this.testConfig.totalTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    else
                        predictionsLw = predictionsLw + barzanLinInvoke(modelLw, [this.testConfig.totalTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    end
                end
                predictionsLw = predictionsLw ./ size(this.trainConfig.mv.numOfTransType, 2);

                %modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                %predictionsLw  = barzanLinInvoke(modelLw, [this.testConfig.transactionCount this.testConfig.lockWaitTime]);

                test_latencies = [];
                for i=1:size(this.testConfig.mv.numOfTransType, 2)
                    if isempty(test_latencies)
                        test_latencies = this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],7);
                    else
                        test_latencies = test_latencies + this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],7);
                    end
                end
                test_latencies(isnan(test_latencies)) = 0;

                ok=[this.testConfig.totalTransactionCount test_latencies];
                tempActual = [this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) test_latencies];
                tempPred = [this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);

                Xdata = {tempActual(:,1)};
                Ydata = {tempActual(:,2:end)};
                Xdata{end+1} = tempPred(:,1);
                Ydata{end+1} = tempPred(:,2:end);
                legends = {};
                for i = 2:size(tempActual, 2)
                    legends{end+1} = ['Actual Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction'];
                end
                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                    errorHeader{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                    meanAbsError{i-1} = mae(tempPred(:,i), tempActual(:,i));
                    meanRelError{i-1} = mre(tempPred(:,i), tempActual(:,i));
                end

                Xlabel = horzcat('Transaction Counts of Type ', num2str(this.trainConfig.transactionType(this.whichTransactionToPlot)));
                Ylabel = 'Latency (seconds)';
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

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],7);
                    train_latencies(isnan(train_latencies)) = 0;

                    modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount{i} this.trainConfig.lockWaitTime(:,i)]);
                    if isempty(predictionsLw)
                        predictionsLw = barzanLinInvoke(modelLw, [testTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    else
                        predictionsLw = predictionsLw + barzanLinInvoke(modelLw, [testTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    end
                end
                predictionsLw = predictionsLw ./ size(this.trainConfig.mv.numOfTransType, 2);

                %modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                %predictionsLw  = barzanLinInvoke(modelLw, [testTransactionCount this.trainConfig.lockWaitTime]); % use lockWaitTime from trainConfig

                ok=[testTransactionCount train_latencies];
                % tempActual = [this.testConfig.transactionCount(:,1) this.testConfig.transactionLatency];
                tempPred = [testTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                % tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);

                Xdata = {tempPred(:,1)};
                Ydata = {tempPred(:,2:end)};

                legends = {};

                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                end

                Xlabel = ['Transaction Counts of Type ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];
                Ylabel = 'Latency (seconds)';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = transactionCountsWaitTimeToLatencyMedian(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsLw = [];

            %train_latencies = this.trainConfig.transactionLatencyPercentile.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
            %train_latencies(isnan(train_latencies)) = 0;

            title = 'Linear model (counts + waiting time): Latency (Median)';

            if this.testMode == PredictionCenter.TEST_MODE_DATASET

                %test_latencies = this.testConfig.transactionLatencyPercentile.latenciesPCtile(:,[this.testConfig.transactionType+1],3);
                %test_latencies(isnan(test_latencies)) = 0;

                %modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                %predictionsLw  = barzanLinInvoke(modelLw, [this.testConfig.transactionCount this.testConfig.lockWaitTime]);

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
                    train_latencies(isnan(train_latencies)) = 0;

                    modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount{i} this.trainConfig.lockWaitTime(:,i)]);
                    if isempty(predictionsLw)
                        predictionsLw = barzanLinInvoke(modelLw, [this.testConfig.totalTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    else
                        predictionsLw = predictionsLw + barzanLinInvoke(modelLw, [this.testConfig.totalTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    end
                end
                predictionsLw = predictionsLw ./ size(this.trainConfig.mv.numOfTransType, 2);

                test_latencies = [];
                for i=1:size(this.testConfig.mv.numOfTransType, 2)
                    if isempty(test_latencies)
                        test_latencies = this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],3);
                    else
                        test_latencies = test_latencies + this.testConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.testConfig.transactionType+1],3);
                    end
                end
                test_latencies(isnan(test_latencies)) = 0;

                ok=[this.testConfig.totalTransactionCount test_latencies];
                tempActual = [this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) test_latencies];
                tempPred = [this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);

                Xdata = {tempActual(:,1)};
                Ydata = {tempActual(:,2:end)};
                Xdata{end+1} = tempPred(:,1);
                Ydata{end+1} = tempPred(:,2:end);
                legends = {};
                for i = 2:size(tempActual, 2)
                    legends{end+1} = ['Actual Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction'];
                end
                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                    errorHeader{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                    meanAbsError{i-1} = mae(tempPred(:,i), tempActual(:,i));
                    meanRelError{i-1} = mre(tempPred(:,i), tempActual(:,i));
                end

                Xlabel = horzcat('Transaction Counts of Type ', num2str(this.trainConfig.transactionType(this.whichTransactionToPlot)));
                Ylabel = 'Latency (seconds)';
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

                for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                    train_latencies = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
                    train_latencies(isnan(train_latencies)) = 0;

                    modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount{i} this.trainConfig.lockWaitTime(:,i)]);
                    if isempty(predictionsLw)
                        predictionsLw = barzanLinInvoke(modelLw, [testTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    else
                        predictionsLw = predictionsLw + barzanLinInvoke(modelLw, [testTransactionCount.*this.trainConfig.TPSRatio{i} mean(this.testConfig.lockWaitTime,2)]);
                    end
                end
                predictionsLw = predictionsLw ./ size(this.trainConfig.mv.numOfTransType, 2);

                %modelLw = barzanLinSolve(train_latencies, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
                %predictionsLw  = barzanLinInvoke(modelLw, [testTransactionCount this.trainConfig.lockWaitTime]); % use lockWaitTime from trainConfig

                ok=[testTransactionCount train_latencies];
                % tempActual = [this.testConfig.transactionCount(:,1) this.testConfig.transactionLatency];
                tempPred = [testTransactionCount(:,this.whichTransactionToPlot) predictionsLw];
                % tempActual = sortrows(tempActual, 1);
                tempPred = sortrows(tempPred, 1);

                Xdata = {tempPred(:,1)};
                Ydata = {tempPred(:,2:end)};

                legends = {};

                for i = 2:size(tempPred, 2)
                    legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i-1)) ' Transaction '];
                end

                Xlabel = ['Transaction Counts of Type ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];
                Ylabel = 'Latency (seconds)';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = blownTransactionCountsToCpu(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                range=1:1:size(this.trainConfig.totalTransactionCount, 2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount.*this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount(:, comb1).*this.trainConfig.totalTransactionCount(:, comb2)];
                blownTestC = [this.testConfig.totalTransactionCount this.testConfig.totalTransactionCount.*this.testConfig.totalTransactionCount this.testConfig.totalTransactionCount(:, comb1).*this.testConfig.totalTransactionCount(:, comb2)];

                blownModelP = barzanLinSolve(mean(this.trainConfig.averageCpuUsage,2), blownTrainC);
                blownPredictionsP = barzanLinInvoke(blownModelP, blownTestC);

                Xdata = {[1:size(this.testConfig.averageCpuUsage, 1)]'};
                Ydata = {[mean(this.testConfig.averageCpuUsage,2) blownPredictionsP]};
                legends = {'Actual CPU usage', 'Predicted CPU usage'};
                title = 'Quadratic Model: Average CPU';
                meanAbsError{1} = mae(blownPredictionsP, this.testConfig.averageCpuUsage);
                meanRelError{1} = mre(blownPredictionsP, this.testConfig.averageCpuUsage);
                errorHeader(1) = legends(2);
                Xlabel = 'Time';
                Ylabel = 'Average CPU (%)';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                range=1:1:size(this.trainConfig.totalTransactionCount, 2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount.*this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount(:, comb1).*this.trainConfig.totalTransactionCount(:, comb2)];
                blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount(:, comb1).*testTransactionCount(:, comb2)];

                blownModelP = barzanLinSolve(mean(this.trainConfig.averageCpuUsage,2), blownTrainC);
                blownPredictionsP = barzanLinInvoke(blownModelP, blownTestC);

                Xdata = {[1:size(blownPredictionsP, 1)]'};
                Ydata = {[blownPredictionsP]};
                legends = {'Predicted CPU usage'};
                title = 'Quadratic Model: Average CPU';
                Xlabel = 'Time';
                Ylabel = 'Average CPU (%)';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = blownTransactionCountsToIO(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                range=1:1:size(this.trainConfig.totalTransactionCount,2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount.*this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount(:, comb1).*this.trainConfig.totalTransactionCount(:, comb2)];
                blownTestC = [this.testConfig.totalTransactionCount this.testConfig.totalTransactionCount.*this.testConfig.totalTransactionCount this.testConfig.totalTransactionCount(:, comb1).*this.testConfig.totalTransactionCount(:, comb2)];

                blownModelIO = barzanLinSolve(sum(this.trainConfig.diskWrite,2), blownTrainC);
                blownPredictionsIO = barzanLinInvoke(blownModelIO, blownTestC);

                Xdata = {[1:size(this.testConfig.diskWrite, 1)]'};
                Ydata = {[sum(this.testConfig.diskWrite,2) blownPredictionsIO] ./ 1024 ./ 1024};

                meanAbsError{1} = mae(blownPredictionsIO, sum(this.testConfig.diskWrite,2)) ./ 1024 ./ 1024;
                meanRelError{1} = mre(blownPredictionsIO, sum(this.testConfig.diskWrite,2));

                title = 'Quadratic Model: Average Physical Writes';
                legends = {'Actual Writes', 'Predicted Writes'};
				errorHeader(1) = legends(2);
                Ylabel = 'Written Data (MB)';
                Xlabel = '';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                range=1:1:size(this.trainConfig.totalTransactionCount,2);
                combs = combnk(range, 2);
                comb1 = combs(:,1);
                comb2 = combs(:,2);

                blownTrainC = [this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount.*this.trainConfig.totalTransactionCount this.trainConfig.totalTransactionCount(:, comb1).*this.trainConfig.totalTransactionCount(:, comb2)];
                blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount(:, comb1).*testTransactionCount(:, comb2)];

                blownModelIO = barzanLinSolve(sum(this.trainConfig.diskWrite,2), blownTrainC);
                blownPredictionsIO = barzanLinInvoke(blownModelIO, blownTestC);

                Xdata = {[1:size(blownPredictionsIO, 1)]'};
                Ydata = {[blownPredictionsIO] ./ 1024 ./ 1024};

                title = 'Quadratic Model: Average Physical Writes';
                legends = {'Predicted Writes'};
                Ylabel = 'Written Data (MB)';
                Xlabel = '';
            end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = linearPrediction(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                trainY = this.trainConfig.logWriteMB;
                testY = this.testConfig.logWriteMB;

                model = barzanLinSolve(trainY, this.trainConfig.totalTransactionCount);
                pred = barzanLinInvoke(model, this.testConfig.totalTransactionCount);

                % xValuesTest = this.testConfig.transactionCount(:,this.whichTransactionToPlot)./ this.testConfig.TPS;
                % xValuesTrain = this.trainConfig.transactionCount(:,this.whichTransactionToPlot)./ this.trainConfig.TPS;

                xValuesTest = this.testConfig.totalTransactionCount(:,this.whichTransactionToPlot);
                xValuesTrain = this.trainConfig.totalTransactionCount(:,this.whichTransactionToPlot);

                temp = [xValuesTest testY pred];
                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};

                meanAbsError{1} = mae(pred, testY);
                meanRelError{1} = mre(pred, testY);

                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = trainY;

                Ylabel = 'Log Writes (MB)';
                Xlabel = ['Counts of Transaction Type ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];
                legends = {'Actual', 'Predicted', 'Training Data'};
				errorHeader(1) = legends(2);
                title = 'Linear Prediction';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
                testTPS = this.testSampleTPS;
                testTransactionCount = this.testSampleTransactionCount;

                trainY = this.trainConfig.logWriteMB;

                model = barzanLinSolve(trainY, this.trainConfig.totalTransactionCount);
                pred = barzanLinInvoke(model, testTransactionCount);

                xValuesTest = testTransactionCount(:,this.whichTransactionToPlot);
                xValuesTrain = this.trainConfig.totalTransactionCount(:,this.whichTransactionToPlot);

                temp = [xValuesTest pred];
                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2)]};

                Xdata{end+1} = xValuesTrain;
                Ydata{end+1} = trainY;

                Ylabel = 'Log Writes (MB)';
                Xlabel = ['Counts of Transaction Type ' num2str(this.trainConfig.transactionType(this.whichTransactionToPlot))];
                legends = {'Predicted', 'Training Data'};
                title = 'Linear Prediction';
            end

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = physicalReadPrediction(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};
            if this.testMode == PredictionCenter.TEST_MODE_DATASET
                cacheMissRate = sum(this.testConfig.logicalReads,2) ./ sum(this.testConfig.physicalReads,2);
                normalization = mean(this.testConfig.physicalReadsMB) ./ mean(cacheMissRate);

                xValuesTest = this.testConfig.TPS;

                temp = [xValuesTest this.testConfig.physicalReadsMB cacheMissRate*normalization];
                temp = sortrows(temp, 1);

                Xdata = {temp(:,1)};
                Ydata = {[temp(:,2) temp(:,3)]};

                %meanAbsError{1} = mae(cacheMissRate*normalization, this.testConfig.physicalReadsMB);
                %meanRelError{1} = mre(cacheMissRate*normalization, this.testConfig.physicalReadsMB);

                title = 'Physical read volume and cache miss rate';
                legends = {'Physical Read Volume', 'Cache Miss Rate'};
                Ylabel = 'Data Read (MB per sec)';
                Xlabel = 'TPS';
            elseif this.testMode == PredictionCenter.TEST_MODE_MIXTURE_TPS
				% Original DBSeer does not support this case currently...
				Xdata = {};
				Ydata = {};
                title = 'Physical read volume and cache miss rate';
                legends = {'Physical Read Volume', 'Cache Miss Rate'};
                Ylabel = 'Data Read (MB per sec)';
                Xlabel = 'TPS';
			end
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = blameAnalysisCPU(this)

            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            idx=1:size(this.trainConfig.averageCpuUsage, 1);
            numTx = size(this.trainConfig.totalTransactionCount, 2);
            myModelP = {};
            myCpuPred = {};
            SSE = [];
            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            for i=1:numTx
                myModelP{i} = barzanLinSolve(mean(this.trainConfig.averageCpuUsage(idx,:),2), this.trainConfig.totalTransactionCount(idx,i));
                myCpuPred{i} = barzanLinInvoke(myModelP{i}, testTransactionCount(:,i));
                myCpuFitting{i} = barzanLinInvoke(myModelP{i}, this.trainConfig.totalTransactionCount(:,i));
                SSE(i) = sqrt(sum((mean(this.trainConfig.averageCpuUsage,2) - myCpuFitting{i}).^2) ./ size(myCpuFitting{i},1));
            end

            % transaction type with the least sum of squared errors will be likely to be most responsible for its high cpu usage.
            [minError minLoc] = min(SSE);
            extra = {[minLoc]};

            xValuesTest = testTPS;
            xValuesTrain = this.trainConfig.TPS;

            Xdata = {xValuesTest};
            Ydata = {};
            legends = {};
            temp = [];

            for i=1:numTx
                temp(:,i) = myCpuPred{i};
                legends{end+1} = horzcat('Type ', num2str(i), ' Transactions');
            end
            Ydata = {temp};

            % temp = [xValuesTest predictionsP myCpuPred];

            Xlabel = 'TPS';
            Ylabel = 'Average CPU (%)';
            title = 'Blame Analysis: CPU';
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = blameAnalysisIO(this)

            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            numTx = size(this.trainConfig.totalTransactionCount, 2);

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            modelRead = {};
            predictionRead = {};
            modelWrite = {};
            predictionWrite = {};
            fitRead = {};
            fitWrite = {};
            SSE_read = [];
            SSE_write = [];

            for i=1:numTx
                modelRead{i} = barzanLinSolve(sum(this.trainConfig.mvUngrouped.dbmsPhysicalReadsMB,2), this.trainConfig.totalTransactionCount(:,i));
                predictionsRead{i} = barzanLinInvoke(modelRead{i}, testTransactionCount(:,i));
                fitRead{i} = barzanLinInvoke(modelRead{i}, this.trainConfig.totalTransactionCount(:,i));
                SSE_read(i) = sqrt(sum((sum(this.trainConfig.mvUngrouped.dbmsPhysicalReadsMB,2) - fitRead{i}).^2) ./ size(fitRead{i},1));

                modelWrite{i} = barzanLinSolve(sum(this.trainConfig.mvUngrouped.dbmsTotalWritesMB,2), this.trainConfig.totalTransactionCount(:,i));
                predictionsWrite{i} = barzanLinInvoke(modelWrite{i}, testTransactionCount(:,i));
                fitWrite{i} = barzanLinInvoke(modelWrite{i}, this.trainConfig.totalTransactionCount(:,i));
                SSE_write(i) = sqrt(sum((sum(this.trainConfig.mvUngrouped.dbmsTotalWritesMB,2) - fitWrite{i}).^2) ./ size(fitWrite{i},1));
            end

            % transaction type with the least sum of squared errors will be likely to be most responsible for its high IO usage.
            [minReadError minReadLoc] = min(SSE_read);
            [minWrite minWriteLoc] = min(SSE_write);
            extra = {[minReadLoc minWriteLoc]};

            Xdata = {testTPS};
            Ydata = {};
            legends = {};
            temp = [];

            for i=1:numTx
                temp(:,i) = predictionsRead{i};
                legends{end+1} = horzcat('Predicted Disk Read for Type ', num2str(i), ' Transactions');
            end
            for i=1:numTx
                temp(:,numTx+i) = predictionsWrite{i};
                legends{end+1} = horzcat('Predicted Disk Write for Type ', num2str(i), ' Transactions');
            end
            Ydata = {temp};

            title = 'Blame Analysis: Disk I/O';
            Ylabel = 'Data read/written (MB)';
            Xlabel = 'TPS';

        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = blameAnalysisLock(this)

            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            lock_conf = this.lockConf;

            numTx = size(this.trainConfig.totalTransactionCount, 2);

            myPredictedLock = [];
            legends = {};
            SSE = [];

            allPreds = useLockModel(lock_conf, this.trainConfig.totalTransactionCount, 'TPCC');
            totalWaits = allPreds.totalWaits;
            temp = horzcat(totalWaits, this.trainConfig.totalTransactionCount);

            filtered = temp(find(sum(isnan(temp),2)==0),:);

            modelLock = {};
            predictionLock = {};

            temp = [];
            for i=1:numTx
                modelLock{i} = barzanLinSolve(filtered(:,1), filtered(:,i+1));
                predictionLock{i} = barzanLinInvoke(modelLock{i}, testTransactionCount(:,i));
                myLockFitting{i} = barzanLinInvoke(modelLock{i}, filtered(:,i+1));
                SSE(i) = sqrt(sum((filtered(:,1) - myLockFitting{i}).^2) ./ size(myLockFitting{i},1));
                temp(:,i) = predictionLock{i};
                legends{end+1} = horzcat('Type ', num2str(i), ' Transactions');
            end

            % transaction type with the least sum of squared errors will be likely to be most responsible for lock contention.
            [minError minLoc] = min(SSE);
            extra = {[minLoc]};

            Xdata = {testTPS};
            Ydata = {temp};

            Xlabel = 'TPS';
            Ylabel = 'Total time spent acquiring row locks (seconds)';
            title = 'Blame Analysis: Lock';
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = whatIfAnalysisLatency(this)
            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictionsL = [];
            latencyPrediction = [];
            latency99Prediction = [];
            latencyMedianPrediction = [];

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            minTransactionCount = this.testMinTPS .* this.testMixture;
            avgTransactionCount = this.testWorkloadRatio .* mean(sum(this.trainConfig.totalTransactionCount,2),1) .* this.testMixture;
            maxTransactionCount = this.testMaxTPS .* this.testMixture;

            tpsRange = this.testMaxTPS - this.testMinTPS;
            tpsStep = tpsRange / 100;
            tpsVector = [this.testMinTPS+tpsStep:tpsStep:this.testMaxTPS]';
            otherTransactionCount = tpsVector * this.testMixture;
            whatIfTransactionCount = vertcat(minTransactionCount, avgTransactionCount, maxTransactionCount, otherTransactionCount);


            for i=1:size(this.trainConfig.mv.numOfTransType, 2)

                latency99 = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],7);
                latency99(isnan(latency99)) = 0;
                latencyMedian = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
                latencyMedian(isnan(latencyMedian)) = 0;

                modelL = barzanLinSolve(this.trainConfig.transactionLatency{i}, this.trainConfig.transactionCount{i});
                modelL99 = barzanLinSolve(latency99, this.trainConfig.transactionCount{i});
                modelLMedian = barzanLinSolve(latencyMedian, this.trainConfig.transactionCount{i});

                % predictionsL  = barzanLinInvoke(modelL, testTransactionCount);
                if isempty(predictionsL)
                    predictionsL = barzanLinInvoke(modelL, whatIfTransactionCount);
                else
                    predictionsL = predictionsL + barzanLinInvoke(modelL, whatIfTransactionCount);
                end

                % predictionsL = whatifPrediction;

                if isempty(latencyPrediction)
                    latencyPrediction = barzanLinInvoke(modelL, avgTransactionCount);
                else
                    latencyPrediction = latencyPrediction + barzanLinInvoke(modelL, avgTransactionCount);
                end
                if isempty(latency99Prediction)
                    latency99Prediction = barzanLinInvoke(modelL99, avgTransactionCount);
                else
                    latency99Prediction = latency99Prediction +  barzanLinInvoke(modelL99, avgTransactionCount);
                end
                if isempty(latencyMedianPrediction)
                    latencyMedianPrediction = barzanLinInvoke(modelLMedian, avgTransactionCount);
                else
                    latencyMedianPrediction = latencyMedianPrediction + barzanLinInvoke(modelLMedian, avgTransactionCount);
                end

                predictionsL(:,find(this.testMixture==0)) = 0;
                latencyPrediction(find(avgTransactionCount==0)) = 0;
                latency99Prediction(find(avgTransactionCount==0)) = 0;
                latencyMedianPrediction(find(avgTransactionCount==0)) = 0;
            end

            predictionsL = predictionsL ./ size(this.trainConfig.mv.numOfTransType, 2);
            latencyPrediction = latencyPrediction ./ size(this.trainConfig.mv.numOfTransType, 2);
            latency99Prediction = latency99Prediction ./ size(this.trainConfig.mv.numOfTransType, 2);
            latencyMedianPrediction = latencyMedianPrediction ./ size(this.trainConfig.mv.numOfTransType, 2);

            whatIfPrediction = vertcat(latencyPrediction, latencyMedianPrediction, latency99Prediction);
            overallLatency = vertcat(sum(latencyPrediction.*this.testMixture), sum(latencyMedianPrediction.*this.testMixture), ...
             sum(latency99Prediction.*this.testMixture));
            % whatifPrediction(find(whatIfTransactionCount==0)) = 0;

            transactionCounts = [mean(sum(this.trainConfig.totalTransactionCount,2),1) sum(avgTransactionCount)];

            % Xdata = {[testTPS]};
            Xdata = {[sum(whatIfTransactionCount,2)]};
            Ydata = {[predictionsL]};
            % Ydata = {[whatifPrediction]};
            % avg, median, 99% quantile transaction latency
            extra = {[whatIfPrediction(1,:)] [whatIfPrediction(2,:)] [whatIfPrediction(3,:)] [overallLatency] [transactionCounts]};
            title = 'What-if Analysis: Average Latency';

            legends = {};

            for i = 1:size(predictionsL, 2)
                %legends{end+1} = ['predicted latency of transaction ' num2str(i)];
                legends{end+1} = ['Predicted Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
            end
            Ylabel = 'Latency (seconds)';
            Xlabel = 'TPS';
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = whatIfAnalysisCPU(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            minTransactionCount = this.testMinTPS .* this.testMixture;
            % avgTransactionCount = this.testWorkloadRatio .* mean(this.trainConfig.transactionCount,1);
            avgTransactionCount = this.testWorkloadRatio .* mean(sum(this.trainConfig.totalTransactionCount,2),1) .* this.testMixture;
            maxTransactionCount = this.testMaxTPS .* this.testMixture;
            whatIfTransactionCount = vertcat(minTransactionCount, avgTransactionCount, maxTransactionCount);

			idx=1:size(this.trainConfig.averageCpuUsage, 1);
            myModelP = barzanLinSolve(mean(this.trainConfig.averageCpuUsage(idx,:),2), this.trainConfig.totalTransactionCount(idx,:));
            myCpuPred = barzanLinInvoke(myModelP, testTransactionCount);
            whatifPrediction = barzanLinInvoke(myModelP, avgTransactionCount);

            transactionCounts = [mean(sum(this.trainConfig.totalTransactionCount,2),1) sum(avgTransactionCount)];

            extra = {[whatifPrediction(1,:)] [transactionCounts]};

            xValuesTest = testTPS;
            xValuesTrain = this.trainConfig.TPS;
            Xlabel = 'TPS';

            modelP = barzanLinSolve(mean(this.trainConfig.averageCpuUsage,2), this.trainConfig.totalTransactionCount);
            predictionsP  = barzanLinInvoke(modelP, testTransactionCount);

            temp = [xValuesTest predictionsP myCpuPred];

            Xdata = {temp(:,1)};
            Ydata = {[temp(:,2) temp(:,3)]};
            Xdata{end+1} = xValuesTrain;
            Ydata{end+1} = this.trainConfig.averageCpuUsage;

            legends = {'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
            Ylabel = 'Average CPU (%)';
            title = 'What-if Analysis: CPU';
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = whatIfAnalysisIO(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            minTransactionCount = this.testMinTPS .* this.testMixture;
            % avgTransactionCount = this.testWorkloadRatio .* mean(this.trainConfig.transactionCount,1);
            avgTransactionCount = this.testWorkloadRatio .* mean(sum(this.trainConfig.totalTransactionCount,2),1) .* this.testMixture;
            maxTransactionCount = this.testMaxTPS .* this.testMixture;
            whatIfTransactionCount = vertcat(minTransactionCount, avgTransactionCount, maxTransactionCount);

            transactionCounts = [mean(sum(this.trainConfig.totalTransactionCount,2),1) sum(avgTransactionCount)];

            readData = [sum(this.trainConfig.mvUngrouped.dbmsPhysicalReadsMB,2), this.trainConfig.totalTransactionCount sum(this.trainConfig.totalTransactionCount, 2)];
            readData = readData(find(readData(:,end)~=0),[1:end-1]);

            writeData = [sum(this.trainConfig.mvUngrouped.dbmsTotalWritesMB,2), this.trainConfig.totalTransactionCount sum(this.trainConfig.totalTransactionCount, 2)];
            writeData = writeData(find(writeData(:,end)~=0),[1:end-1]);

            % modelRead = barzanLinSolve(this.trainConfig.mvUngrouped.dbmsPhysicalReadsMB, this.trainConfig.transactionCount);
            modelRead = barzanLinSolve(readData(:,1), readData(:,2:end));
            predictionsRead = barzanLinInvoke(modelRead, testTransactionCount);
            % modelWrite = barzanLinSolve(this.trainConfig.mvUngrouped.dbmsTotalWritesMB, this.trainConfig.transactionCount);
            modelWrite = barzanLinSolve(writeData(:,1), writeData(:,2:end));
            predictionsWrite = barzanLinInvoke(modelWrite, testTransactionCount);

            readPrediction = barzanLinInvoke(modelRead, avgTransactionCount);
            writePrediction = barzanLinInvoke(modelWrite, avgTransactionCount);

            extra = {[readPrediction] [writePrediction] [transactionCounts]};

            Xdata = {testTPS};
            Ydata = {[predictionsRead predictionsWrite]};
            title = 'What-if Analysis: Disk I/O';
            legends = {'Predicted Disk Reads' 'Predicted Disk Writes'};
            Ylabel = 'Data read/written (MB)';
            Xlabel = 'TPS';
        end

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = whatIfAnalysisFlushRate(this)
            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            minTransactionCount = this.testMinTPS .* this.testMixture;
            % avgTransactionCount = this.testWorkloadRatio .* mean(this.trainConfig.transactionCount,1);
            avgTransactionCount = this.testWorkloadRatio .* mean(sum(this.trainConfig.totalTransactionCount,2),1) .* this.testMixture;
            maxTransactionCount = this.testMaxTPS .* this.testMixture;
            whatIfTransactionCount = vertcat(minTransactionCount, avgTransactionCount, maxTransactionCount);

            treeModel = barzanRegressTreeLearn(sum(this.trainConfig.pagesFlushed,2), this.trainConfig.totalTransactionCount);
            treePred = barzanRegressTreeInvoke(treeModel, testTransactionCount);

            naiveLinModel = barzanLinSolve(sum(this.trainConfig.pagesFlushed,2), sum(this.trainConfig.TPS,2));
            linPred = barzanLinInvoke(naiveLinModel, testTPS);

            betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.totalTransactionCount);
            classLinPred = barzanLinInvoke(betterLinModel, testTransactionCount);

            emp = zeros(size(this.trainConfig.totalTransactionCount,1), 0);

            % check the availability of 'fitnet' function (Neural Network Toolbox).
            if exist('fitnet') == 5
                nnModel = barzanNeuralNetLearn(sum(this.trainConfig.pagesFlushed,2), this.trainConfig.totalTransactionCount);
                nnPred = barzanNeuralNetInvoke(nnModel, testTransactionCount);
            end

            config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            myPred = cfFlushRateApprox(config, testTransactionCount);
            whatifPrediction = cfFlushRateApprox(config, avgTransactionCount);
            transactionCounts = [mean(sum(this.trainConfig.totalTransactionCount,2),1) sum(avgTransactionCount)];

            extra = {[whatifPrediction] [transactionCounts]};

            if exist('fitnet') == 5
                temp = [linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
            else
                temp = [linPred classLinPred myPred treePred]; % kccaPred is not included for now.
            end
            temp = [testTPS temp];

            temp = sortrows(temp,1);

            Xdata = {temp(:,1)};
            if exist('fitnet') == 5
                Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6)]};
                legends = {'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};
            else
                Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5)]};
                legends = {'LR', 'LR+classification', 'Our model', 'Tree regression'};
            end

            title = 'What-if Analysis: Disk Flush Rate';
            Ylabel = 'Average # of page flush per seconds';
            Xlabel = 'TPS';

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = bottleneckAnalysisMaxThroughput(this)

            meanAbsError = {};
            meanRelError = {};
			errorHeader = {};
            extra = {};

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            range = (1:10000)';
            config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            maxFlushRate = 6000;

            % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
            % cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            % myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);
            modelP = barzanLinSolve(mean(this.trainConfig.averageCpuUsage,2), this.trainConfig.totalTransactionCount);

            % [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
            [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(sum(this.trainConfig.TPSUngrouped,2));

            actualThr = [];

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
            cpuTModel = barzanLinSolve(mean(this.trainConfig.averageCpuUsage,2), sum(this.trainConfig.TPS,2));
            cpuT = barzanLinInvoke(cpuTModel, range);
            cpuTLThroughput = find(cpuT>88 & cpuT<90, 1, 'last');
            cpuTUThroughput = find(cpuT>98 & cpuT<100, 1, 'last');

            myModelP = barzanLinSolve(sum(this.trainConfig.averageCpuUsage(idx,:),2), this.trainConfig.totalTransactionCount(idx,:));
            myCpuC = barzanLinInvoke(myModelP, range*this.testMixture);
            myCpuCLThroughput = find(myCpuC>88 & myCpuC<90, 1, 'last');
            myCpuCUThroughput = find(myCpuC>98 & myCpuC<100, 1, 'last');

            %myCpuCLThroughput = find(myCpuC>44 & myCpuC<45, 1, 'last');
            %myCpuCUThroughput = find(myCpuC>59 & myCpuC<50, 1, 'last');

            %Our IO-based throughput
            % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
            cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);

            %Lock-based throughput
            % getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
            getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
            concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:5000)'*this.testMixture, 160, getConcurrencyLebel_conf);

            %Linear IO-based throughput
            modelFlushRate = barzanLinSolve(sum(this.trainConfig.pagesFlushed,2), this.trainConfig.totalTransactionCount);
            linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testMixture);
            linFlushRateThroughput = find(linFlushRate<maxFlushRate, 1, 'last');
            if isempty(linFlushRateThroughput)
                linFlushRateThroughput=0;
            end

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

            finalMaxThroughput = min([cpuCLThroughput cpuCUThroughput cpuTLThroughput cpuTUThroughput myFlushRateThroughput linFlushRateThroughput concurrencyThroughput]);

            extra = {[finalMaxThroughput]};

            title = 'Bottleneck Analysis: Max Throughput';
            Ylabel = 'TPS';
            Xlabel = 'Time';

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = bottleneckAnalysisResource(this)

            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;

            range = (1:15000)';
            config = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            maxFlushRate = 6000;

            % cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
            cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);
            modelP = barzanLinSolve(mean(this.trainConfig.averageCpuUsage,2), this.trainConfig.totalTransactionCount);

            % [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
            [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(sum(this.trainConfig.TPSUngrouped,2));

            actualThr = [];

            if exist('trainMaxThroughputIdx') && ~isempty(trainMaxThroughputIdx)
                idx=1:trainMaxThroughputIdx;
            else
                idx=1:size(this.trainConfig.averageCpuUsage,1);
            end

            %CPU-based throughput with classification
            cpuC = barzanLinInvoke(modelP, range*this.testMixture);
            cpuThroughput = find(cpuC>90 & cpuC<100, 1, 'last');

            if isempty(cpuThroughput)
                cpuThroughput = inf;
            end

            % Our IO-based throughput
            % cfFlushRateApprox_conf = struct('io_conf', this.ioConf, 'workloadName', 'TPCC');
            % myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);
            myFlushRateThroughput = 0;

            % Lock-based throughput
            % getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
            % concurrencyThroughput = findClosestValue(@getConcurrencyLevel, (1:10000)'*this.testMixture, 160, getConcurrencyLebel_conf);
            getConcurrencyLebel_conf = struct('lock_conf', this.lockConf, 'workloadName', 'TPCC');
            concurrencyThroughput = findClosestValue(@getConcurrencyLevelNew, (1:10000)'*this.testMixture, 10e+200, this.trainConfig.datasetList{1}.datasets{1})

            Xdata = {};
            Ydata = {};

            if cpuThroughput==Inf
                cpuThroughput=5000;
            end
            if myFlushRateThroughput==Inf
                myFlushRateThroughput=5000;
            end
            if concurrencyThroughput==Inf
                concurrencyThroughput=5000;
            end

            Ydata{end+1} = cpuThroughput;
            Ydata{end+1} = myFlushRateThroughput;
            Ydata{end+1} = concurrencyThroughput;

            legends = {};
            legends{end+1} = 'CPU';
            legends{end+1} = 'I/O';
            legends{end+1} = 'Lock Contention';

            [minThroughput minIndex] = min([cpuThroughput myFlushRateThroughput concurrencyThroughput]);
            extra = {[minThroughput] [legends{minIndex}]};

            title = 'Bottleneck Analysis: Bottleneck Resource';
            Ylabel = 'Throughput';
            Xlabel = 'Resources';

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = throttlingAnalysis(this)

            meanAbsError = {};
            meanRelError = {};
            errorHeader = {};
            extra = {};

            predictedLatency = [];

            testTPS = this.testSampleTPS;
            testTransactionCount = this.testSampleTransactionCount;
            avgTransactionCount = this.testWorkloadRatio .* mean(sum(this.trainConfig.totalTransactionCount,2),1) .* this.testMixture;

            for i=1:size(this.trainConfig.mv.numOfTransType, 2)
                latencyAvg = this.trainConfig.transactionLatency{i};
                latency99 = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],7);
                latency99(isnan(latency99)) = 0;
                latencyMedian = this.trainConfig.transactionLatencyPercentile{i}.latenciesPCtile(:,[this.trainConfig.transactionType+1],3);
                latencyMedian(isnan(latencyMedian)) = 0;

                trainLatency = [];
                latencyType = '';

                if this.throttleLatencyType == 0
                    trainLatency = latencyAvg;
                    latencyType = 'Average';
                elseif this.throttleLatencyType == 1
                    trainLatency = latencyMedian;
                    latencyType = 'Median';
                elseif this.throttleLatencyType == 2
                    trainLatency = latency99;
                    latencyType = '99% Quantile';
                end

                range=(1:15000)';
                testTransactionCount = range * this.testMixture;

                modelL = barzanLinSolve(trainLatency, this.trainConfig.totalTransactionCount);
                if isempty(predictedLatency)
                    predictedLatency = barzanLinInvoke(modelL, testTransactionCount);
                else
                    predictedLatency = predictedLatency + barzanLinInvoke(modelL, testTransactionCount);
                end
            end
            predictedLatency = predictedLatency ./ size(this.trainConfig.mv.numOfTransType, 2);

            targetLatency = predictedLatency(:,this.throttleTargetTransactionIndex);

            maxTPS = find(targetLatency < this.throttleTargetLatency, 1, 'last');

            if isempty(maxTPS)
                maxTPS = 0;
            end

            if maxTPS == 0
                Xdata = {[(1:maxTPS+100)']};
                Ydata = {[predictedLatency(1:maxTPS+100,:)]};
            else
                Xdata = {[(1:maxTPS)']};
                Ydata = {[predictedLatency(1:maxTPS,:)]};
            end


            if this.throttleIndividualTransactions
                % perform linear programming to find optimal transaction counts.
                numTx = size(avgTransactionCount, 2);
                avgTotal = sum(avgTransactionCount, 2);
                f = this.throttlePenalty';
                A = -1 * ones(1, numTx);
                b = maxTPS - avgTotal;
                lb = zeros(numTx, 1);
                ub = avgTransactionCount';
                % 0 - no need to throttle, 1 - failed to find a solution, 2 - found a solution.
                if b > 0
                    extra = {[0] [this.throttleTargetTransactionIndex] [this.throttleTargetLatency]};
                else
                    if isOctave
                        pkg load optim;
                        sol = linprog(f,A,b,[],[],lb,ub);
                        exitflag = 1;
                    else
                        [sol, fval, exitflag, output, lambda] = linprog(f,A,b,[],[],lb,ub);
                    end
                    if exitflag ~= 1
                        extra = {[1]};
                    else
                        extra = {[2] [avgTransactionCount-sol'] [this.throttleTargetTransactionIndex] [this.throttleTargetLatency]};
                    end
                end
            else
                extra = {[maxTPS] [this.throttleTargetTransactionIndex] [this.throttleTargetLatency]};
            end

            title = 'Throttle Analysis';
            legends = {};

            for i = 1:size(predictedLatency, 2)
                %legends{end+1} = ['predicted latency of transaction ' num2str(i)];
                legends{end+1} = ['Predicted ' latencyType ' Latency of Type ' num2str(this.trainConfig.transactionType(i)) ' Transaction '];
            end
            Ylabel = 'Latency (seconds)';
            Xlabel = 'TPS';
        end % end function

    end % end methods

end % end classdef
