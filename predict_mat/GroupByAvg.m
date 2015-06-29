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

function [groupedMatrix freuqencies] = GroupByAvg(matrix, keyIndices, relativeKeyDiff, minFreq, minKeyValue, maxKeyValue)

    idx = find(sum(matrix(:,keyIndices),2)>=minKeyValue);
    matrix = matrix(idx,:);
    
    idx = find(sum(matrix(:,keyIndices),2)<=maxKeyValue);
    matrix = matrix(idx,:);
    
    bigMatrix = sortrows([matrix(:,keyIndices) matrix]);
    matrix = bigMatrix(:,length(keyIndices)+1:end);
    key = matrix(:,keyIndices);       
    nRows = size(key,1);
        
    groupedMatrix = zeros(nRows, size(matrix,2));
    freuqencies = zeros(nRows,1);
    
    groupIdx = 1;
    freq = 0;    
    keySt = 1;
    
    for i=1:nRows
        if all(key(i,:) <= key(keySt,:)+relativeKeyDiff*key(keySt,:))
            freq = freq +1;
        else
            if freq >= minFreq
               groupedMatrix(groupIdx,:) = mean(matrix(keySt:i-1,:), 1); 
               freuqencies(groupIdx) = freq;
               groupIdx = groupIdx+1;
            end
            freq = 1;
            keySt = i;
        end            
    end
    if freq >= minFreq
       groupedMatrix(groupIdx,:) = mean(matrix(keySt:nRows,:), 1);
       freuqencies(groupIdx) = freq;
       groupIdx = groupIdx+1;
    end

    groupedMatrix = groupedMatrix(1:groupIdx-1,:);
    freuqencies = freuqencies(1:groupIdx-1);    

return; 

