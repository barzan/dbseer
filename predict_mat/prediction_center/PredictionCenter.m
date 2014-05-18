classdef PredictionCenter

    properties
        taskDescription
        trainConfigList
        testConfig
    end
    
    methods
        function result = performPrediction(this)
            if this.taskDescription.taskName == 'FlushRatePrediction'
                result = this.flushRatePrediction;
            else
                error(strcat('Unsupported task name: ', this.taskDescription.taskName));
            end
        end
        
        function result = flushRatePrediction(this)
            if isempty(this.testConfig.groupingStrategy)
                mv = load_modeling_variables(this.testConfig.directory, this.testConfig.signature);
            else
                groupStrategy = struct('groupParams', this.testConfig.groupingStrategy.getStruct);
                mv = load_modeling_variables(this.testConfig.directory, this.testConfig.signature, groupStrategy);
            end
            transactionsForTest = mv.clientIndividualSubmittedTrans(:, this.testConfig.transactionTypes);
            config = struct('io_conf', this.testConfig.ioConfiguration, 'workloadName', this.taskDescription.workloadName);
            result = cfFlushRateApprox(config, transactionsForTest);
        end
    end

end