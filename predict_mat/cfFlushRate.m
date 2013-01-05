function flushRates = cfFlushRate(conf, TPS)
max_log_capacity = conf(1,1);
maxPagesPerSecs = conf(1,2);
D = conf(1,3);

mixture = [1];


PP = 1:D;
PP = 1./PP;
%comment the next line to get power law, otherwise it would be uniform
PP = ones(1,D);
PP = PP / sum(PP);
overallTime = tic;



%%%%%%%%%% remove the part before this line

flushRates = zeros(size(TPS));

uniqueTps = unique(TPS);
uniqueFlush = zeros(size(uniqueTps));

%%initialization %%%%%%%%
logSizePerTransaction = 1;

L = max_log_capacity / logSizePerTransaction;

for i=1:length(uniqueTps)

    initTime = tic;

    tps = TPS(i);
    T = probOfBeingChosenAtLeastOnce(PP, mixture, tps);
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
        mysum = (1-T)^howManySecondsToRotate - (1-tps/L)^howManySecondsToRotate;
    else
        Tpowers = recpow((1-T), howManySecondsToRotate);
        cachedCoef = recpow((1-tps/L), howManySecondsToRotate);
        mysum = recpow(1-T, howManySecondsToRotate) - recpow(1-tps/L, howManySecondsToRotate);
    end    
    
    onesIdx = find(T==tps/L);
    nononesIdx = find(T~=tps/L);
    
    mysum(onesIdx) = howManySecondsToRotate * Tpowers(onesIdx) ./ (1-T(onesIdx));
    mysum(nononesIdx) = mysum(nononesIdx) ./ (1-T(nononesIdx)-1+tps/L);
    
    epsilon = 0.000001;

    initTime = toc(overallTime);
    fprintf(1,'initialization time=%f\n', initTime);
        
    while abs(avgf-oldAvgf)>0.0001 || seenFullLog<100 
        oldAvgf = avgf;
        
        d1 = sum(p1);
        d2 = sum(p2);
        d3 = sum(p3);
        
        f = min(d1/(L/tps), maxPagesPerSecs);
        
        d1 = d1 - f*howManySecondsToRotate;
        if d1>f+epsilon
            fprintf(1,'We could not keep up! %f pages left at log rotation time\n', d1-f);
        end
        %%%%%%%%%%%%%% THIS IS THE LINE TO CHANGE!!
        %p2 = p2 + (1-p1-p2).* T;
        %we know that when the log neds to be rotated, we have the following:
        
        %right when the log becomes full but before the rotation we have:       
        p3 = p3 .* Tpowers + (tps/L) * p1 .* mysum; 
        p1 = p1 * cachedCoef;
        p2 = 1 - p1 - p3;
        
        fprintf(1,'From p1=%f, p2=%f, p3=%f  (f=%f)\n', p1(1), p2(1), p3(1), f);
        
        %now we need to rotate the log and flush the last bit of remaining
        %logs
        p3 = p3 + p1;
        p1 = p2;
        p2(:,:) = 0;
        
        fprintf(1,'To p1=%f, p2=%f, p3=%f  (f=%f)\n', p1(1), p2(1), p3(1), f);
        
        seenFullLog = seenFullLog + 1;
        
        avgf = (f + nRounds*avgf) / (1+nRounds);
        nRounds = nRounds+1;
        %allf(n) = f;
        if mod(nRounds,1000)==0
            nRounds
        end
    end
    
    %plot(allf(1:n),'-');
    uniqueFlush(i) = avgf;
    
end % for the for over different TPSs

for i=1:length(TPS)
    flushRates(i) = uniqueFlush(uniqueTps==TPS(i));
end

elapsed = toc(overallTime);
fprintf(1,'cfFlushRate elapsed time=%f\n',elapsed);



end % of function
