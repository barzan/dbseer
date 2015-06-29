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

function [ Monitor LatencyAvg Counts DiffedMonitor] = load3( inputDir, signature, startIdx, endIdx )

Monitor = csvread(horzcat(inputDir,'/monitor-',signature),2,0);

%Monitor = [Monitor zeros(size(Monitor,1), 11)];

DiffedMonitor = diff(Monitor);
LatencyAvg = load(horzcat(inputDir,'/trans-',signature,'_avg_latency.al'));
%prclat = load(horzcat(inputDir,'/trans-',signature,'_prctile_latencies.mat'));
Counts = load(horzcat(inputDir,'/trans-',signature,'_rough_trans_count.al'));
Counts = Counts(:,2:end); % getting rid of the rightmost column which is just some timestamps!

if nargin == 3
   Monitor = Monitor(startIdx+1:end,:);
   LatencyAvg = LatencyAvg(startIdx+1:end,:);
   Counts = Counts(startIdx+1:end,:);
   DiffedMonitor = DiffedMonitor(startIdx:end-1,:);
end

if nargin == 4
   Monitor = Monitor(startIdx+1:endIdx,:);
   LatencyAvg = LatencyAvg(startIdx+1:endIdx,:);
   Counts = Counts(startIdx+1:endIdx,:);
   DiffedMonitor = DiffedMonitor(startIdx:endIdx-1,:);
end

end

