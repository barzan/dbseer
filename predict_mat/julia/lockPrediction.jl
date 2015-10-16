function lockPrediction(this::PredictionCenter)
	meanAbsError = {};
	meanRelError = {};
	errorHeader = {};
	extra = {};
	if this.testMode == 1 #PredictionCenter.TEST_MODE_DATASET
		if this.lockType == "waitTime"
			my_train_lock = this.trainConfig.lockWaitTime;
			my_test_lock = this.testConfig.lockWaitTime;
		elseif this.lockType == "numberOfLocks"
			my_train_lock = this.trainConfig.currentLockWait;
			my_test_lock = this.testConfig.currentLockWait;
		elseif this.lockType == "numberOfConflicts"
			my_train_lock = this.trainConfig.lockWaitTime;
			my_test_lock = this.testConfig.lockWaitTime;
		else
			error(string("Invalid lockType:", this.lockType));
		end

		if this.learnLock == true # re-learn it!
			#%taskDesc.emIters is hard-coded as 5 for now.
			domain_cost = barzanCurveFit(this.lockType, this.trainConfig.transactionCount, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);
			lock_conf = [0.125 0.0001 domain_cost];
		elseif !isempty(this.lockConf)
			lock_conf = this.lockConf;
		else
			error("You should either let us re-learn or should give us the lock_conf to use!");
		end

		# %allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, this.workloadName);
		allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, "TPCC");

		if this.lockType == "waitTime"
			myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);
			if(length(myPredictedLock)==1)
				myPredictedLock=myPredictedLock[1]
			end
		elseif this.lockType == "numberOfLocks"
			myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
			if(length(myPredictedLock)==1)
				myPredictedLock=myPredictedLock[1]
			end
		elseif this.lockType == "numberOfConflicts"
			myPredictedLock = sum(allPreds.totalWaits, 2);
			if(length(myPredictedLock)==1)
				myPredictedLock=myPredictedLock[1]
			end
		else
                    error(string("Invalid lockType:", this.lockType));
		end

		classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount);
		classifierLinPredictions = barzanLinInvoke(classifierLinModel, this.testConfig.transactionCount);

		range = [1:1:size(this.trainConfig.transactionCount,2)];
		combs = collect(combinations(range,2))
		tempLen = length(combs)
		comb1 = zeros(tempLen,1);
		for i=tempLen:-1:1
			comb1[tempLen-i+1] = combs[i][1]
		end
		comb2 = zeros(tempLen,1);
		for i=tempLen:-1:1
			comb2[tempLen-i+1] = combs[i][2]
		end
		
		#Julia: may have some bugs
		blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount[:, vec(comb1)].*this.trainConfig.transactionCount[:, vec(comb2)]];
		blownTestC = [this.testConfig.transactionCount this.testConfig.transactionCount.*this.testConfig.transactionCount this.testConfig.transactionCount[:, vec(comb1)].*this.testConfig.transactionCount[:, vec(comb2)]];
		classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
		classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);
		
		treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS);
		treePredictions = barzanRegressTreeInvoke(treeModel, this.testConfig.TPS);
		
		#% kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
		#% kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);

		#% allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, this.workloadName);
		allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, "TPCC");

		
		if this.lockType == "waitTime"
			thomasianPreds = sum(allPreds.TimeSpentWaiting, 2);
			if(length(thomasianPreds)==1)
				thomasianPreds=thomasianPreds[1]
			end
		elseif this.lockType == "numberOfLocks"
			thomasianPreds = sum(allPreds.LocksBeingHeld, 2);
			if(length(thomasianPreds)==1)
				thomasianPreds=thomasianPreds[1]
			end
		elseif this.lockType == "numberOfConflicts"
			thomasianPreds = sum(allPreds.totalWaits, 2);
			if(length(thomasianPreds)==1)
				thomasianPreds=thomasianPreds[1]
			end
		else
			error(string("Invalid lockType:", this.lockType));
		end

		temp = [my_test_lock myPredictedLock classifierLinPredictions classQuadPredictions treePredictions thomasianPreds]; 
		#% kccaPredictions omitted.

		#% by TPS only for now
		temp = [this.testConfig.TPS temp];

		temp = sortrows(temp);

		Xdata = {temp[:,1]};
		Ydata = {temp[:,2:end]};

		for i=3:7
			push!(meanAbsError, mae(temp[:,i], temp[:,2]));
			push!(meanRelError, mre(temp[:,i], temp[:,2]));
		end

		Xlabel = "TPS";
		Ylabel = "Total time spent acquiring row locks (seconds)";
		legends = {"Actual", "Our contention model", "LR+class", "quad+class", "Dec. tree regression", "Orig. Thomasian"};
		errorHeader = legends[2:6];
		title = "Lock Prediction";
	elseif this.testMode == 0 #PredictionCenter.TEST_MODE_MIXTURE_TPS

		testTPS = this.testSampleTPS;
		testTransactionCount = this.testSampleTransactionCount;

		if this.lockType == "waitTime"
			my_train_lock = this.trainConfig.lockWaitTime;
		elseif this.lockType == "numberOfLocks"
			my_train_lock = this.trainConfig.currentLockWait;
		elseif this.lockType == "numberOfConflicts"
			my_train_lock = this.trainConfig.lockWaitTime;
		else
			error(string("Invalid lockType:", this.lockType));
		end

		if this.learnLock == true # re-learn it!

			#% taskDesc.emIters is hard-coded as 5 for now.
			domain_cost = barzanCurveFit(this.lockType, this.trainConfig.transactionCount, my_train_lock, [0.1 0.0000000001], [1000000 10], [50 0.01], [5 5]);

			lock_conf = [0.125 0.0001 domain_cost];
		elseif !isempty(this.lockConf)
			lock_conf = this.lockConf;
		else
			error("You should either let us re-learn or should give us the lock_conf to use!");
		end
		#% allPreds = useLockModel(lock_conf, this.testConfig.transactionCount, this.workloadName);
		allPreds = useLockModel(lock_conf, testTransactionCount, "TPCC");
		if this.lockType == "waitTime"
			myPredictedLock = sum(allPreds.TimeSpentWaiting, 2);
			if(length(myPredictedLock)==1)
				myPredictedLock=myPredictedLock[1]
			end
		elseif this.lockType == "numberOfLocks"
			myPredictedLock = sum(allPreds.LocksBeingHeld, 2);
			if(length(myPredictedLock)==1)
				myPredictedLock=myPredictedLock[1]
			end
		elseif this.lockType == "numberOfConflicts"
			myPredictedLock = sum(allPreds.totalWaits, 2);
			if(length(myPredictedLock)==1)
				myPredictedLock=myPredictedLock[1]
			end
		else
                    error(string("Invalid lockType:", this.lockType));
		end

		classifierLinModel = barzanLinSolve(my_train_lock, this.trainConfig.transactionCount);
		classifierLinPredictions = barzanLinInvoke(classifierLinModel, testTransactionCount);

		range = [1:1:size(this.trainConfig.transactionCount,2)];
		combs = collect(combinations(range,2))
		tempLen = length(combs)
		comb1 = zeros(tempLen,1);
		for i=tempLen:-1:1
			comb1[tempLen-i+1] = combs[i][1]
		end
		comb2 = zeros(tempLen,1);
		for i=tempLen:-1:1
			comb2[tempLen-i+1] = combs[i][2]
		end

		blownTrainC = [this.trainConfig.transactionCount this.trainConfig.transactionCount.*this.trainConfig.transactionCount this.trainConfig.transactionCount[:, vec(comb1)].*this.trainConfig.transactionCount[:, vec(comb2)]];
		blownTestC = [testTransactionCount testTransactionCount.*testTransactionCount testTransactionCount[:, vec(comb1)].*testTransactionCount[:, vec(comb2)]];
		classQuadModel = barzanLinSolve(my_train_lock, blownTrainC);
		classQuadPredictions = barzanLinInvoke(classQuadModel, blownTestC);
		
		treeModel = barzanRegressTreeLearn(my_train_lock, this.trainConfig.TPS);
		treePredictions = barzanRegressTreeInvoke(treeModel, testTPS);

		#% kccaModel = barzanKccaLearn(my_train_lock, blownTrainC);
		#% kccaPredictions = barzanKccaInvoke(kccaModel, blownTestC);

		#% allPreds = useLockModel([1 1 1 1], this.testConfig.transactionCount, this.workloadName);
		allPreds = useLockModel([1 1 1 1], testTransactionCount, "TPCC");


		if this.lockType == "waitTime"
			thomasianPreds = sum(allPreds.TimeSpentWaiting, 2);
			if(length(thomasianPreds)==1)
				thomasianPreds=thomasianPreds[1]
			end
		elseif this.lockType == "numberOfLocks"
			thomasianPreds = sum(allPreds.LocksBeingHeld, 2);
			if(length(thomasianPreds)==1)
				thomasianPreds=thomasianPreds[1]
			end
		elseif this.lockType == "numberOfConflicts"
			thomasianPreds = sum(allPreds.totalWaits, 2);
			if(length(thomasianPreds)==1)
				thomasianPreds=thomasianPreds[1]
			end
		else
			error(string("Invalid lockType:", this.lockType));
		end

		temp = [myPredictedLock classifierLinPredictions classQuadPredictions treePredictions thomasianPreds]; 
		#% kccaPredictions omitted.

		#% by TPS only for now
		temp = [testTPS temp];

		temp = sortrows(temp);

		Xdata = {temp[:,1]};
		Ydata = {temp[:,2:end]};


		Xlabel = "TPS";
		Ylabel = "Total time spent acquiring row locks (seconds)";
		legends = {"Our contention model", "LR+class", "quad+class", "Dec. tree regression", "Orig. Thomasian"};
		title = "Lock Prediction";
            end
	title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra
end # end function
