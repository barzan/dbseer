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

function [Headers Monitors LatencyAvgs LatencyPercentiles TransactionCounts DiffedMonitors] = load_stats2(datasets, skipFromBegining, skipFromEnd, flag, tranTypes)

num_dataset = size(datasets, 2);
Headers = cell(1,num_dataset);
Monitors = cell(1,num_dataset);
LatencyAvgs = cell(1,num_dataset);
LatencyPercentiles = cell(1,num_dataset);
TransactionCounts = cell(1,num_dataset);
DiffedMonitors = cell(1,num_dataset);

for i=1:num_dataset

  monitor_path = datasets{i}.monitor_path;
  header_path = datasets{i}.header_path;
  avg_latency_path = datasets{i}.avg_latency_path;
  trans_count_path = datasets{i}.trans_count_path;
  percentile_latency_path = datasets{i}.percentile_latency_path;

  Monitor = csvread(monitor_path,2,0); % dyoon: 0 added as third argument for col.
  LatencyAvg = dlmread(avg_latency_path);

  startIdx = 1 + skipFromBegining;
  if flag == true
    endIdx = size(Monitor, 1) - skipFromEnd;
    if endIdx > size(LatencyAvg, 1)
        endIdx = size(LatencyAvg, 1);
    end
  else
    if skipFromEnd > size(Monitor, 1) || skipFromEnd == 0
      endIdx = size(Monitor, 1);
      if endIdx > size(LatencyAvg, 1)
        endIdx = size(LatencyAvg, 1);
      end
    else
      endIdx = skipFromEnd;
    end
  end

  run(header_path);
  Header = header;
  DiffedMonitor = diff(Monitor(startIdx:endIdx,:));
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

  Monitor = Monitor(startIdx:endIdx,:);
  LatencyAvg = LatencyAvg(startIdx:endIdx,:);
  Counts = Counts(startIdx:endIdx,:);
  DiffedMonitor = vertcat(zeros(1,size(DiffedMonitor,2)), DiffedMonitor);

  Headers{i} = Header;
  Monitors{i} = Monitor;
  LatencyAvgs{i} = LatencyAvg;
  LatencyPercentiles{i} = LatencyPercentile;
  TransactionCounts{i} = Counts;
  DiffedMonitors{i} = DiffedMonitor;
end

end % end function

