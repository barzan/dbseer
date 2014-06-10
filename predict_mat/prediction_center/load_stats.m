function [Header Monitor LatencyAvg LatencyPercentile Counts DiffedMonitor] = load_stats(header_path, monitor_path, trans_count_path, avg_latency_path, percentile_latency_path, skipFromBegining, skipFromEnd, flag)

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

DiffedMonitor = diff(Monitor);
LatencyAvg = load(avg_latency_path);
LatencyAvg = LatencyAvg(:,2:end); % getting rid of the rightmost column which is just some timestamps!
%prclat = load(horzcat(inputDir,'/trans-',signature,'_prctile_latencies.mat'));
Counts = load(trans_count_path);
Counts = Counts(:,2:end); % getting rid of the rightmost column which is just some timestamps!

LatencyPercentile = load(percentile_latency_path);

if nargin == 6
   Monitor = Monitor(startIdx+2:end,:);
   LatencyAvg = LatencyAvg(startIdx+2:end,:);
   Counts = Counts(startIdx+2:end,:);
   DiffedMonitor = DiffedMonitor(startIdx+1:end-1,:);
end

if nargin >= 7
   Monitor = Monitor(startIdx+2:endIdx,:);
   LatencyAvg = LatencyAvg(startIdx+2:endIdx,:);
   Counts = Counts(startIdx+2:endIdx,:);
   DiffedMonitor = DiffedMonitor(startIdx+1:endIdx-1,:);
end

end

