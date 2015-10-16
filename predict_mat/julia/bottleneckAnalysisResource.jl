function bottleneckAnalysisResource(this::PredictionCenter)

	meanAbsError = {};
	meanRelError = {};
	errorHeader = {};
	extra = {};
	title = ""
	legends={}
	Xdata={}
	Ydata={}
	Xlabel=""
	Ylabel=""

	testTPS = this.testSampleTPS;
	testTransactionCount = this.testSampleTransactionCount;

	range = (1:15000)'';
	maxFlushRate = 6000;
	
	#%cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
	cfFlushRateApprox_conf = typeOfFlushRate(this.ioConf,"TPCC");

	myFlushRateThroughput = findClosestValue("cfFlushRateApprox", (1:6000)''*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);
	modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);

	#%[testMaxThroughputIdx testMaxThroughput] = findMaxThroughput(testTPS);
	trainMaxThroughputIdx, trainMaxThroughput = findMaxThroughput(this.trainConfig.TPSUngrouped);


	actualThr = [];
	
	idx=[]
	if isdefined(:trainMaxThroughputIdx) && !isempty(trainMaxThroughputIdx)
		idx=(1:trainMaxThroughputIdx)';
	else
		idx=(1:size(this.trainConfig.averageCpuUsage,1))';
	end

	#%CPU-based throughput with classification
	cpuC = barzanLinInvoke(modelP, range*this.testMixture);

	tempLen=length(cpuC)
	cpuThroughput=[]
	while tempLen>=1
		if cpuC[tempLen]>90 && cpuC[tempLen]<100
			cpuThroughput = tempLen
			break
		end
		tempLen -= 1
	end

	#Our IO-based throughput
	#% cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
	cfFlushRateApprox_conf = typeOfFlushRate(this.ioConf, "TPCC");
	myFlushRateThroughput = findClosestValue("cfFlushRateApprox", (1:6000)''*this.testMixture, maxFlushRate, cfFlushRateApprox_conf);

	#%Lock-based throughput
	#%getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
	getConcurrencyLebel_conf = typeOfConcurrencyLevel(this.lockConf, "TPCC");
	concurrencyThroughput = findClosestValue("getConcurrencyLevel", (1:10000)''*this.testMixture, 160, getConcurrencyLebel_conf);

	Xdata = {};
	Ydata = {};
	legends = {};

	push!(Ydata, cpuThroughput);
	push!(Ydata, myFlushRateThroughput);
	push!(Ydata, concurrencyThroughput);

	push!(legends, "CPU");
	push!(legends, "I/O");
	push!(legends, "Lock Contention");

	minThroughput = minimum([cpuThroughput myFlushRateThroughput concurrencyThroughput]);
	minIndex = indmin([cpuThroughput myFlushRateThroughput concurrencyThroughput]);

	extra = {minThroughput minIndex};

	title = "Bottleneck Analysis: Bottleneck Resource";
	Ylabel = "Throughput";
	Xlabel = "Resources";

	return title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra
end # end function
