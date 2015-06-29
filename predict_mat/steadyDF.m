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

function [dirtyPages flushRate] = steadyDF(D, RowsChanged)
dirtyPages = 0;
prevFlushRate = 1e+20;
flushRate = 0;

bestC = [2027811.6708307797 2000000.0000000000 1.0000000000 0.8346540331 76443.8419762718 78408.0000000000 D]; %t12345-memless-long

while abs(prevFlushRate-flushRate) > 0.01
    prevFlushRate = flushRate;
    
    dirtyPages = dirtyPages + mapRowsToPages(D-dirtyPages, RowsChanged.*(D-dirtyPages)/D) - flushRate; 
    flushRate = estimateFlushRate(bestC, RowsChanged);
end


end

