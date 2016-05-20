function flushRatePredictionByCounts(this::PredictionCenter)
    meanAbsError = {};
    meanRelError = {};
    errorHeader = {};
    extra = {};
    
    title = "flushRatePredictionByCount"
    legends = {}
    Xdata = {}
    Ydata = {}
    Xlabel = ""
    Ylabel = ""

    treePred = [];
    linPred = [];
    classLinPred = [];
    #nnPred = []; currently not support neural network
    myPred = [];

    if this.testMode == 1 # PredictionCenter.TEST_MODE_DATASET
        
        treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
        naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
        betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);

        treePred = barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount);
        classLinPred = barzanLinInvoke(betterLinModel, this.testConfig.transactionCount);
        linPred = barzanLinInvoke(naiveLinModel, this.testConfig.TPS);

        config = typeOfFlushRate(this.ioConf, "TPCC");
        myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);
        println("this.testConfig.transactionCount: ", this.testConfig.transactionCount)
        println("this.ioConf: ", this.ioConf)
        println("myPred: ", myPred)
        totalTxCount = this.testConfig.transactionCount;
        #totalTxCount = []
        #totalTxCount = vcat(totalTxCount, this.testConfig.transactionCount);
        
        totalTPS = sum(this.testConfig.TPS, 2);
        temp = [sum(this.testConfig.pagesFlushed,2) sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)];
        temp = [totalTxCount[:,this.whichTransactionToPlot]./totalTPS temp];
        temp = sortrows(temp);
        
        Xdata = {temp[:,1]''};
        Ydata = {[temp[:,2] temp[:,3] temp[:,4] temp[:,5] temp[:,6]]};
        legends = ["Actual", "LR", "LR+classification", "Our model", "Tree regression"];

        for i=3:6
            push!(meanAbsError, mae(temp[:,i], temp[:,2]));
            push!(meanRelError, mre(temp[:,i], temp[:,2]));
        end

        title = string("Flush rate prediction with # test points = ", size(this.testConfig.TPS,1));
        Ylabel = "Average # of page flush per seconds";
        Xlabel = "TPS";

    elseif this.testMode == 0 #PredictionCenter.TEST_MODE_MIXTURE_TPS
        testTPS = this.testSampleTPS;
        testTransactionCount = this.testSampleTransactionCount;

        treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);

        naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);

        betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);

        treePred = barzanRegressTreeInvoke(treeModel, testTransactionCount);

        linPred = barzanLinInvoke(naiveLinModel, testTPS); 

        classLinPred = barzanLinInvoke(betterLinModel, testTransactionCount);

        config = typeOfFlushRate(this.ioConf, "TPCC");
        myPred = cfFlushRateApprox(config, testTransactionCount);

        temp = [sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)];
        temp = [testTransactionCount[:,this.whichTransactionToPlot] temp];
        temp = sortrows(temp);

        this.testVar = temp;
        Xdata = {temp[:,1]''};
        Ydata = {[temp[:,2] temp[:,3] temp[:,4] temp[:,5]]};
        legends = ["LR", "LR+classification", "Our model", "Tree regression"];

        title = string("Flush rate prediction with transaction mixture = ", this.testMixture, ", Min TPS = ", this.testMinTPS, ", Max TPS = ", this.testMaxTPS);
        Ylabel = "Average # of page flush per seconds";
        Xlabel = string("# of transaction ", this.trainConfig.transactionType[this.whichTransactionToPlot]);
    end

    return title, legends, Xdata, Ydata, Xlabel, Ylabel, meanAbsError, meanRelError, errorHeader, extra;
end
