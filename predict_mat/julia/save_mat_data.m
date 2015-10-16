function save_mat_data(dbseerPath,pc)

path=strcat(dbseerPath,'/predict_mat/julia/datForJulia.mat');

pctrainConfigio_conf=pc.trainConfig.io_conf;
pctrainConfiglock_conf=pc.trainConfig.lock_conf;
pctrainConfigtransactionType=pc.trainConfig.transactionType;
pctrainConfiggroupingStrategy=pc.trainConfig.groupingStrategy;
pctrainConfiginitialized=pc.trainConfig.initialized;
pctrainConfigconfigSummary=pc.trainConfig.configSummary;
pctrainConfigcurrentLockWait=pc.trainConfig.currentLockWait;
pctrainConfiglockWaitTime=pc.trainConfig.lockWaitTime;
pctrainConfigpagesFlushed=pc.trainConfig.pagesFlushed;
pctrainConfigTPS=pc.trainConfig.TPS;
pctrainConfigTPSUngrouped=pc.trainConfig.TPSUngrouped;
pctrainConfigtransactionCount=pc.trainConfig.transactionCount;
pctrainConfigaverageCpuUsage=pc.trainConfig.averageCpuUsage;
pcTEST_MODE_MIXTURE_TPS=pc.TEST_MODE_MIXTURE_TPS;
pcTEST_MODE_DATASET=pc.TEST_MODE_DATASET;
pcNUM_TPS_SAMPLES=pc.NUM_TPS_SAMPLES;
pctestVar=pc.testVar;
pctaskName=pc.taskName;
pcworkloadName=pc.workloadName;
pclockType=pc.lockType;
pclearnLock=pc.learnLock;
pcwhichTransactionToPlot=pc.whichTransactionToPlot;
pcioConf=pc.ioConf;
pclockConf=pc.lockConf;
pctestMode=pc.testMode;
pctestConfig=pc.testConfig;
pctestMixture=pc.testMixture;
pctestMinTPS=pc.testMinTPS;
pctestMaxTPS=pc.testMaxTPS;
pctestWorkloadRatio=pc.testWorkloadRatio;
pctestSampleTPS=pc.testSampleTPS;
pctestSampleTransactionCount=pc.testSampleTransactionCount;
pcthrottleLatencyType=pc.throttleLatencyType;
pcthrottleTargetLatency=pc.throttleTargetLatency;
pcthrottleTargetTransactionIndex=pc.throttleTargetTransactionIndex;
pcthrottlePenalty=pc.throttlePenalty;
pcthrottleIndividualTransactions=pc.throttleIndividualTransactions;

if exist('OCTAVE_VERSION')
	save('-v7',path,'pctrainConfigio_conf','pctrainConfiglock_conf', ...
	'pctrainConfigtransactionType','pctrainConfiggroupingStrategy', ...
	'pctrainConfiginitialized','pctrainConfigconfigSummary','pctrainConfigcurrentLockWait', ...
	'pctrainConfiglockWaitTime','pctrainConfigpagesFlushed','pctrainConfigTPS','pctrainConfigTPSUngrouped', ...
	'pctrainConfigtransactionCount','pctrainConfigaverageCpuUsage', ...
	'pcTEST_MODE_MIXTURE_TPS','pcTEST_MODE_DATASET','pcNUM_TPS_SAMPLES', ...
	'pctestVar','pctaskName','pcworkloadName','pclockType','pclearnLock','pcwhichTransactionToPlot','pcioConf', ...
	'pclockConf','pctestMode','pctestConfig','pctestMixture','pctestMinTPS','pctestMaxTPS','pctestWorkloadRatio', ...
	'pctestSampleTPS','pctestSampleTransactionCount','pcthrottleLatencyType','pcthrottleTargetLatency', ...
	'pcthrottleTargetTransactionIndex','pcthrottlePenalty','pcthrottleIndividualTransactions');
else
	save(path,'pctrainConfigio_conf','pctrainConfiglock_conf', ...
	'pctrainConfigtransactionType','pctrainConfiggroupingStrategy', ...
	'pctrainConfiginitialized','pctrainConfigconfigSummary','pctrainConfigcurrentLockWait', ...
	'pctrainConfiglockWaitTime','pctrainConfigpagesFlushed','pctrainConfigTPS','pctrainConfigTPSUngrouped', ...
	'pctrainConfigtransactionCount','pctrainConfigaverageCpuUsage', ...
	'pcTEST_MODE_MIXTURE_TPS','pcTEST_MODE_DATASET','pcNUM_TPS_SAMPLES', ...
	'pctestVar','pctaskName','pcworkloadName','pclockType','pclearnLock','pcwhichTransactionToPlot','pcioConf', ...
	'pclockConf','pctestMode','pctestConfig','pctestMixture','pctestMinTPS','pctestMaxTPS','pctestWorkloadRatio', ...
	'pctestSampleTPS','pctestSampleTransactionCount','pcthrottleLatencyType','pcthrottleTargetLatency', ...
	'pcthrottleTargetTransactionIndex','pcthrottlePenalty','pcthrottleIndividualTransactions');
end

clear pctrainConfigio_conf;
clear pctrainConfiglock_conf;
clear pctrainConfigtransactionType;
clear pctrainConfiggroupingStrategy;
clear pctrainConfiginitialized;
clear pctrainConfigconfigSummary;
clear pctrainConfigcurrentLockWait;
clear pctrainConfiglockWaitTime;
clear pctrainConfigpagesFlushed;
clear pctrainConfigTPS;
clear pctrainConfigTPSUngrouped;
clear pctrainConfigtransactionCount;
clear pctrainConfigaverageCpuUsage;
clear pcTEST_MODE_MIXTURE_TPS;
clear pcTEST_MODE_DATASET;
clear pcNUM_TPS_SAMPLES;
clear pctestVar;
clear pctaskName;
clear pcworkloadName;
clear pclockType;
clear pclearnLock;
clear pcwhichTransactionToPlot;
clear pcioConf;
clear pclockConf;
clear pctestMode;
clear pctestConfig;
clear pctestMixture;
clear pctestMinTPS;
clear pctestMaxTPS;
clear pctestWorkloadRatio;
clear pctestSampleTPS;
clear pctestSampleTransactionCount;
clear pcthrottleLatencyType;
clear pcthrottleTargetLatency;
clear pcthrottleTargetTransactionIndex;
clear pcthrottlePenalty;
clear pcthrottleIndividualTransactions;

end
