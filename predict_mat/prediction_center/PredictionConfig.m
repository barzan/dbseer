classdef PredictionConfig < handle

    properties
        io_conf
        lock_conf
        transactionType
        groupingStrategy
    end
  
    properties (SetAccess='private', GetAccess='public')
        datasetList = {};
        initialized = false;
    end
    
    properties (SetAccess='private', GetAccess='public')
        mv % this can be grouped or ungrouped depending on group parameters.
        mvUngrouped % this is always ungrouped.
        configSummary
        
        transactionCount % C
        averageCpuUsage % P
        diskWrite % IO
        transactionLatency % L
        lockWaitTime % W, NumOfWaitDueToLocks
        currentLockWait % LocksBeingWaitedFor
        TPS % TPS
        rowsChanged % RowsChanged
        pagesFlushed % PagesFlushed
        transactionMixture % Mixture
        logicalReads 
        physicalReads
        physicalReadsMB
        networkSendKB
        networkRecvKB
        logWriteMB % LogIOw
        
        transactionCountUngrouped % C
        averageCpuUsageUngrouped % P
        diskWriteUngrouped % IO
        transactionLatencyUngrouped % L
        lockWaitTimeUngrouped % W, NumOfWaitDueToLocks
        currentLockWaitUngrouped % LocksBeingWaitedFor
        TPSUngrouped % TPS
        rowsChangedUngrouped % RowsChanged
        pagesFlushedUngrouped % PagesFlushed
        transactionMixtureUngrouped % Mixture
        logicalReadsUngrouped 
        physicalReadsUngrouped
        physicalReadsMBUngrouped
        networkSendKBUngrouped
        networkRecvKBUngrouped
        logWriteMBUngrouped % LogIOw

        clusteredPageFreq
        clusteredPageMix
    end
    
    methods
        function result = isInitialized(this)
            result = this.initialized;
        end
        function setGroupingStrategy(this, paramStruct)
            % if isfield(paramStruct, 'groupingStrategy')
            %     groupParam = GroupParameters;
            %     groupParam.setStruct(paramStruct);
            %     this.groupingStrategy = groupParam;
            %     this.initialized = false;
            % end
            this.groupingStrategy = paramStruct;
            this.initialized = false;
        end
        function setTransactionType(this, transactionType)
            this.transactionType = transactionType;
            this.initialized = false;
        end
        function setIOConfiguration(this, io_conf)
            this.io_conf = io_conf;
            this.initialized = false;
        end
        function setLockConfiguration(this, lock_conf)
            this.lock_conf = lock_conf;
            this.initialized = false;
        end
        function addDataset(this, data_profile)
            this.datasetList{end+1} = data_profile;
            this.initialized = false;
        end
        function cleanDataset(this)
            this.datasetList = {};
            this.initialized = false;
        end
        function mergeDataset(this)
            this.configSummary = '{';
            this.clusteredPageMix = [];
            this.clusteredPageFreq = [];
            howManyDesc = length(this.datasetList);
            for i=1:howManyDesc
                this.datasetList{i}.loadStatistics;
                conf = this.datasetList{i}.getStruct;
                if ~isempty(this.groupingStrategy)
                    [mv_i mv_ungrouped_i] = load_mv(conf.header, conf.monitor, conf.averageLatency, conf.percentileLatency, conf.transactionCount, conf.diffedMonitor, this.groupingStrategy);
                else
                    [mv_i mv_ungrouped_i] = load_mv(conf.header, conf.monitor, conf.averageLatency, conf.percentileLatency, conf.transactionCount, conf.diffedMonitor);
                end
    
                if i==1
                    this.mv = mv_i;
                    this.mvUngrouped = mv_ungrouped_i;
                    % if ~isempty(this.io_conf)
                    %     this.io_conf = conf.io_conf;
                    % end
                    % if ~isempty(this.lock_conf)
                    %     this.lock_conf = conf.lock_conf;
                    % end
                else
                    this.mv = merge_structs(this.mv, mv_i);
                    this.mvUngrouped = merge_structs(this.mvUngrouped, mv_ungrouped_i);
                end
    
                tps = sum(this.mv.clientIndividualSubmittedTrans(:,this.transactionType), 2);
                this.configSummary = [this.configSummary num2str(min(tps)) '-' num2str(max(tps))];
                if i == 1
                    this.clusteredPageMix = conf.clusteredPageMix;
                    this.clusteredPageFreq = conf.clusteredPageFreq;
                else
                    this.clusteredPageMix = this.clusteredPageMix + conf.clusteredPageMix;
                    this.clusteredPageFreq = this.clusteredPageFreq + conf.clusteredPageFreq;
                end
                if i < howManyDesc
                    this.configSummary = [this.configSummary ',' ];
                end
            end
            this.clusteredPageMix = this.clusteredPageMix ./ howManyDesc;
            this.clusteredPageFreq = this.clusteredPageFreq ./ howManyDesc;
        end
        
        function initialize(this)
            this.transactionType = this.transactionType;
            this.mergeDataset;
            mv = this.mv;
            
            this.transactionCount = mv.clientIndividualSubmittedTrans(:,this.transactionType);
            this.averageCpuUsage = mean(mv.cpu_usr, 2);
            this.diskWrite = mv.osNumberOfSectorWrites;
            if isfield(mv, 'dbmsLockWaitTime')
                this.lockWaitTime = mv.dbmsLockWaitTime;
                this.currentLockWait = mv.dbmsCurrentLockWaits;
            else
                this.lockWaitTime = [];
                this.currentLockWait = [];
            end
            this.transactionLatency = mv.clientTransLatency(:,this.transactionType);
            this.TPS = sum(this.transactionCount,2);
            if isfield(mv, 'dbmsChangedRows')
                this.rowsChanged = mv.dbmsChangedRows;
            else
                this.rowsChanged = [];
            end
            this.pagesFlushed = mv.dbmsFlushedPages;
            idx = find(this.TPS>0);
            ratios = this.transactionCount(idx,:) ./ repmat(this.TPS(idx),1,size(this.transactionCount,2));
            this.transactionMixture = mean(ratios);
            this.logicalReads = mv.dbmsReadRequests;
            this.physicalReads = mv.dbmsReads;
            if isfield(mv, 'dbmsNumberOfDataReads')
                this.physicalReadsMB = mv.dbmsNumberOfDataReads / 1024 / 1024; 
            else
                this.physicalReadsMB = [];
            end
            this.networkSendKB = mv.osNetworkSendKB;
            this.networkRecvKB = mv.osNetworkRecvKB;
            this.logWriteMB = mv.dbmsLogWritesMB;
            
            %% Do it again for ungrouped.
            mvUngrouped = this.mvUngrouped;
            
            this.transactionCountUngrouped = mvUngrouped.clientIndividualSubmittedTrans(:,this.transactionType);
            this.averageCpuUsageUngrouped = mean(mvUngrouped.cpu_usr, 2);
            this.diskWriteUngrouped = mvUngrouped.osNumberOfSectorWrites;
            if isfield(mvUngrouped, 'dbmsLockWaitTime')
                this.lockWaitTimeUngrouped = mvUngrouped.dbmsLockWaitTime;
                this.currentLockWaitUngrouped = mvUngrouped.dbmsCurrentLockWaits;
            else
                this.lockWaitTimeUngrouped = [];
                this.currentLockWaitUngrouped = [];
            end
            this.transactionLatencyUngrouped = mvUngrouped.clientTransLatency(:,this.transactionType);
            this.TPSUngrouped = sum(this.transactionCount,2);
            if isfield(mvUngrouped, 'dbmsChangedRows')
                this.rowsChangedUngrouped = mvUngrouped.dbmsChangedRows;
            else
                this.rowsChangedUngrouped = [];
            end
            this.pagesFlushedUngrouped = mvUngrouped.dbmsFlushedPages;
            idx = find(this.TPS>0);
            ratios = this.transactionCountUngrouped(idx,:) ./ repmat(this.TPSUngrouped(idx),1,size(this.transactionCount,2));
            this.transactionMixtureUngrouped = mean(ratios);
            this.logicalReadsUngrouped = mvUngrouped.dbmsReadRequests;
            this.physicalReadsUngrouped = mvUngrouped.dbmsReads;
            if isfield(mvUngrouped, 'dbmsNumberOfDataReads')
                this.physicalReadsMBUngrouped = mvUngrouped.dbmsNumberOfDataReads / 1024 / 1024; 
            else
                this.physicalReadsMBUngrouped = [];
            end
            this.networkSendKBUngrouped = mvUngrouped.osNetworkSendKB;
            this.networkRecvKBUngrouped = mvUngrouped.osNetworkRecvKB;
            this.logWriteMBUngrouped = mvUngrouped.dbmsLogWritesMB;
            
            this.initialized = true;
        end
    end
end