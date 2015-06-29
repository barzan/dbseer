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

function [ estimatedDirtyPages  flushRates] = estimateWriteIO(startingDirtyPages, D, cur_log_capacity, max_log_capacity, maxPagesPerSecs, logSize, rowsChanged)
estimatedDirtyPages = zeros(size(rowsChanged,1),1);    
flushRates = zeros(size(rowsChanged,1),1);    

estimatedDirtyPages(1) = startingDirtyPages;
if cur_log_capacity <= 0
   flushRates(1) = max_log_capacity;
   cur_log_capacity = max_log_capacity;
else
   flushRates(1) = estimatedDirtyPages(1) .* rowsChanged(1) *logSize / cur_log_capacity; % - initialFlushRate;
end

for i=1:size(rowsChanged,1)-1
   cur_log_capacity = cur_log_capacity - rowsChanged(i)*logSize;
   if cur_log_capacity <= 0
       flushRates(i+1) = max_log_capacity;
       cur_log_capacity = max_log_capacity;
   else
       flushRates(i+1) = estimatedDirtyPages(i) .* rowsChanged(i) *logSize / cur_log_capacity; % - flushRates(i-1);
   end
    
   estimatedDirtyPages(i+1) = estimatedDirtyPages(i) + mapRowsToPages(D-estimatedDirtyPages(i), ...
                                                rowsChanged(i)*(D-estimatedDirtyPages(i))/D); % - flushRates(i); 
end    



    
    
    

end

