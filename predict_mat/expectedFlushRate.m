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

function [flushRates dirtyPagesOldLog] = expectedFlushRate(conf, TPS)
max_log_capacity = conf(1,1);
maxPagesPerSecs = conf(1,2);
D = conf(1,3);    
    
flushRates = zeros(size(TPS));
dirtyPagesOldLog = zeros(size(TPS));

uniqueTps = unique(TPS);
uniqueFlush = zeros(size(uniqueTps));
uniqueDPOL = zeros(size(uniqueTps));

for i=1:length(uniqueTps)
    %%initialization %%%%%%%%

    cur_log_capacity = max_log_capacity; % just assume that initially your log is empty!
    logSizePerTransaction = 1;
    %num of dirty pages in the old log
    d1 = 0;
    %num of dirty pages in the new log
    d2 = 0;
    
    %%see if we converge
    tps = TPS(i);
    
    n = 0;
    avgf = 0;
    oldAvgf = 1e10;
    avgd1 = 0;
    seenFullLog = 0;
    % allf = zeros(100000,1);
    
    while abs(avgf-oldAvgf)>0.00001 || seenFullLog<100 
        oldAvgf = avgf;

        if (cur_log_capacity <= tps*logSizePerTransaction)  %if I need to finish less than 1 sec!
            f = min(d1, maxPagesPerSecs);
            d1 = d1 - f;
            if d1 <= 0
                % we can now rotate the log!
                d1 = d2;
                d2 = 0;
                cur_log_capacity = max_log_capacity;
                seenFullLog = seenFullLog + 1;
            end
        else
            f = min(d1*tps*logSizePerTransaction / cur_log_capacity, maxPagesPerSecs);
            d1 = d1 - f;
            cur_log_capacity = cur_log_capacity - tps*logSizePerTransaction ;
            d2 = d2 + mapRowsToPages(D-d1-d2, tps*(D-d1-d2)/D); % BUG: This is incorrect!
        end    
        
        avgf = (f + n*avgf) / (1+n);
        avgd1 = (d1 + n*avgd1) / (1+n);

        n = n+1;
        %allf(n) = f;
    end
    
    %plot(allf(1:n),'-');
    uniqueFlush(i) = avgf;
    uniqueDPOL(i) = avgd1;
    
end % for the for over different TPSs

for i=1:length(TPS)
    flushRates(i) = uniqueFlush(uniqueTps==TPS(i));
    dirtyPagesOldLog(i) = uniqueDPOL(uniqueTps==TPS(i));
end

end % for function

