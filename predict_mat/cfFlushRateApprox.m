% Copyright 2013 Barzan Mozafari
%
% Licensed under the Apache License, Version 2.0 (the "License");
% you may not use this file except in compliance with the License.
% You may obtain a copy of the License at
%
%     http://www.apache.org/licenses/LICENSE-2.0
%
% Unless required by applicable law or agreed to in writing, software
% distributed under the License is distributed on an "AS IS" BASIS,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
% See the License for the specific language governing permissions and
% limitations under the License.

function flushRates = cfFlushRateApprox(conf, transCounts)
overallTime = tic;

io_conf = conf.io_conf;
max_log_capacity = io_conf(1,1);
maxPagesPerSecs = io_conf(1,2);
scaling = io_conf(1,3);

minIO = 50;
IOcorrection = 0;
tolerance=0.0000001;

if strcmp(conf.workloadName, 'TPCC')
    load('tpcc-write.mat');
elseif strcmp(conf.workloadName, 'WIKI')
    load('wiki-write.mat');
elseif strcmp(conf.workloadName, 'WIKI-FAKE')
    D = 375102;
    PPwrite = [(1:D).^1; (1:D).^1; (1:D).^0.1; (1:D).^1; (1:D).^1];
    FreqWrite = ones(size(PPwrite));
    PPwrite = 2 * 1 ./ PPwrite.^0.1;
    PPwrite(PPwrite>1) = 1;
elseif strcmp(conf.workloadName, 'pgtpcc')
    load('tpcc-write.mat');
else
    error(['Unknown workloadName in cfFlushRateApprox: ' conf.workloadName]);
end

D = size(PPwrite,2);

PPwrite = PPwrite * scaling;
PPwrite(PPwrite>1)=1;

[PP IX] = sort(PPwrite,2,'descend');
freq = FreqWrite;
for i=1:size(freq,1)
    freq(i,:)=FreqWrite(i,IX(i,:));
end
% Now PP is the sorted version of PPwrite and freq is also sorted along
% with PP to keep the correspondence between PP and freq.

nClusters=1;
startIdx =1;
for i=2:D
   if sum(abs(PP(:,startIdx)-PP(:,i))) + sum(abs(freq(:,startIdx)-freq(:,i)))> tolerance
       nClusters = nClusters +1;
       startIdx = i;
   end
end
%now we know that we need to have 'cluster' number of buckets!
nClusters
newPP = zeros(size(PP,1), nClusters);
newFreq = zeros(size(freq,1), nClusters);
counts = zeros(1, nClusters);

curCluster=1;
startIdx =1;
newPP(:,curCluster) = PP(:,1);
newFreq(:,curCluster) = freq(:,1);
counts(curCluster) = 1;

for i=2:D
   if sum(abs(PP(:,startIdx)-PP(:,i))) + sum(abs(freq(:,startIdx)-freq(:,i))) > tolerance
       curCluster = curCluster +1;
       startIdx = i;
       newPP(:,curCluster) = PP(:,i);
       newFreq(:,curCluster) = freq(:,i);
       counts(curCluster) = 1;
   else
       newPP(:,curCluster) = newPP(:,curCluster) + PP(:,i);
       newFreq(:,curCluster) = newFreq(:,curCluster) + freq(:,i);
       counts(curCluster) = counts(curCluster) + 1;
   end
end

for i=1:size(newPP,1)
    newPP(i,:) = newPP(i,:) ./ counts;
    newFreq(i,:) = newFreq(i,:) ./ counts;
end

PP = newPP;
freq = newFreq;
%%%%%%%%%% remove the part before this line

flushRates = zeros(size(transCounts,1),1);

[uniqueTransCounts smallIdx bigIdx]= unique(transCounts, 'rows');
uniqueFlush = zeros(size(uniqueTransCounts,1),1);

%%initialization %%%%%%%%
logSizePerTransaction = 1;

L = max_log_capacity / logSizePerTransaction;

couldNotKeepUp = false;

for i=1:size(uniqueTransCounts,1)

    initTime = tic;

    curTransCounts = uniqueTransCounts(i,:);
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

    onesIdx = find(T==tps/L);
    nononesIdx = find(T~=tps/L);

    mysum(onesIdx) = howManySecondsToRotate * Tpowers(onesIdx) ./ (1-T(onesIdx));
    mysum(nononesIdx) = mysum(nononesIdx) ./ (1-T(nononesIdx)-1+tps/L);

    epsilon = 0.000001;

    initTime = toc(overallTime);
    fprintf(1,'initialization time=%f\n', initTime);

    while abs(avgf-oldAvgf)>0.0001 || seenFullLog<100
        oldAvgf = avgf;

        d1 = sum(p1 .* counts);
        d2 = sum(p2 .* counts);
        d3 = sum(p3 .* counts);

        f = min(d1/round(L/tps), maxPagesPerSecs);

        d1 = d1 - f*howManySecondsToRotate;
        if d1>f+epsilon
            couldNotKeepUp = true;
        end
        %%%%%%%%%%%%%% THIS IS THE LINE TO CHANGE!!
        %p2 = p2 + (1-p1-p2).* T;
        %we know that when the log neds to be rotated, we have the following:

        %right when the log becomes full but before the rotation we have:

        p3 = p3 .* Tpowers + (tps/L) * p1 .* mysum;
        p1 = p1 * cachedCoef;
        p2 = 1 - p1 - p3;

        %now we need to rotate the log and flush the last bit of remaining
        %logs
        p3 = p3 + p1;
        p1 = p2;
        p2(:,:) = 0;

        seenFullLog = seenFullLog + 1;

        if f<minIO
           f = f + IOcorrection;
        end

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

for i=1:size(transCounts,1)
    flushRates(i) = uniqueFlush(bigIdx(i));
end

if couldNotKeepUp
    fprintf(1,'WARNING: We could not keep up! %f pages left at log rotation time. f=%f\n', d1-f, f);
end

elapsed = toc(overallTime);
fprintf(1,'cfFlushRateApprox  time=%f\n',elapsed);



end % of function
