function maxThroughputPrediction(this::PredictionCenter)

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

	if this.testMode == 1 #PredictionCenter.TEST_MODE_DATASET
		range = (1:15000)'';
		maxFlushRate = 300; # DY: This is hard-coded... should we do something about this?

		#%cfFlushRateApprox_conf = struct('io_conf', this.testConfig.io_conf, 'workloadName', this.workloadName);
		cfFlushRateApprox_conf = typeOfFlushRate(this.ioConf,"TPCC");
		myFlushRateThroughput = findClosestValue("cfFlushRateApprox", (1:6000)''*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);
		modelP = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.transactionCount);
		
		testMaxThroughputIdx, testMaxThroughput = findMaxThroughput(this.testConfig.TPSUngrouped);
		trainMaxThroughputIdx, trainMaxThroughput = findMaxThroughput(this.trainConfig.TPSUngrouped);
		
		actualThr = testMaxThroughput;
		if isempty(actualThr)
			actualThr = Max(vec(this.testConfig.TPSUngrouped));
		end

		idx=[]
		if isdefined(:trainMaxThroughputIdx) && !isempty(trainMaxThroughputIdx)
			idx=(1:trainMaxThroughputIdx)';
		else
			idx=(1:size(this.trainConfig.averageCpuUsage,1))';
		end

		#%CPU-based throughput with classification
		cpuC = barzanLinInvoke(modelP, range*this.testConfig.transactionMixture);

		tempLen=length(cpuC)
		cpuCLThroughput=[]
		while tempLen>=1
			if cpuC[tempLen]>88 && cpuC[tempLen]<90
				cpuCLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(cpuC)
		cpuCUThroughput=[]
		while tempLen>=1
			if cpuC[tempLen]>98 && cpuC[tempLen]<100
				cpuCUThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		#%CPU-based without classification
		cpuTModel = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.TPS);
		cpuT = barzanLinInvoke(cpuTModel, range);
		tempLen=length(cpuT)
		cpuTLThroughput=[]
		while tempLen>=1
			if cpuT[tempLen]>88 && cpuT[tempLen]<90
				cpuTLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(cpuT)
		cpuTUThroughput=[]
		while tempLen>=1
			if cpuT[tempLen]>98 && cpuT[tempLen]<100
				cpuTUThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage[vec(idx),:], this.trainConfig.transactionCount[vec(idx),:]);
		myCpuC = barzanLinInvoke(myModelP, range*this.testConfig.transactionMixture);
		tempLen=length(myCpuC)
		myCpuCLThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>88 && myCpuC[tempLen]<90
				myCpuCLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(myCpuC)
		myCpuCUThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>98 && myCpuC[tempLen]<100
				myCpuCUThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(myCpuC)
		myCpuCLThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>44 && myCpuC[tempLen]<45
				myCpuCLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(myCpuC)
		myCpuCUThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>59 && myCpuC[tempLen]<50
				myCpuCUThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		#%Our IO-based throughput
		cfFlushRateApprox_conf = typeOfFlushRate(this.ioConf,"TPCC");
		myFlushRateThroughput = findClosestValue("cfFlushRateApprox", (1:6000)''*this.testConfig.transactionMixture, maxFlushRate, cfFlushRateApprox_conf);

		#%Lock-based throughput
		#%getConcurrencyLebel_conf = struct('lock_conf', this.testConfig.lock_conf, 'workloadName', this.workloadName);
		getConcurrencyLebel_conf = typeOfConcurrencyLevel( this.lockConf, "TPCC");
		concurrencyThroughput = findClosestValue("getConcurrencyLevel", (1:10000)''*this.testConfig.transactionMixture, 160, getConcurrencyLebel_conf);

		#%Linear IO-based throughput
		modelFlushRate = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
		linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testConfig.transactionMixture);
		linFlushRateThroughput = 0
		for i=length(linFlushRate):-1:1
			if linFlushRate[i]<maxFlushRate
				linFlushRateThroughput = i
				break
			end
		end
			
		if isempty(linFlushRateThroughput)
			linFlushRateThroughput=0; 
		end

		myMaxThroughput1 = min(myCpuCLThroughput, myFlushRateThroughput, concurrencyThroughput);
		if myMaxThroughput1 == myCpuCLThroughput
			PredReasonIdx1 = 1
		elseif myMaxThroughput1 == myFlushRateThroughput
			PredReasonIdx1 = 2
		elseif myMaxThroughput1 == concurrencyThroughput
			PredReasonIdx1 = 3
		end
		myMaxThroughput2 = min(myCpuCUThroughput, myFlushRateThroughput, concurrencyThroughput);
		if myMaxThroughput2 == myCpuCUThroughput
			PredReasonIdx2 = 1
		elseif myMaxThroughput2 == myFlushRateThroughput
			PredReasonIdx2 = 2
		elseif myMaxThroughput2 == concurrencyThroughput
			PredReasonIdx2 = 3
		end


		Xdata = {[1:size(this.testConfig.TPS, 1)]''};
		Ydata = {this.testConfig.TPS};
		legends = {"Original Signal"};

		num_row = size(Xdata[1], 1);
		num_col = size(Xdata[1], 2);

		if !isempty(actualThr)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'');
			push!(Ydata,repmat([actualThr], num_row, num_col));
			push!(legends,"Actual Max Throughput");
		end
		if !isempty(cpuCLThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([cpuCLThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on adjusted LR for CPU+classification");
			push!(errorHeader,"Max Throughput based on adjusted LR for CPU+classification");
			push!(meanAbsError,mae(cpuCLThroughput, actualThr));
			push!(meanRelError,mre(cpuCLThroughput, actualThr));
		end
		if !isempty(cpuCUThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([cpuCUThroughput], num_row, num_col));
			push!(legends,"Max Throughput on LR for CPU+classification");
			push!(errorHeader,"Max Throughput on LR for CPU+classification");
			push!(meanAbsError,mae(cpuCUThroughput, actualThr));
			push!(meanRelError,mre(cpuCUThroughput, actualThr));
		end
		if !isempty(cpuTLThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([cpuTLThroughput], num_row, num_col));
			push!(legends,"Max Throughput on adjusted LR for CPU");
			push!(errorHeader,"Max Throughput on adjusted LR for CPU");
			push!(meanAbsError,mae(cpuTLThroughput, actualThr));
			push!(meanRelError,mre(cpuTLThroughput, actualThr));
		end
		if !isempty(cpuTUThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([cpuTUThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on LR for CPU");
			push!(errorHeader,"Max Throughput based on LR for CPU");
			push!(meanAbsError,mae(cpuTUThroughput, actualThr));
			push!(meanRelError,mre(cpuTUThroughput, actualThr));
		end
		if !isempty(myFlushRateThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([myFlushRateThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on our flush rate model");
			push!(errorHeader,"Max Throughput based on our flush rate model");
			push!(meanAbsError,mae(myFlushRateThroughput, actualThr));
			push!(meanRelError,mre(myFlushRateThroughput, actualThr));
		end
		if !isempty(linFlushRateThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([linFlushRateThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on LR for flush rate");
			push!(errorHeader,"Max Throughput based on LR for flush rate");
			push!(meanAbsError,mae(linFlushRateThroughput, actualThr));
			push!(meanRelError,mre(linFlushRateThroughput, actualThr));
		end
		if !isempty(concurrencyThroughput)
			push!(Xdata,[1:size(this.testConfig.TPS, 1)]'')
			push!(Ydata,repmat([concurrencyThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on our contention model");
			push!(errorHeader,"Max Throughput based on our contention model");
			push!(meanAbsError,mae(concurrencyThroughput, actualThr));
			push!(meanRelError,mre(concurrencyThroughput, actualThr));
		end

                title = "Max Throughput Prediction";
                Ylabel = "TPS";
                Xlabel = "Time";
	elseif this.testMode == 0 #PredictionCenter.TEST_MODE_MIXTURE_TPS
                #% testTPS = [this.testMinTPS:(this.testMaxTPS-this.testMinTPS)/(this.NUM_TPS_VALUES-1):this.testMaxTPS];
                #% testTransactionCount(1,:) = this.testMixture * this.testMinTPS;
                #% testTransactionCount(2,:) = this.testMixture * ((this.testMinTPS + this.testMaxTPS) / 2);
                #% testTransactionCount(3,:) = this.testMixture * this.testMaxTPS;

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
		cpuCLThroughput=[]
		while tempLen>=1
			if cpuC[tempLen]>88 && cpuC[tempLen]<90
				cpuCLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(cpuC)
		cpuCUThroughput=[]
		while tempLen>=1
			if cpuC[tempLen]>98 && cpuC[tempLen]<100
				cpuCUThroughput = tempLen
				break
			end
			tempLen -= 1
		end


		#%CPU-based without classification
		cpuTModel = barzanLinSolve(this.trainConfig.averageCpuUsage, this.trainConfig.TPS);
		cpuT = barzanLinInvoke(cpuTModel, range);

		tempLen=length(cpuT)
		cpuTLThroughput=[]
		while tempLen>=1
			if cpuT[tempLen]>88 && cpuT[tempLen]<90
				cpuTLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(cpuT)
		cpuTUThroughput=[]
		while tempLen>=1
			if cpuT[tempLen]>98 && cpuT[tempLen]<100
				cpuTUThroughput = tempLen
				break
			end
			tempLen -= 1
		end
		myModelP = barzanLinSolve(this.trainConfig.averageCpuUsage[vec(idx),:], this.trainConfig.transactionCount[vec(idx),:]);
		myCpuC = barzanLinInvoke(myModelP, range*this.testMixture);

		tempLen=length(myCpuC)
		myCpuCLThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>88 && myCpuC[tempLen]<90
				myCpuCLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(myCpuC)
		myCpuCUThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>98 && myCpuC[tempLen]<100
				myCpuCUThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(myCpuC)
		myCpuCLThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>44 && myCpuC[tempLen]<45
				myCpuCLThroughput = tempLen
				break
			end
			tempLen -= 1
		end

		tempLen=length(myCpuC)
		myCpuCUThroughput=[]
		while tempLen>=1
			if myCpuC[tempLen]>59 && myCpuC[tempLen]<50
				myCpuCUThroughput = tempLen
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

		#%Linear IO-based throughput
		modelFlushRate = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
		linFlushRate = barzanLinInvoke(modelFlushRate, range*this.testMixture);
		for i=length(linFlushRate):-1:1
			if linFlushRate[i]<maxFlushRate
				linFlushRateThroughput = i
				break
			end
		end
		if isempty(linFlushRateThroughput)
			linFlushRateThroughput=0
		end

		myMaxThroughput1 = min(myCpuCLThroughput, myFlushRateThroughput, concurrencyThroughput);
		if myMaxThroughput1 == myCpuCLThroughput
			PredReasonIdx1 = 1
		elseif myMaxThroughput1 == myFlushRateThroughput
			PredReasonIdx1 = 2
		elseif myMaxThroughput1 == concurrencyThroughput
			PredReasonIdx1 = 3
		end
		myMaxThroughput2 = min(myCpuCUThroughput, myFlushRateThroughput, concurrencyThroughput);
		if myMaxThroughput2 == myCpuCUThroughput
			PredReasonIdx2 = 1
		elseif myMaxThroughput2 == myFlushRateThroughput
			PredReasonIdx2 = 2
		elseif myMaxThroughput2 == concurrencyThroughput
			PredReasonIdx2 = 3
		end

		Xdata = {[1:size(testTPS, 1)]''};
		Ydata = {testTPS};
		legends = {"Signal Generated From User Input"};

		num_row = size(Xdata[1], 1);
		num_col = size(Xdata[1], 2);


		if !isempty(actualThr)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([actualThr], num_row, num_col));
			push!(legends,"Actual Max Throughput");
		end
		if !isempty(cpuCLThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([cpuCLThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on adjusted LR for CPU+classification");
		end
		if !isempty(cpuCUThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([cpuCUThroughput], num_row, num_col));
			push!(legends,"Max Throughput on LR for CPU+classification");
		end
		if !isempty(cpuTLThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([cpuTLThroughput], num_row, num_col));
			push!(legends,"Max Throughput on adjusted LR for CPU");
		end
		if !isempty(cpuTUThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([cpuTUThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on LR for CPU");
		end
		if !isempty(myFlushRateThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([myFlushRateThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on our flush rate model");
		end
		if !isempty(linFlushRateThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([linFlushRateThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on LR for flush rate");
		end
		if !isempty(concurrencyThroughput)
			push!(Xdata,[1:size(testTPS, 1)]'')
			push!(Ydata,repmat([concurrencyThroughput], num_row, num_col));
			push!(legends,"Max Throughput based on our contention model");
		end

		title = "Max Throughput Prediction";
		Ylabel = "TPS";
		Xlabel = "Time";
	end
	return title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra
end # end function
