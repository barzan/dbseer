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

function cl = getConcurrencyLevelNew(datasetPath, counts)

allPredictions  = useLockModelNew(datasetPath, counts);
if allPredictions.TimeSpentWaiting < 1e-5
    cl = 0; % as if there's not that many people in the system!
else
    cl = allPredictions.M_total;
end
% cl = allPredictions.M_total

end
