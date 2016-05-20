function flushRatePredictionByTPS(this::PredictionCenter)
    meanAbsError = {};
    meanRelError = {};
    errorHeader = {};
    extra = {};
    
    title = "flushRatePredictionByTPS"
    legends = {}
    Xdata = {}
    Ydata = {}
    Xlabel = ""
    Ylabel = ""

    treePred = [];
    linPred = [];
    classLinPred = [];
    #nnPred = []; # currently not support NeuralNetLearn
    myPred = [];

    if this.testMode == 1 # PredictionCenter.TEST_MODE_DATASET
        treeModel = barzanRegressTreeLearn(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);      
        naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, sum(this.trainConfig.transactionCount, 2));
        betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);

        treePred = barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount);
        classLinPred = barzanLinInvoke(betterLinModel, this.testConfig.transactionCount);
        linPred = barzanLinInvoke(naiveLinModel, this.testConfig.TPS);

        #Alternative: (if size(mv.numOfTransType, 2) == 1 is not an invariant for .jl data)
        
        #for i=1:size(mv.numOfTransType, 2)
            #treePred = vcat(treePred, barzanRegressTreeInvoke(treeModel, this.testConfig.transactionCount[i]));

            #classLinPred = vcat(classLinPred, barzanLinInvoke(betterLinModel, this.testConfig.transactionCount[i]));

            #linPred = vcat(linPred, barzanLinInvoke(naiveLinModel, this.testConfig.TPS(:,i)));
        #end

        config = typeOfFlushRate(this.ioConf, "TPCC");
        myPred = cfFlushRateApprox(config, this.testConfig.transactionCount);
        
        #temp = [this.testConfig.pagesFlushed linPred classLinPred myPred treePred];
        temp = [sum(this.testConfig.pagesFlushed,2) sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)];
        temp = [sum(this.testConfig.TPS,2)'' temp];
        temp = sortrows(temp);

        Xdata = {temp[:,1]''};
        Ydata = {[temp[:,2] temp[:,3] temp[:,4] temp[:,5] temp[:,6]]};
        legends = {"Actual", "LR", "LR+classification", "Our model", "Tree regression"};

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
        treePred = barzanRegressTreeInvoke(treeModel, testTransactionCount)

        naiveLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.TPS);
        linPred = barzanLinInvoke(naiveLinModel, testTPS); 

        betterLinModel = barzanLinSolve(this.trainConfig.pagesFlushed, this.trainConfig.transactionCount);
        classLinPred = barzanLinInvoke(betterLinModel, testTransactionCount);

        config = typeOfFlushRate(this.ioConf, "TPCC");
        myPred = cfFlushRateApprox(config, testTransactionCount);
    
        temp = [linPred classLinPred myPred treePred];
        #temp = [sum(linPred,2) sum(classLinPred,2) sum(myPred,2) sum(treePred,2)]; 
        temp = [testTPS temp];
        temp = sortrows(temp);
        Xdata = {temp[:,1]''};
        Ydata = {[temp[:,2] temp[:,3] temp[:,4] temp[:,5]]};
        
        legends = {"LR", "LR+classification", "Our model", "Tree regression"};

        title = string("Flush rate prediction with transaction mixture = ", this.testMixture, ", Min TPS = ", this.testMinTPS, ", Max TPS = ", this.testMaxTPS);
        Ylabel = "Average # of page flush per seconds";
        Xlabel = "TPS";
    end

    return title, legends, Xdata, Ydata, Xlabel, Ylabel, meanAbsError, meanRelError, errorHeader, extra;
end
