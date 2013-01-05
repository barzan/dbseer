function [C L M] = loadAligned( dir, config)
% Returns the counts in C, the latencies in L, and the monitoring numbers
% in M.
%   Detailed explanation goes here

C = load(horzcat(dir, '/coefs-', config, '_count.al'));
L = load(horzcat(dir, '/coefs-', config, '_latency.al'));
M = load(horzcat(dir, '/monitor-', config, '.al'));

end

