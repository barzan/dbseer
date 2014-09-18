function [Header Monitor LatencyAvg LatencyPercentile Counts DiffedMonitor StatementStat] = ...
  load_stats(header_path, monitor_path, trans_count_path, avg_latency_path, percentile_latency_path, statement_stat_path, skipFromBegining, skipFromEnd, flag, page_info_path)

Monitor = csvread(monitor_path,2,0); % dyoon: 0 added as third argument for col.

startIdx = 1 + skipFromBegining;
if flag == true
    endIdx = size(Monitor, 1) - skipFromEnd;
else
    endIdx = skipFromEnd;
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

StatementStat = {};
StatementCounts = csvread(statement_stat_path, 2, 0);
numTable = floor(size(StatementCounts, 2)/4);
tables = textread(statement_stat_path, '%s', numTable, 'delimiter', ',');
StatementCounts = StatementCounts(:,2:(numTable*4+1));
StatementStat{end+1} = tables;
StatementStat{end+1} = StatementCounts;

DiffedMonitor = diff(Monitor(startIdx:end,:));
LatencyAvg = load(avg_latency_path);
LatencyAvg = LatencyAvg(:,2:end); % getting rid of the rightmost column which is just some timestamps!
%prclat = load(horzcat(inputDir,'/trans-',signature,'_prctile_latencies.mat'));
Counts = load(trans_count_path);
Counts = Counts(:,2:end); % getting rid of the rightmost column which is just some timestamps!

LatencyPercentile = load(percentile_latency_path);

if nargin == 6
   Monitor = Monitor(startIdx:end,:);
   LatencyAvg = LatencyAvg(startIdx:end,:);
   Counts = Counts(startIdx:end,:);
   % DiffedMonitor = DiffedMonitor(startIdx+1:end-1,:);
   DiffedMonitor = vertcat(zeros(1,size(DiffedMonitor,2)), DiffedMonitor);
end

if nargin >= 7
   Monitor = Monitor(startIdx:end,:);
   LatencyAvg = LatencyAvg(startIdx:end,:);
   Counts = Counts(startIdx:end,:);
   % DiffedMonitor = DiffedMonitor(startIdx+1:end-1,:);
   DiffedMonitor = vertcat(zeros(1,size(DiffedMonitor,2)), DiffedMonitor);
   % Monitor = Monitor(startIdx+2:endIdx,:);
   % LatencyAvg = LatencyAvg(startIdx+2:endIdx,:);
   % Counts = Counts(startIdx+2:endIdx,:);
   % DiffedMonitor = DiffedMonitor(startIdx+1:endIdx-1,:);
end

end

