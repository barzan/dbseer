classdef PredictionCenter < handle

    properties
        taskDescription
        trainConfig
        testConfig
    end
    
    methods
        function [title legends Xdata Ydata Xlabel Ylabel] = performPrediction(this)
            if strcmp(this.taskDescription.taskName, 'FlushRatePredictionByTPS')
                [title legends Xdata Ydata Xlabel Ylabel] = this.flushRatePredictionByTPS;
            elseif strcmp(this.taskDescription.taskName, 'FlushRatePredictionByCounts')
                [title legends Xdata Ydata Xlabel Ylabel] = this.flushRatePredictionByCounts;
            elseif strcmp(this.taskDescription.taskName, 'MaxThroughputPrediction')
                [title legends Xdata Ydata Xlabel Ylabel] = this.maxThroughputPrediction;
            elseif strcmp(this.taskDescription.taskName, 'TransactionCountsToCpuByTPS')
                [title legends Xdata Ydata Xlabel Ylabel] = this.transactionCountsToCpuByTPS;
            elseif strcmp(this.taskDescription.taskName, 'TransactionCountsToCpuByCounts')
                [title legends Xdata Ydata Xlabel Ylabel] = this.transactionCountsToCpuByCounts;
            elseif strcmp(this.taskDescription.taskName, 'TransactionCountsToIO')
                [title legends Xdata Ydata Xlabel Ylabel] = this.transactionCountsToIO;
            elseif strcmp(this.taskDescription.taskName, 'TransactionCountsToLatency')
                [title legends Xdata Ydata Xlabel Ylabel] = this.transactionCountsToLatency;
            elseif strcmp(this.taskDescription.taskName, 'TransactionCountsWaitTimeToLatency')
                [title legends Xdata Ydata Xlabel Ylabel] = this.transactionCountsWaitTimeToLatency;
            elseif strcmp(this.taskDescription.taskName, 'BlownTransactionCountsToCpu')
                [title legends Xdata Ydata Xlabel Ylabel] = this.blownTransactionCountsToCpu;
            elseif strcmp(this.taskDescription.taskName, 'BlownTransactionCountsToIO')
                [title legends Xdata Ydata Xlabel Ylabel] = this.blownTransactionCountsToIO;
            elseif strcmp(this.taskDescription.taskName, 'LinearPrediction')
                [title legends Xdata Ydata Xlabel Ylabel] = this.linearPrediction;
            elseif strcmp(this.taskDescription.taskName, 'PhysicalReadPrediction')
                [title legends Xdata Ydata Xlabel Ylabel] = this.physicalReadPrediction;
            elseif strcmp(this.taskDescription.taskName, 'LockPrediction')
                [title legends Xdata Ydata Xlabel Ylabel] = this.lockPrediction;
            else
                error(strcat('Unsupported task name: ', this.taskDescription.taskName));
            end
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = flushRatePredictionByTPS(this)
            mv = this.testConfig.mv;

            treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
            treePred = barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount);
            
            naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
            linPred = barzanLinInvoke(naiveLinModel, this.testConfig.TPS);

            betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
            classLinPred = barzanLinInvoke(betterLinModel, this.testConfig.transactionCount);

            kccaGroupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', this.testConfig.transactionType, 'nClusters', 30, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
            emp = zeros(size(this.trainConfig.transactionCount,1), 0);
            %[emp1 emp2 kccaTrainC kccaTrainPagesFlushed] = applyGroupingPolicy(struct('groupParams', kccaGroupParams), emp, emp, this.trainConfig.transactionCount, this.trainConfig.pagesFlushed);

            %kccaModel = barzanKccaLearn(kccaTrainPagesFlushed, kccaTrainC);
            %kccaPred = barzanKccaInvoke(kccaModel, testC);

            nnModel = barzanNeuralNetLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
            nnPred = barzanNeuralNetInvoke(nnModel, this.testConfig.transactionCount);

            config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.taskDescription.workloadName);
            myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);

            temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
            temp = [this.testConfig.TPS temp];

            temp = sortrows(temp,1);

            Xdata = {temp(:,1)};
            Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6) temp(:,7)]};

            legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};

            title = horzcat('Flush rate prediction with # test points = ', num2str(size(this.testConfig.transactionCount),1));
            Ylabel = 'Average # of page flush per seconds';
            Xlabel = 'TPS';
        end

        function [title legends Xdata Ydata Xlabel Ylabel] = flushRatePredictionByCounts(this)
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

            config = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.taskDescription.workloadName);
            myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);

            temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred nnPred]; % kccaPred is not included for now.
            temp = [this.testConfig.transactionCount(:,this.taskDescription.whichTransactionToPlot)./this.testConfig.TPS temp];

            temp = sortrows(temp,1);

            Xdata = {temp(:,1)};
            Ydata = {[temp(:,2) temp(:,3) temp(:,4) temp(:,5) temp(:,6) temp(:,7)]};

            legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression', 'Neural Net'};

            title = horzcat('Flush rate prediction with # test points = ', num2str(size(this.testConfig.transactionCount),1));
            Ylabel = 'Average # of page flush per seconds';
            Xlabel = ['Ratio of transaction ' num2str(this.taskDescription.whichTransactionToPlot)];
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = maxThroughputPrediction(this)
            range = (1:15000)';
            maxFlushRate = 1000;
            
            cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.taskDescription.workloadName);
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
            cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.taskDescription.workloadName);
            myFlushRateThroughput = findClosestValue(@cfFlushRateApprox, (1:6000)'*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);
            
            %Lock-based throughput
            getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.taskDescription.workloadName);
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
            end
            if ~isempty(cpuCUThroughput)
                Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                Ydata{end+1} = repmat(cpuCUThroughput, num_row, num_col);
                legends{end+1} = 'Max Throughput on LR for CPU+classification';
            end
            if ~isempty(cpuTLThroughput)
                Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                Ydata{end+1} = repmat(cpuTLThroughput, num_row, num_col);
                legends{end+1} = 'Max Throughput on adjusted LR for CPU';
            end
            if ~isempty(cpuTUThroughput)
                Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                Ydata{end+1} = repmat(cpuTUThroughput, num_row, num_col);
                legends{end+1} = 'Max Throughput based on LR for CPU';
            end
            if ~isempty(myFlushRateThroughput)
                Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                Ydata{end+1} = repmat(myFlushRateThroughput, num_row, num_col);
                legends{end+1} = 'Max Throughput based on our flush rate model';
            end
            if ~isempty(linFlushRateThroughput)
                Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                Ydata{end+1} = repmat(linFlushRateThroughput, num_row, num_col);
                legends{end+1} = 'Max Throughput based on LR for flush rate'; 
            end
            if ~isempty(concurrencyThroughput)
                Xdata{end+1} = [1:size(this.testConfig.TPS, 1)]';
                Ydata{end+1} = repmat(concurrencyThroughput, num_row, num_col);
                legends{end+1} = 'Max Throughput based on our contention model';
            end

            title = 'Max Throughput Prediction';
            Ylabel = 'TPS';
            Xlabel = 'Time';

        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel] = lockPrediction(this)

            if strcmp(this.taskDescription.lockType, 'waitTime')
                my_train_lock = this.trainConfig.lockWaitTime;
                my_test_lock = this.testConfig.lockWaitTime;
            elseif strcmp(this.taskDescription.lockType, 'numberOfLocks')
                my_train_lock = this.trainConfig.currentLockWait;
                my_test_lock = this.testConfig.currentLockWait;
            elseif strcmp(this.taskDescription.lockType, 'numberOfConflicts')
                my_train_lock = this.trainConfig.lockWaitTime;
                my_test_lock = this.testConfig.lockWaitTime;
            else
                error(['Invalid lockType:' this.taskDescription.lockType]);
            end

            if this.taskDescription.learnLock == true % re-learn it!
                if strcmp(this.taskDescription.lockType, 'waitTime')
                    f = @(conf2, data)(getfield(useLockModel([0.125 0.0001 conf2], data, this.taskDescription.workloadName), 'TimeSpentWaiting'));
                elseif strcmp(this.taskDescription.lockType, 'numberOfLocks')
                    f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').LocksBeingHeld'));
                elseif strcmp(this.taskDescription.lockType, 'numberOfConflicts')
                    f = @(conf2, data)(eval('useLockModel([0.125 0.0001 conf2], data, ''TPCC'').totalWaits'));
                else
                    error(['Invalid lockType:' this.taskDescription.lockType]);
                end
                
                % taskDesc.emIters is hard-coded as 5 for now.
                domain_cost = barzanCurveFit(f, this.trainConfig.transactionCount, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);
                lock_conf = [0.125 0.0001 domain_cost];
            elseif isfield(this.testConfig.getStruct, 'lock_conf')
                lock_conf = this.testConfig.getStruct.lock_conf;
            else
                error('You should either let us re-learn or should give us the lock_conf to use!');
            end
         
            allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, this.taskDescription.workloadName);
            if strcmp(this.taskDescription.lockType, 'waitTime')
                myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);        
            elseif strcmp(this.taskDescription.lockType, 'numberOfLocks')
                myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
            elseif strcmp(this.taskDescription.lockType, 'numberOfConflicts')
                myPredictedLock = sum(allPreds.totalWaits, 2);
            else
                error(['Invalid lockType:' this.taskDescription.lockType]);
            end

            classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount);
            classifierLinPredictions = barzanLinInvoke(classifierLinModel, this.testConfig.transactionCount);

            blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount(:, comb1).*this.trainConfig.transactionCount(:, comb2)];
            blownTestC = [this.testConfig.transactionCount this.testConfig.transactionCount.*this.testConfig.transactionCount this.testConfig.transactionCount(:, comb1).*this.testConfig.transactionCount(:, comb2)];
            classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
            classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);
            
            treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS);
            treePredictions = barzanRegressTreeInvoke(treeModel, this.testConfig.TPS);

            % kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
            % kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);
            
            allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, this.taskDescription.workloadName);

            if strcmp(this.taskDescription.lockType, 'waitTime')
                thomasianPreds = sum(allPreds.TimeSpentWaiting, 2);        
            elseif strcmp(this.taskDescription.lockType, 'numberOfLocks')
                thomasianPreds = sum(allPreds.LocksBeingHeld, 2);
            elseif strcmp(this.taskDescription.lockType, 'numberOfConflicts')
                thomasianPreds = sum(allPreds.totalWaits, 2);
            else
                error(['Invalid lockType:' this.taskDescription.lockType]);
            end

            temp = [my_test_lock myPredictedLock classifierLinPredictions classQuadPredictions treePredictions thomasianPreds]; % kccaPredictions omitted.

            % by TPS only for now
            temp = [this.testConfig.TPS temp];

            temp = sortrows(temp, 1);

            Xdata = {temp(:,1)};
            Ydata = {temp(:,2:end)};

            Xlabel = 'TPS';
            Ylabel = 'Total time spent acquiring row locks (seconds)';
            legends = {'Actual', 'Our contention model', 'LR+class', 'quad+class', 'Dec. tree regression', 'Orig. Thomasian'};
            title = 'Lock Prediction';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = transactionCountsToCpuByTPS(this)
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
            
            legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data'};
            Ylabel = 'Average CPU (%)';
            title = 'Linear model: Avg CPU';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = transactionCountsToCpuByCounts(this)
            
            [trainMaxThroughputIdx trainMaxThroughput] = findMaxThroughput(this.trainConfig.TPSUngrouped);
            [testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(this.testConfig.TPSUngrouped);
            idx=1:trainMaxThroughputIdx;
            myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage(idx,:), this.trainConfig.transactionCount(idx,:));
            myCpuPred = barzanLinInvoke(myModelP, this.testConfig.transactionCount);
    
            xValuesTest = this.testConfig.transactionCount(:,this.taskDescription.whichTransactionToPlot) ./ this.testConfig.TPS;
            xValuesTrain = this.trainConfig.transactionCount(:,this.taskDescription.whichTransactionToPlot) ./ this.trainConfig.TPS;
            Xlabel = ['Fraction of transaction ' num2str(this.taskDescription.whichTransactionToPlot)];
            
            modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
            predictionsP  = barzanLinInvoke(modelP, this.testConfig.transactionCount);
            
            temp = [xValuesTest this.testConfig.averageCpuUsage predictionsP myCpuPred];
            
            Xdata = {temp(:,1)};
            Ydata = {[temp(:,2) temp(:,3) temp(:,4)]}
            Xdata{end+1} = xValuesTrain;
            Ydata{end+1} = this.trainConfig.averageCpuUsage;
            
            legends = {'Actual CPU usage', 'LR Predictions', 'LR+noise removal Predictions', 'Training data'}; 
            Ylabel = 'Average CPU (%)';
            title = 'Linear model: Avg CPU';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = transactionCountsToIO(this)
            modelIO = barzanLinSolve(this.trainConfig.diskWrite, this.trainConfig.transactionCount);
            predictionsIO = barzanLinInvoke(modelIO, this.testConfig.transactionCount);

            Xdata = {this.testConfig.TPS};
            Ydata = {[this.testConfig.diskWrite predictionsIO]};
            title = 'Linear model: Avg Physical Writes';
            legends = {'actual writes', 'predicted writes'};
            Ylabel = 'Written data (MB)';
            Xlabel = 'TPS';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = transactionCountsToLatency(this)
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
            end
            Ylabel = 'Time (seconds)';
            Xlabel = '';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = transactionCountsWaitTimeToLatency(this)
            modelLw = barzanLinSolve(this.trainConfig.transactionLatency, [this.trainConfig.transactionCount this.trainConfig.lockWaitTime]);
            predictionsLw  = barzanLinInvoke(modelLw, [this.testConfig.transactionCount this.testConfig.lockWaitTime]);
            
            ok=[this.testConfig.transactionCount this.testConfig.transactionLatency];
            tempActual = [this.testConfig.transactionCount(:,1) this.testConfig.transactionLatency];
            tempPred = [this.testConfig.transactionCount(:,1) predictionsLw];
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
            end

            Xlabel = horzcat('# of trans type ', num2str(this.testConfig.transactionType(1)-1));
            Ylabel = 'Time (seconds)';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = blownTransactionCountsToCpu(this)
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
            legends = {'Actual CPU usage', 'Predicted CPU usage]'};
            title = 'Quadratic Model: Average CPU';
            Xlabel = '';
            Ylabel = 'Average CPU (%)';
        end % end function
        
        function [title legends Xdata Ydata Xlabel Ylabel] = blownTransactionCountsToIO(this)
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

            title = 'Quadratic Model: Average Physical Writes';
            legends = {'Actual Writes', 'Predicted Writes'};
            Ylabel = 'Written Data (MB)';
            Xlabel = '';
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel] = linearPrediction(this)
            trainY = this.trainConfig.logWriteMB;
            testY = this.testConfig.logWriteMB;

            model = barzanLinSolve(trainY, this.trainConfig.transactionCount);
            pred = barzanLinInvoke(model, this.testConfig.transactionCount);

            xValuesTest = this.testConfig.transactionCount(:,1)./ this.testConfig.TPS;
            xValuesTrain = this.trainConfig.transactionCount(:,1)./ this.trainConfig.TPS; 

            temp = [xValuesTest testY pred];
            temp = sortrows(temp, 1);

            Xdata = {temp(:,1)};
            Ydata = {[temp(:,2) temp(:,3)]};

            Xdata{end+1} = xValuesTest;
            Ydata{end+1} = trainY;

            Ylabel = 'Log Writes (MB)';
            Xlabel = 'TPS or Ratio of transaction type 1';
            legends = {'Actual', 'Predicted', 'Training Data'};
            title = 'Linear Prediction';
        end % end function

        function [title legends Xdata Ydata Xlabel Ylabel] = physicalReadPrediction(this)
            cacheMissRate = this.testConfig.logicalReads ./ this.testConfig.physicalReads;
            normalization = mean(this.testConfig.physicalReadsMB) ./ mean(cacheMissRate);

            xValuesTest = this.testConfig.TPS;

            temp = [xValuesTest this.testConfig.physicalReadsMB cacheMissRate*normalization];
            temp = sortrows(temp, 1);

            Xdata = {temp(:,1)};
            Ydata = {[temp(:,2) temp(:,3)]};

            title = 'Physical read volume and cache miss rate';
            legends = {'Physical Read Volume', 'Cache Miss Rate'};
            Ylabel = 'Data Read (MB per sec)';
            Xlabel = 'TPS or Ratio of transaction type 1';
        end % end function
        
    end % end methods

end % end classdef