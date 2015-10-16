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

function [Header Monitor LatencyAvg LatencyPercentile Counts DiffedMonitor] = ...
  load_stats(header_path, monitor_path, trans_count_path, avg_latency_path, percentile_latency_path, statement_stat_path, skipFromBegining, skipFromEnd, flag, tranTypes)

Monitor = csvread(monitor_path,2,0); % dyoon: 0 added as third argument for col.

startIdx = 1 + skipFromBegining;
if flag == true
    endIdx = size(Monitor, 1) - skipFromEnd;
else
	if skipFromEnd > size(Monitor, 1) || skipFromEnd == 0
		endIdx = size(Monitor, 1);
	else
		endIdx = skipFromEnd;
	end
end

%Monitor = [Monitor zeros(size(Monitor,1), 11)];

run(header_path);
Header = header;

% ClusteredPageMix = [];
% ClusteredPageFreq = [];

% if ~isempty(page_info_path) && strcmp(page_info_path, 'null') == false
%   run(page_info_path);
%   ClusteredPageFreq = clusteredPageFreq;
%   ClusteredPageMix = clusteredPageMix;
% end

%StatementStat = {};
%StatementCounts = csvread(statement_stat_path, 2, 0);
%numTable = floor(size(StatementCounts, 2)/4);
%tables = textread(statement_stat_path, '%s', numTable, 'delimiter', ',');
%StatementCounts = StatementCounts(:,2:(numTable*4+1));
%StatementStat{end+1} = tables;
%StatementStat{end+1} = StatementCounts;

DiffedMonitor = diff(Monitor(startIdx:endIdx,:));
% LatencyAvg = load(avg_latency_path);
LatencyAvg = dlmread(avg_latency_path);
%prclat = load(horzcat(inputDir,'/trans-',signature,'_prctile_latencies.mat'));
% Counts = load(trans_count_path);
Counts = dlmread(trans_count_path);

if isempty(percentile_latency_path)
  [currentPath, name, ext] = fileparts(avg_latency_path);
  LatencyPercentile = struct();
  LatencyPercentile.latenciesPCtile = load_prctile_from_file(currentPath, LatencyAvg);
else
  LatencyPercentile = load(percentile_latency_path); 
  LatencyPercentile.latenciesPCtile = LatencyPercentile.latenciesPCtile(startIdx:endIdx, :, :); 
end

LatencyAvg = LatencyAvg(:,2:end); % getting rid of the rightmost column which is just some timestamps!
Counts = Counts(:,2:end); % getting rid of the rightmost column which is just some timestamps!

LatencyAvg = LatencyAvg(:, tranTypes);
Counts = Counts(:, tranTypes);
LatencyPercentile.latenciesPCtile = LatencyPercentile.latenciesPCtile(:, [1 tranTypes+1], :);

if nargin == 6
   Monitor = Monitor(startIdx:end,:);
   LatencyAvg = LatencyAvg(startIdx:end,:);
   Counts = Counts(startIdx:end,:);
   % DiffedMonitor = DiffedMonitor(startIdx+1:end-1,:);
   DiffedMonitor = vertcat(zeros(1,size(DiffedMonitor,2)), DiffedMonitor);
end

if nargin >= 7
   Monitor = Monitor(startIdx:endIdx,:);
   LatencyAvg = LatencyAvg(startIdx:endIdx,:);
   Counts = Counts(startIdx:endIdx,:);
   % DiffedMonitor = DiffedMonitor(startIdx+1:end-1,:);
   DiffedMonitor = vertcat(zeros(1,size(DiffedMonitor,2)), DiffedMonitor);
   % Monitor = Monitor(startIdx+2:endIdx,:);
   % LatencyAvg = LatencyAvg(startIdx+2:endIdx,:);
   % Counts = Counts(startIdx+2:endIdx,:);
   % DiffedMonitor = DiffedMonitor(startIdx+1:endIdx-1,:);
end

end

