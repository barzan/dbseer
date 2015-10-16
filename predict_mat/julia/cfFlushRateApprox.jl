function cfFlushRateApprox(conf, transCounts)

	tic();
	initTime = 0;
	io_conf = conf.io_conf;
	max_log_capacity = io_conf[1];
	maxPagesPerSecs = io_conf[1];
	scaling = io_conf[1];

	minIO = 50;
	IOcorrection = 0;
	tolerance = 0.0000001;
	
	#Julia: loadPath needs to be modified
	if conf.workloadName == "TPCC"
		vars = matread("tpcc-write.mat")
		PPwrite = vars["PPwrite"]
		FreqWrite = vars["FreqWrite"]
	elseif conf.workloadName == "WIKI"
		vars = matread("wiki-write.mat")
		PPwrite = vars["PPwrite"]
		FreqWrite = vars["FreqWrite"]
	elseif conf.workloadName == "WIKI-FAKE"
		D = 375102;
		PPwrite = [[1:D]'.^1; [1:D]'.^1; [1:D]'.^0.1; [1:D]'.^1; [1:D]'.^1];
		FreqWrite = ones(size(PPwrite));
		PPwrite = 2 * 1 ./ PPwrite.^0.1;
		for i=1:length(PPwrite)
			if PPwrite[i] > 1
				PPwrite[i] = 1
			end
		end
	elseif conf.workloadName == "pgtpcc"
		vars = matread("tpcc-write.mat")
		PPwrite = vars["PPwrite"]
		FreqWrite = vars["FreqWrite"]
	else
	    error(string("Unknown workloadName in cfFlushRateApprox: ", conf.workloadName));
	end

	D = size(PPwrite,2);

	PPwrite = PPwrite * scaling;
	for i=1:length(PPwrite)
		if PPwrite[i] > 1
			PPwrite[i] = 1
		end
	end

	#[PP IX] = sort(PPwrite,2,'descend');
	IX = ones(size(PPwrite))
	for i=1:size(PPwrite,1)
		IX[i,:]=sortperm(vec(PPwrite[i,:]),rev=true)
	end
	PP = sort(PPwrite,2,rev=true)

	freq = FreqWrite;
	for i=1:size(freq,1)
	    freq[i,:]=FreqWrite[i,vec(IX[i,:])];
	end
	#% Now PP is the sorted version of PPwrite and freq is also sorted along
	#% with PP to keep the correspondence between PP and freq.

	nClusters=1;
	startIdx =1;

	for i=2:D
		if sum(abs(PP[:,startIdx]-PP[:,i])) + sum(abs(freq[:,startIdx]-freq[:,i]))> tolerance
			nClusters = nClusters +1;
			startIdx = i;
		end
	end
	#%now we know that we need to have 'cluster' number of buckets!
	nClusters
	newPP = zeros(size(PP,1), nClusters);
	newFreq = zeros(size(freq,1), nClusters);
	counts = zeros(1, nClusters);

	curCluster=1;
	startIdx =1;
	newPP[:,curCluster] = PP[:,1];
	newFreq[:,curCluster] = freq[:,1];
	counts[curCluster] = 1;

	for i=2:D
		if sum(abs(PP[:,startIdx]-PP[:,i])) + sum(abs(freq[:,startIdx]-freq[:,i]))> tolerance
			curCluster = curCluster +1;
			startIdx = i;
			newPP[:,curCluster] = PP[:,i];
			newFreq[:,curCluster] = freq[:,i];
			counts[curCluster] = 1;
		else
			newPP[:,curCluster] = newPP[:,curCluster] + PP[:,i];
			newFreq[:,curCluster] = newFreq[:,curCluster] + freq[:,i];
			counts[curCluster] = counts[curCluster] + 1;
		end
	end

	for i=1:size(newPP,1)
		newPP[i,:] = newPP[i,:] ./ counts;
	    newFreq[i,:] = newFreq[i,:] ./ counts;
	end


	PP = newPP;
	freq = newFreq;
	#%%%%%%%%%% remove the part before this line

	flushRates = zeros(size(transCounts,1),1);

	#relation: transCounts[i,:]=uniqueTransCounts[bigIdx[i],:]
	#[uniqueTransCounts smallIdx bigIdx]= unique(transCounts, 'rows');

	uniqueTransCounts = sortrows(unique(transCounts,1))
	bigIdx = zeros(size(transCounts,1),1)
	tempHashOfUniqueTransCounts = zeros(size(uniqueTransCounts,1),1)
	tempHashOfTransCounts = zeros(size(transCounts,1),1)
	for i = 1:size(uniqueTransCounts,1)
		tempHashOfUniqueTransCounts[i] = hash(uniqueTransCounts[i,:])
	end 

	for i = 1:size(transCounts,1)
		tempHashOfTransCounts[i] = hash(transCounts[i,:])
	end

	for i = 1:size(transCounts,1)
		bigIdx[i] = findfirst(tempHashOfUniqueTransCounts,tempHashOfTransCounts[i])
	end

	uniqueFlush = zeros(size(uniqueTransCounts,1),1);

	#%%initialization %%%%%%%%
	logSizePerTransaction = 1;

	L = max_log_capacity / logSizePerTransaction;

	couldNotKeepUp = false;

	d1 = 0;
	d2 = 0;
	d3 = 0;
	f = 0;
	for i=1:size(uniqueTransCounts,1)

		initTime = initTime + toc();
		tic();

		curTransCounts = uniqueTransCounts[i,:];
		tps = sum(curTransCounts);
		T = probOfBeingChosenAtLeastOnce(PP, freq, curTransCounts);
		mysum = zeros(size(T));

		p1 = zeros(size(T));
		p2 = zeros(size(T));
		p3 = ones(size(T));

		nRounds = 0;
		avgf = 0;
		oldAvgf = 1e10;
		seenFullLog = 0;

		howManySecondsToRotate = round(L / tps)-1;

		if 1==0
			Tpowers = (1-T).^howManySecondsToRotate;
			cachedCoef = (1-tps/L)^howManySecondsToRotate;
			mysum = (1-T).^howManySecondsToRotate - (1-tps/L).^howManySecondsToRotate;
		else
			Tpowers = recpow((1-T), howManySecondsToRotate);
			cachedCoef = recpow((1-tps/L), howManySecondsToRotate);
			mysum = recpow(1-T, howManySecondsToRotate) - recpow(1-tps/L, howManySecondsToRotate);
		end

		onesIdx = Int64[]
		nononesIdx = Int64[]
		for j=1:length(T)
			if T[j] == tps/L
				onesIdx = push!(onesIdx,j)
			else
				nononesIdx = push!(nononesIdx,j)
			end
		end

		mysum[onesIdx] = howManySecondsToRotate * Tpowers[onesIdx] ./ (1-T[onesIdx]);
		mysum[nononesIdx] = mysum[nononesIdx] ./ (1-T[nononesIdx]-1+tps/L);

		epsilon = 0.000001;

		initTime = initTime + toc();
		tic();
		println(string("initialization time=", initTime));

		while abs(avgf-oldAvgf)>0.0001 || seenFullLog<100
			oldAvgf = avgf;

			d1 = sum(p1 .* counts);
			if(length(d1)==1)
				d1=d1[1]
			end
			d2 = sum(p2 .* counts);
			if(length(d2)==1)
				d2=d2[1]
			end
			d3 = sum(p3 .* counts);
			if(length(d3)==1)
				d3=d3[1]
			end
			f = min(d1/round(L/tps), maxPagesPerSecs);
			d1 = d1 - f*howManySecondsToRotate;
			if d1>f+epsilon
				couldNotKeepUp = true;
			end
			#%%%%%%%%%%%%%% THIS IS THE LINE TO CHANGE!!
			#%p2 = p2 + (1-p1-p2).* T;
			#%we know that when the log neds to be rotated, we have the following:

			#%right when the log becomes full but before the rotation we have:

			p3 = p3 .* Tpowers + (tps/L) * p1 .* mysum;
			p1 = p1 * cachedCoef;
			p2 = 1 - p1 - p3;

			#%now we need to rotate the log and flush the last bit of remaining
			#%logs
			p3 = p3 + p1;
			p1 = p2+0.0;
			p2[:,:] = 0;

			seenFullLog = seenFullLog + 1;

			if f<minIO
				f = f + IOcorrection;
			end

			avgf = (f + nRounds*avgf) / (1+nRounds);
			nRounds = nRounds+1;
			#%allf(n) = f;
			if mod(nRounds,1000)==0
				println(nRounds)
			end
		end

		#%plot(allf(1:n),'-');
		uniqueFlush[i] = avgf;

	end # for the for over different TPSs

	for i=1:size(transCounts,1)
	    flushRates[i] = uniqueFlush[bigIdx[i]];
	end


	if couldNotKeepUp
		println(string("WARNING: We could not keep up! ",d1-f," pages left at log rotation time. f=", f))
	end

	initTime = initTime + toc()
	println(string("cfFlushRateApprox  time=",initTime));


	return flushRates
end # of function
