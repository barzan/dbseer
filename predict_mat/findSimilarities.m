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

function findSimilarities(FeatureList, sig1, breakPointIndex1, sig2, breakPointIndex2)
mon1 = csvread(horzcat('monitor-',sig1),2);
mon2 = csvread(horzcat('monitor-',sig1),2);

range = 20;
s1 = breakPointIndex1 - range;
e1 = breakPointIndex1 + range;
s2 = breakPointIndex2 - range;
e2 = breakPointIndex2 + range;

if s1 < 1; s1 = 1; end
if s2 < 1; s2 = 1; end
if e1 > size(mon1,1); e1 = size(mon1,1); end
if e2 > size(mon2,1); e2 = size(mon2,1); end

subMon1 = mon1(s1:e1, FeatureList);
subMon2 = mon2(s2:e2, FeatureList);

avg1 = mean(subMon1)';
avg2 = mean(subMon2)';

diff = abs(avg1 - avg2);
both = abs(avg1+avg2)/2;
both(both==0) = 1;

relDiff = diff ./ both;

    figure;
    subplot(1,3,1);
    plot([avg1 avg2],'-');
    title('Both signals');
    ylabel('Actual Values');
    legend(horzcat(sig1,' around ',num2str(breakPointIndex1)), horzcat(sig2,' around ',num2str(breakPointIndex2)));
    grid on;
    
    subplot(1,3,2);
    plot(diff,'-');
    title('Diff');
    ylabel('Difference');
    grid on;
    
    subplot(1,3,3);
    plot(relDiff,'-');
    title('Relative Diff');
    ylabel('Relative Difference');
    grid on;
    
    diff
    
    relDiff
    
end

