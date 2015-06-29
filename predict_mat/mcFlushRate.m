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

%function flushRates = mcFlushRate(conf, PP, mixture, TPS)
function flushRates = mcFlushRate(conf, TPS)
max_log_capacity = conf(1,1);
maxPagesPerSecs = conf(1,2);
D = conf(1,3);

mixture = [1];

%powerlaw
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

for i=1:length(uniqueTps)
    cur_log_capacity = max_log_capacity; % just assume that initially your log is empty!
    p1 = zeros(1,D);
    p2 = zeros(1,D);
    newP1 = p1;
    newP2 = p2;

    tps = TPS(i)
    T = probOfBeingChosenAtLeastOnce(PP, mixture, tps);
    
    n = 0;
    avgf = 0;
    oldAvgf = 1e10;
    seenFullLog = 0;
    
    d1=0;
    d2=0;

    while abs(avgf-oldAvgf)>0.0001 || seenFullLog<100 
        oldAvgf = avgf;
        
        d1 = sum(p1);
        d2 = sum(p2);
        if (cur_log_capacity <= tps*logSizePerTransaction)  %if I need to finish less than 1 sec!
            f = min(d1, maxPagesPerSecs);
            if f>0; newP1 = p1 * (1-f/d1); else newP1 = p1; end
            %d1 = d1 - f;
            if sum(newP1) <= 0
                % we can now rotate the log!
                newP1=p2;
                newP2(:,:) =0;
                d1 = d2;
                d2 = 0;
                cur_log_capacity = max_log_capacity;
                seenFullLog = seenFullLog + 1;
            else
                fprintf('what is going on? old d1=%f, new d1=%f, f=%f\n', sum(p1), sum(newP1), f);
            end
            fprintf(1,'seenFullLog=%d, p1=%f, p2=%f, p3=%f  (f=%f)\n', seenFullLog, newP1(1), newP2(1), 1-newP1(1)-newP2(1), f);
        else
            f = min(d1/ round(cur_log_capacity / (tps*logSizePerTransaction)) , maxPagesPerSecs);
            f = min(d1/ (cur_log_capacity / (tps*logSizePerTransaction)) , maxPagesPerSecs);
            if f>0; newP1 = p1 * (1-f/d1); else newP1 = p1; end
            %d1 = d1 - f;
            cur_log_capacity = cur_log_capacity - tps*logSizePerTransaction;
            %%%%%%%%%%%%%% THIS IS THE LINE TO CHANGE!!
            newP2 = p2 + (1-p1-p2).* T;
        end    
        
        p1 = newP1;
        p2 = newP2;

        fprintf(1,'log=%d, p1=%f, p2=%f, p3=%f  (f=%f)\n', cur_log_capacity, p1(1), p2(1), 1-p1(1)-p2(1), f);
        
        avgf = (f + n*avgf) / (1+n);
        n = n+1;
        %allf(n) = f;
        if mod(n,10000)==0
            n
            seenFullLog
        end
    end
    
    %plot(allf(1:n),'-');
    uniqueFlush(i) = avgf;
    
end % for the for over different TPSs

for i=1:length(TPS)
    flushRates(i) = uniqueFlush(uniqueTps==TPS(i));
end

elapsed = toc(overallTime);
fprintf(1,'elapsed time=%f\n',elapsed);

end % of function




