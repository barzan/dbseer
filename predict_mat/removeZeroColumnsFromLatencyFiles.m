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


bareMinimum = [1 3 4 5 6 7]; %psql
%bareMinimum = [1 2 3 4 5 6]; %mysql
%bareMinimum = [];

clear dir;
myfiles = dir('*_avg_latency.al');
%myfiles = dir('*_rough_trans_count.al');

for i=1:length(myfiles);
    file = myfiles(i);
    fprintf(1, ['We found file: ' file.name '\n']);
    A = load(file.name);
    minA = min(A, [], 1);
    maxA = max(A, [], 1);
    whichOnesAreNonZero1 = (minA ~= 0);
    whichOnesAreNonZero2 = (maxA ~= 0);
    whichOnesAreNonZero = whichOnesAreNonZero1 | whichOnesAreNonZero2;
    nonZeroColIdx = find(whichOnesAreNonZero==1);
    columnsThatMatter = nonZeroColIdx;
    
    colIdx = [bareMinimum columnsThatMatter];
    colIdx = sort(unique(colIdx));
    fprintf(1, 'columnsThatMatter=%s\n', num2str(colIdx));
    if (length(colIdx) ~= 6) 
        error(['error: columnsThatMatter=' num2str(colIdx) '\n']);
        return;
    end
    
    B = A(:,colIdx);
    
    if (size(A,2) ~= size(B,2))
        fprintf(1, 'replaced\n');     
        save(file.name, 'B', '-ascii','-double');
    else
        fprintf(1, 'skipped\n');     
    end
end