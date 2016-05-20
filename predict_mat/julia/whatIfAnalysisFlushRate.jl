function whatIfAnalysisFlushRate(this::PredictionCenter)
    meanAbsError = {};
    meanRelError = {};
	errorHeader = {};
    extra = {};

    title = ""
    legends = {}
    Xdata = {}
    Ydata = {}
    Xlabel = ""
    Ylabel = ""

    testTPS = this.testSampleTPS;
    testTransactionCount = this.testSampleTransactionCount;

    minTransactionCount = this.testMinTPS .* this.testMixture;

    avgTransactionCount = this.testWorkloadRatio .* mean(sum(this.trainConfig.transactionCount,2),1) .* this.testMixture;
    maxTransactionCount = this.testMaxTPS .* this.testMixture;
    whatIfTransactionCount = vcat(minTransactionCount, avgTransactionCount, maxTransactionCount);

    treeModel = barzanRegressTreeLearn(sum(this.trainConfig.pagesFlushed,2), this.trainConfig.transactionCount);
    treePred = barzanRegressTreeInvoke(treeModel, testTransactionCount);

    naiveLinModel = barzanLinSolve(sum(this.trainConfig.pagesFlushed,2), sum(this.trainConfig.TPS,2));
    linPred = barzanLinInvoke(naiveLinModel, testTPS);

    betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
    classLinPred = barzanLinInvoke(betterLinModel, testTransactionCount);

    emp = zeros(size(this.trainConfig.transactionCount,1), 0);

    config = typeOfFlushRate(this.ioConf, "TPCC");
    myPred = cfFlushRateApprox(config, testTransactionCount);
    whatifPrediction = cfFlushRateApprox(config, avgTransactionCount);
    transactionCounts = [mean(sum(this.trainConfig.transactionCount,2),1) sum(avgTransactionCount)];

    extra = Array{Array}(2);
    extra[1] = whatifPrediction; 
    extra[2] = transactionCounts;

    temp = [linPred classLinPred myPred treePred]; #% kccaPred is not included
    temp = [testTPS temp];

    temp = sortrows(temp);

    Xdata = {temp[:,1]''};
    Ydata = {[temp[:,2] temp[:,3] temp[:,4] temp[:,5]]};
    legends = ["LR", "LR+classification", "Our model", "Tree regression"];
    

    title = "What-if Analysis: Disk Flush Rate";
    Ylabel = "Average # of page flush per seconds";
    Xlabel = "TPS";

    return title, legends, Xdata, Ydata, Xlabel, Ylabel, meanAbsError, meanRelError, errorHeader, extra    
end