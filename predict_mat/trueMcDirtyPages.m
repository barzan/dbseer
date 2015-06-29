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

function avgDirty = trueMcDirtyPages(uniquePages, TPS, seconds)

pages = zeros(1, uniquePages);
requests = TPS*seconds;

avgDirty = 0;

nIterations = 100000;

for iter=1:nIterations
    pages(:) = 0;
    for r=1:requests 
        p = randsample(uniquePages,1);
        pages(p) = 1; 
    end
    dirty = length(find(pages));
    avgDirty = dirty + avgDirty;
end

avgDirty = avgDirty / nIterations; 

end

