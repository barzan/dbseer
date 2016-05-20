using MAT
using Regression
using DecisionTree

type PredictionCenter
	TEST_MODE_MIXTURE_TPS #default value:0
	TEST_MODE_DATASET #default value:1
	NUM_TPS_SAMPLES  #default value:100
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
	throttleIndividualTransactions #default value: false;
end
PredictionCenter()=PredictionCenter(0,0,100,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,false)

type PredictionConfig
	io_conf
	lock_conf
	transactionType
	groupingStrategy
	datasetList #default value: {};
	initialized #default value: false;
	mv # this can be grouped or ungrouped depending on group parameters.
	mvUngrouped # this is always ungrouped.
	configSummary

	transactionCount # C
	averageCpuUsage # P
	diskWrite # IO
	transactionLatency # L
	transactionLatencyPercentile
	lockWaitTime # W, NumOfWaitDueToLocks
	currentLockWait # LocksBeingWaitedFor
	TPS # TPS
	rowsChanged # RowsChanged
	pagesFlushed # PagesFlushed
	transactionMixture # Mixture
	logicalReads 
	physicalReads
	physicalReadsMB
	networkSendKB
	networkRecvKB
	logWriteMB # LogIOw

	transactionCountUngrouped # C
	averageCpuUsageUngrouped # P
	diskWriteUngrouped # IO
	transactionLatencyUngrouped # L
	transactionLatencyPercentileUngrouped
	lockWaitTimeUngrouped # W, NumOfWaitDueToLocks
	currentLockWaitUngrouped # LocksBeingWaitedFor
	TPSUngrouped # TPS
	rowsChangedUngrouped # RowsChanged
	pagesFlushedUngrouped # PagesFlushed
	transactionMixtureUngrouped # Mixture
	logicalReadsUngrouped 
	physicalReadsUngrouped
	physicalReadsMBUngrouped
	networkSendKBUngrouped
	networkRecvKBUngrouped
	logWriteMBUngrouped # LogIOw

end
PredictionConfig()=PredictionConfig(0,0,0,0,{},false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)

type typeOfPredictions
	R
	T_total
	M_total
	Vp
	V
	W
	Pcon
	totalWaits
	TimeSpentWaiting
	LocksBeingHeld
end

type typeOfFlushRate
	io_conf
	workloadName
end

type typeOfConcurrencyLevel
	lock_conf
	workloadName
end
