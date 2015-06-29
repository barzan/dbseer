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

function [ PP ] = primaryKeyDist2PageDist(P, Card, rowSize)
% P(i,t,k) is the probability that transaction i "updates" for the k'th key in table t 
%     this means that sum(P(i,:,:))==the average number of rows updated by
%     each transaction of type i. This does not have to be 1.
% Card(t) is the number of keys in table t 
% mixture(i) is the probability that an incoming transaction is of type i.
%     this means that sum(mixture)==1
% rowSize(t) is the avg size of a row in table t
% TPS is the steady TPS
%
% 
% Summary:
% PP(i,g) is the probability that a transaction of type i updates 
% the g'th page!
% 
% WARNING: in this version I am only worried about the write access
% pattern, and the reads will be later used in estimating the memory
% requirements.
overallTime = tic;

pageSize = 16*1024;

numTrans = size(P,1);
numTables = size(P,2);
maxNumKeys = size(P,3);

% Some sanity checks!
for i=1:numTables
    if rowSize(i) > 8*1024
        rowSize(i) = 8*1024;
    end 
end 

if any(rowSize>16*1024) 
    error('some rows do not even fit in one page!');
end

% we first have to convert P into a PP by aggregating keys into pages
% such that PP(i,g) is the probability that a transaction of type i updates 
% the g'th page!
nPages = sum(ceil(Card .* rowSize ./ pageSize)); 
PP = zeros(numTrans, nPages);

curPageIdx = 1;
curPageCapacity = pageSize;
buffer = 1;

for i=1:numTrans
    for t=1:numTables
        fprintf(1,'%d %d %d...\n',i,t,Card(t));
        for k=1:Card(t)
            if rowSize(t) <= curPageCapacity
                buffer = buffer * (1-P(i,t,k)); % buffer will eventually=prob of not being touched at all!
                curPageCapacity = curPageCapacity - rowSize(t);
            else
                PP(i, curPageIdx) = 1-buffer;
                buffer = 1;
                curPageIdx = curPageIdx+1;
                curPageCapacity = pageSize;
            end
        end
    end
    if buffer~=1        
        PP(i, curPageIdx) = 1-buffer;
        buffer = 1;
    end
end

save('page-dist.mat', 'PP');

elapsed = toc(overallTime);
fprintf(1,'elapsed time=%f\n',elapsed);

end

