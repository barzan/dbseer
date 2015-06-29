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

% Copyright [yyyy] [name of copyright owner]
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
function flushRates = cfFlushRateNew(io_conf, trans_info)
max_log_capacity = io_conf(1,1);
maxPagesPerSecs = io_conf(1,2);
D = trans_info.clustered_pages;
freq = trans_info.freq;
TPS = trans_info.tps;

maxRounds = 200000;

overallTime = tic;

%%%%%%%%%% remove the part before this line

flushRates = zeros(size(TPS,1), 1);

[uniqueTps smallIdx bigIdx] = unique(TPS, 'rows');
uniqueFlush = zeros(size(uniqueTps, 1), 1);
    
%%initialization %%%%%%%%
logSizePerTransaction = 1;

L = max_log_capacity / logSizePerTransaction;

PP = zeros(size(freq));
PP(freq>1)=1;

for i=1:size(uniqueTps, 1)
    
    initTime = tic;

    % tps = TPS(i);
    curTransCounts = uniqueTps(i,:);
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
    % fprintf(1,'initialization time=%f\n', initTime);
        
    while abs(avgf-oldAvgf)>0.0001 || seenFullLog<100 
        oldAvgf = avgf;

        d1 = sum(p1 .* D);
        d2 = sum(p2 .* D);
        d3 = sum(p3 .* D);
        
        f = min(d1/(L/tps), maxPagesPerSecs);
        
        d1 = d1 - f*howManySecondsToRotate;
        if d1>f+epsilon
            % fprintf(1,'We could not keep up! %f pages left at log rotation time\n', d1-f);
        end
        %%%%%%%%%%%%%% THIS IS THE LINE TO CHANGE!!
        %p2 = p2 + (1-p1-p2).* T;
        %we know that when the log neds to be rotated, we have the following:
        
        %right when the log becomes full but before the rotation we have:       
        p3 = p3 .* Tpowers + (tps/L) * p1 .* mysum; 
        p1 = p1 * cachedCoef;
        p2 = 1 - p1 - p3;
        
        % fprintf(1,'From p1=%f, p2=%f, p3=%f  (f=%f)\n', p1(1), p2(1), p3(1), f);
        
        %now we need to rotate the log and flush the last bit of remaining
        %logs
        p3 = p3 + p1;
        p1 = p2;
        p2(:,:) = 0;
        
        % fprintf(1,'To p1=%f, p2=%f, p3=%f  (f=%f)\n', p1(1), p2(1), p3(1), f);
        
        seenFullLog = seenFullLog + 1;
        
        avgf = (f + nRounds*avgf) / (1+nRounds);
        nRounds = nRounds+1;
        %allf(n) = f;
        if mod(nRounds,1000)==0
            nRounds
        end

        if nRounds > maxRounds
            break
        end
    end
    
    %plot(allf(1:n),'-');
    uniqueFlush(i) = avgf;
    
end % for the for over different TPSs

for i=1:size(TPS, 1)
    flushRates(i) = uniqueFlush(bigIdx(i));
end

elapsed = toc(overallTime);
fprintf(1,'cfFlushRate elapsed time=%f\n',elapsed);



end % of function
