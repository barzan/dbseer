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

function flushRates = estimateFlushRate(conf, TPS)
cur_log_capacity = conf(1,1);
max_log_capacity = conf(1,2);
maxPagesPerSecs = conf(1,3);
logSizePerTransaction = conf(1,4);
%num of dirty pages in the old log
d1 = conf(1,5);
%num of dirty pages in the new log
d2 = conf(1,6);
D = conf(1,7);

flushRates = zeros(size(TPS));

for i=1:length(TPS)
    if (cur_log_capacity <= TPS(i)*logSizePerTransaction)  %if I need to finish less than 1 sec!
        flushRates(i) = min(d1,maxPagesPerSecs);
        d1 = d1 - flushRates(i);
        if d1 <= 0
            % we can now rotate the log!
            d1 = d2;
            d2 = 0;
            cur_log_capacity = max_log_capacity;
        end
    else
        flushRates(i) = min(d1*TPS(i)*logSizePerTransaction / cur_log_capacity, maxPagesPerSecs);
        d1 = d1 - flushRates(i);
        cur_log_capacity = cur_log_capacity - TPS(i)*logSizePerTransaction ;
        d2 = d2 + mapRowsToPages(D-d1-d2, TPS(i)*(D-d1-d2)/D);
    end    
end

%currentPagesDirty.*rowsChanged ./ (2000000 - cumRowsChanged)
%flushRates = flushRates / 5;
end

