%%
% Each dataset has the following information associated with it:
%
% dir:  the directory in which it resides
%
% signature: E.g., when signature is "t12345-diff-memless" you need to have the following files in "dir":
%monitor-t12345-diff-memless                     
%trans-t12345-diff-memless_prctile_latencies.mat 
%trans-t12345-diff-memless_avg_latency.al        
%trans-t12345-diff-memless_rough_trans_count.al
%
% tranTypes:    E.g. [1 2 3 4 5] when you have 5 transaction types, 1 to 5.
%
% startIdx:     an integer indicating the first row (inclusive) of the trans-t12345-diff-memless_rough_trans_count.al that should be considered in the analysis 
%
% endIdx:   an integer indicating the last row (inclusive) of the trans-t12345-diff-memless_rough_trans_count.al that should be considered in the analysis 
%
% maxThroughputIdx:     indicating the first row number at which the traces
% have hit the maximum throughput of the system, and rows beyond that
% should not be considered.
% 
% io_conf:      a 3*1 vector containing the configuration learned based on this data that could be used for
% predicting the IO behavior, e.g. [1004040     1100       10]
%
% lock_conf:    a 4*1 vector containing the configuation learned based on this data that could be used for
% predicting the lock behavior, e.g. [0.080645      0.0001           2         0.8]
%   
% groupingStrategy: a struct containing either groups, or groupParams, or
% neither one! 
% groups:   a 2*k matrix, where the i'th row is [a b] indicating the starting
% (inclusive) and ending row number of the i'th group of the data
%
% groupParams:	a struct containing the following fields:
%   minFreq:  [always needed] an integer specifying the min number of rows that can form a group, e.g. 30
%   minTPS:   [always needed] an integer specifying minimum TPS of the rows that are considered for grouping, e.g. 10
%   maxTPS:   [always needed] an integer specifying maximum TPS of the rows that are considered for grouping, e.g. 950
%   groupByTPSinsteadOfIndivCounts:   [needed for GroupByAvg] a boolean which if true (false) considers the average TPS (trans counts) of rows for clustering
%   byWhichTranTypes:     [needed if groupByTPSinsteadOfIndivCounts is false] a vector of integers [a1 a2 ... ak], where each ai is a trans type, specifying which trans counts will be considered to group the rows, e.g. [2 3 5]
%   allowedRelativeDiff:  [needed for GroupByAvg] a number in [0,1] indicating how much the subsequent traces can differ from each other, e.g. 0.3
%   nClusers:     [needed for BetterGroupByAvg] an integer specifying the exact number of groups to partition the data in, e.g. 8, 
% 

%defaults: 

groupParams = struct('allowedRelativeDiff', 0.3, 'minFreq', 30, 'minTPS', 10, 'maxTPS', 950, 'groupByTPSinsteadOfIndivCounts', true, 'nClusers', 8, 'byWhichTranTypes', [1 2 3]);
groups = [];
%typical_conf = struct('dir', '.', 'signature', 'blah', 'tranTypes', [1 2 3 4 5], 'startIdx', 1, 'endIdx', 3000, 'groupingStrategy', groupingStrategy, 'groupingStrategy', groupingStrategy , ...
%    'testMaxThroughputIdx', testMaxThroughputIdx, 'testMaxThroughputIdx', testMaxThroughputIdx, 'io_conf', io_conf, 'lock_conf', lock_conf);

%tpcc4-redo
%% desc: 
dirName = 'tpcc4-redo';
tranTypes = [1 2 3 4 5];

groupParams = struct('allowedRelativeDiff', 0.3, 'minFreq', 30, 'minTPS', 10, 'maxTPS', 950, 'groupByTPSinsteadOfIndivCounts', true);
%'t12345', 2500+600, 2500+1600, 't12345', 2500, 2500+600
groupingStrategy = struct('groupParams', groupParams);
tpcc4_redo_t12345_conf = struct('dir', dirName, 'signature', 't12345', 'tranTypes', tranTypes, 'startIdx', 2500, 'endIdx', 2500+1600, 'groupingStrategy', groupingStrategy);




%testing	testing	actual max throughput	Command	Parameters
				
%t12345-b0-orig:0-615	t12345-b0-orig:55-2128	1549	linfitCPU([1  2  3  4  5],'t12345-b0-orig',4600,7000,'t12345-b0-orig',3600,4600);	io_conf=[669360    1100      20]; lock_conf=[0.080645      0.0001           2         0.8];
%t3:230-4066,t5:212-4066	t35:0-6202	4838	linfitCPU([1  2  3  4  5],'t35',1,8350,'t3',1250,7000,'t5',1250,7000);	io_conf=[1004040     1100     1000]; lock_conf=[0.00625      0.0001           2         0.8];
%,t3:230-4066,t35:225-4066	t5:0-3469	3212	linfitCPU([1  2  3  4  5],'t5',1,8350,'t3',1250,7000,'t35',1250,7000);	io_conf=[1004040     1100     1000]; lock_conf=[0.00625      0.0001           2         0.8];
%,256m-t35:1-7628,256m-t3:1-7628	256m-t5:1-3563	3396	linfitCPU([1  2  3  4  5],'256m-t5',3600,7000,'256m-t35',3600,7000,'256m-t3',3600,7000);	io_conf=[1004040     1100     1000]; lock_conf=[0.00625      0.0001           2         0.8];
%,t12345-b0-orig:0-2128	t12345-b1:1-2175	1504	linfitCPU([1  2  3  4  5],'t12345-b1',3600,7000,'t12345-b0-orig',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b0-orig:0-2128	t12345-b2:0-2233	1891	linfitCPU([1  2  3  4  5],'t12345-b2',3600,7000,'t12345-b0-orig',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b0-orig:0-2128	t12345-b3:1-2239	2041	linfitCPU([1  2  3  4  5],'t12345-b3',3600,7000,'t12345-b0-orig',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b0-orig:0-2128	t12345-b5:1-2057	1557	linfitCPU([1  2  3  4  5],'t12345-b5',3600,7000,'t12345-b0-orig',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b1:1-2175	t12345-00:1-2224	1740	linfitCPU([1  2  3  4  5],'t12345-00',3600,7000,'t12345-b1',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b1:1-451	t12345-b1:82-2175	1504	linfitCPU([1  2  3  4  5],'t12345-b1',4600,7000,'t12345-b1',3600,4600);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b1:1-2175	t12345-b0-orig:0-2128	1549	linfitCPU([1  2  3  4  5],'t12345-b0-orig',3600,7000,'t12345-b1',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b1:1-2175	t12345-b2:0-2233	1891	linfitCPU([1  2  3  4  5],'t12345-b2',3600,7000,'t12345-b1',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b1:1-2175	t12345-b3:1-2239	2041	linfitCPU([1  2  3  4  5],'t12345-b3',3600,7000,'t12345-b1',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b1:1-2175	t12345-b5:1-2057	1557	linfitCPU([1  2  3  4  5],'t12345-b5',3600,7000,'t12345-b1',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b4:0-1593	t12345-00:1-2224	1740	linfitCPU([1  2  3  4  5],'t12345-00',3600,7000,'t12345-b4',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b4:0-1593	t12345-b1:1-2175	1504	linfitCPU([1  2  3  4  5],'t12345-b1',3600,7000,'t12345-b4',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b4:0-1593	t12345-b2:0-2233	1891	linfitCPU([1  2  3  4  5],'t12345-b2',3600,7000,'t12345-b4',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b4:0-1593	t12345-b3:1-2239	2041	linfitCPU([1  2  3  4  5],'t12345-b3',3600,7000,'t12345-b4',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b4:0-1593	t12345-b0-orig:0-2128	1549	linfitCPU([1  2  3  4  5],'t12345-b0-orig',3600,7000,'t12345-b4',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];
%,t12345-b4:0-1593	t12345-b5:1-2057	1557	linfitCPU([1  2  3  4  5],'t12345-b5',3600,7000,'t12345-b4',3600,7000);	io_conf=[1004040     1100       10]; lock_conf=[0.080645      0.0001           2         0.8];

%Max Throughput Datasets!
%training data:
Dt12345_b0_orig_0_615_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0-orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 4600, 'io_conf', [669360    1100      20], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt3_230_4066_conf = struct('dir', './tpcc4-redo', 'signature', 't3', 'tranTypes', [1  2  3  4  5], 'startIdx', 1250, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'groupingStrategy', struct());
Dt5_212_4066_conf = struct('dir', './tpcc4-redo', 'signature', 't5', 'tranTypes', [1  2  3  4  5], 'startIdx', 1250, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'groupingStrategy', struct());
Dt3_230_4066_conf = struct('dir', './tpcc4-redo', 'signature', 't3', 'tranTypes', [1  2  3  4  5], 'startIdx', 1250, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'groupingStrategy', struct());
Dt35_225_4066_conf = struct('dir', './tpcc4-redo', 'signature', 't35', 'tranTypes', [1  2  3  4  5], 'startIdx', 1250, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'groupingStrategy', struct());
D256m_t35_1_7628_conf = struct('dir', './tpcc4-256m', 'signature', '256m-t35', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'groupingStrategy', struct());
D256m_t3_1_7628_conf = struct('dir', './tpcc4-256m', 'signature', '256m-t3', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0_orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0_orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0_orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0_orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b1_1_451_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 4600, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b4_0_1593_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b4', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b4_0_1593_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b4', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b4_0_1593_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b4', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b4_0_1593_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b4', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b4_0_1593_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b4', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());
Dt12345_b4_0_1593_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b4', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'groupingStrategy', struct());

%testing data:
Dt12345_b0_orig_55_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0-orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 4600, 'endIdx', 7000, 'io_conf', [669360    1100      20], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1549, 'groupingStrategy', struct());
Dt35_0_6202_conf = struct('dir', './tpcc4-redo', 'signature', 't35', 'tranTypes', [1  2  3  4  5], 'startIdx', 1, 'endIdx', 8350, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'actualMaxThroughput', 4838, 'groupingStrategy', struct());
Dt5_0_3469_conf = struct('dir', './tpcc4-redo', 'signature', 't5', 'tranTypes', [1  2  3  4  5], 'startIdx', 1, 'endIdx', 8350, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'actualMaxThroughput', 3212, 'groupingStrategy', struct());
D256m_t5_1_3563_conf = struct('dir', './tpcc4-256m', 'signature', '256m-t5', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100     1000], 'lock_conf', [0.00625      0.0001           2         0.8], 'actualMaxThroughput', 3396, 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1504, 'groupingStrategy', struct());
Dt12345_b2_0_2233_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b2', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1891, 'groupingStrategy', struct());
Dt12345_b3_1_2239_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b3', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 2041, 'groupingStrategy', struct());
Dt12345_b5_1_2057_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b5', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1557, 'groupingStrategy', struct());
Dt12345_00_1_2224_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-00', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1740, 'groupingStrategy', struct());
Dt12345_b1_82_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 4600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1504, 'groupingStrategy', struct());
Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0-orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1549, 'groupingStrategy', struct());
Dt12345_b2_0_2233_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b2', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1891, 'groupingStrategy', struct());
Dt12345_b3_1_2239_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b3', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 2041, 'groupingStrategy', struct());
Dt12345_b5_1_2057_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b5', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1557, 'groupingStrategy', struct());
Dt12345_00_1_2224_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-00', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1740, 'groupingStrategy', struct());
Dt12345_b1_1_2175_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b1', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1504, 'groupingStrategy', struct());
Dt12345_b2_0_2233_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b2', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1891, 'groupingStrategy', struct());
Dt12345_b3_1_2239_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b3', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 2041, 'groupingStrategy', struct());
Dt12345_b0_orig_0_2128_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b0-orig', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1549, 'groupingStrategy', struct());
Dt12345_b5_1_2057_conf = struct('dir', './tpcc4-redo', 'signature', 't12345-b5', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 7000, 'io_conf', [1004040     1100       10], 'lock_conf', [0.080645      0.0001           2         0.8], 'actualMaxThroughput', 1557, 'groupingStrategy', struct());




%%%%%%%%%%%%%%%%%%%%%%%%%%
%% FlushRate Prediction datasets
%Training Data	Training_Avg_TPS	Testing_Data	Testing_Avg TPS	Linear Regression			Linear Regression + classification			Our Model			Command	Parameters	Improvement over LR	Improvement Over LR+ classification
%				MAE	MRE	discrete MRE	MAE	MRE	discrete MRE	MAE	MRE	discrete MRE				
%,wiki-dist-100:0-107	100	wiki-dist-900:0-1074	878	479	671	671	479	671	671	22	31	31	linfitCPU([1  2  3  4  5],'wiki-dist-900',1,30000,'wiki-dist-100',1250,30000);	io_conf=[115050     300      27]; allowedRelativeDiff=0.2; minFreq=50;	21.64516129	21.64516129
%,wiki-dist-900:0-1074	900	wiki-dist-100:0-107	100	186	3269	3465	186	3269	3465	3	46	49	linfitCPU([1  2  3  4  5],'wiki-dist-100',1,30000,'wiki-dist-900',1250,30000);	io_conf=[67950    300     39]; allowedRelativeDiff=0.2; minFreq=50;	70.71428571	70.71428571
%,wiki100k-io:0-2044	461	wiki100k-io:0-2044	450	18	19	19	18	19	19	11	11	11	linfitCPU([1  2  3  4  5],'wiki100k-io',3600,24000,'wiki100k-io',3600,24000);	io_conf=[94099    300    119]; allowedRelativeDiff=0.01; minFreq=1000;	1.727272727	1.727272727
%,t12345-brk-100:0-121	100	t12345-brk-500:0-1984	495	151	130	129	164	146	146	25	24	24	linfitCPU([1  2  3  4  5],'t12345-brk-500',1,28000,'t12345-brk-100',4000,28000);	io_conf=[1525423.883              1000       8.606397511]; allowedRelativeDiff=0.4; minFreq=70;	5.375	6.083333333
%,t12345-brk-100:0-121	100	t12345-brk-600:0-2175	592	213	132	132	219	141	141	36	23	23	linfitCPU([1  2  3  4  5],'t12345-brk-600',1,28000,'t12345-brk-100',4000,28000);	io_conf=[1525423.883              1000       8.606397511]; allowedRelativeDiff=0.3; minFreq=70;	5.739130435	6.130434783
%,t12345-brk-100:0-121	100	t12345-brk-600:0-2175	598	227	128	128	224	126	126	28	16	16	linfitCPU([1  2  3  4  5],'t12345-brk-600',1,28000,'t12345-brk-100',4000,28000);	io_conf=[1787598.3575              1000      15.586694996]; allowedRelativeDiff=0.3; minFreq=500;	8	7.875
%,t12345-brk-100:0-121	100	t12345-brk-700:0-2217	690	263	133	133	276	142	141	35	17	17	linfitCPU([1  2  3  4  5],'t12345-brk-700',1,28000,'t12345-brk-100',4000,28000);	io_conf=[1787598.3575              1000      15.586694996]; allowedRelativeDiff=0.3; minFreq=100;	7.823529412	8.294117647
%,t12345-brk-100:0-121	100	t12345-brk-800:0-2344	794	326	135	135	345	146	146	58	19	19	linfitCPU([1  2  3  4  5],'t12345-brk-800',1,28000,'t12345-brk-100',4000,28000);	io_conf=[1787598.3575              1000      15.586694996]; allowedRelativeDiff=0.3; minFreq=100;	7.105263158	7.684210526
%,t12345-brk-100:0-121	100	t12345-brk-900:0-2094	894	361	138	138	389	152	152	57	20	20	linfitCPU([1  2  3  4  5],'t12345-brk-900',1,28000,'t12345-brk-100',4000,28000);	io_conf=[1787598.3575              1000      15.586694996]; allowedRelativeDiff=0.2; minFreq=70;	6.9	7.6
%,t12345-brk-100:0-121,t12345-brk-200:0-230	150	t12345-brk-700:0-2217	690	115	57	57	140	72	72	26	14	14	linfitCPU([1  2  3  4  5],'t12345-brk-700',1,28000,'t12345-brk-100',4000,28000,'t12345-brk-200',4000,28000);	io_conf=[1525423.883              1000       8.606397511]; allowedRelativeDiff=0.3; minFreq=100;	4.071428571	5.142857143
%,t12345-brk-100:0-121,t12345-brk-200:0-230	150	t12345-brk-900:0-2094	894	169	62	62	217	85	85	42	16	16	linfitCPU([1  2  3  4  5],'t12345-brk-900',1,28000,'t12345-brk-100',4000,28000,'t12345-brk-200',4000,28000);	io_conf=[1525423.883              1000       8.606397511]; allowedRelativeDiff=0.2; minFreq=70;	3.875	5.3125
%,t12345-brk-500:0-784	500	t12345-brk-100:0-1351	187	49	165	166	43	134	134	18	28	29	linfitCPU([1  2  3  4  5],'t12345-brk-100',1,28000,'t12345-brk-500',4000,28000);	io_conf=[2242874.3906              1000                20]; allowedRelativeDiff=0.45; minFreq=50;	5.724137931	4.620689655
%,t12345-brk-600:0-1215	600	t12345-brk-100:0-1351	187	38	133	133	36	122	123	12	22	22	linfitCPU([1  2  3  4  5],'t12345-brk-100',1,28000,'t12345-brk-600',4000,28000);	io_conf=[1707887.0511              1000       3.257251899]; allowedRelativeDiff=0.45; minFreq=50;	6.045454545	5.590909091
%,t12345-brk-800:0-1484	799	t12345-brk-200:0-2127	201	46	87	87	43	77	77	8	13	13	linfitCPU([1  2  3  4  5],'t12345-brk-200',1,28000,'t12345-brk-800',4000,28000);	io_conf=[1499771.0694              1000       4.014853186]; allowedRelativeDiff=0.3; minFreq=50;	6.692307692	5.923076923
%,t12345-brk-900:0-1498	899	t12345-brk-200:0-2127	201	50	96	96	46	84	84	8	14	14	linfitCPU([1  2  3  4  5],'t12345-brk-200',1,28000,'t12345-brk-900',4000,28000);	io_conf=[1524984.8632              1000       4.985266236]; allowedRelativeDiff=0.3; minFreq=50;	6.857142857	6
%,t12345-brk-500:0-784	500	t12345-brk-400:0-2248	400	102	146	147	23	31	31	7	12	12	linfitCPU([1  2  3  4  5],'t12345-brk-400',1,28000,'t12345-brk-500',4000,28000);	io_conf=[2242874.3906              1000                20]; allowedRelativeDiff=0.3; minFreq=50;	12.25	2.583333333

%Training data for PageFlush Prediction
groups = [1250 2900; ...
        4250 5900; ...
        7250 8900; ...
        10250 11900; ...
    	13250 14900; ...
        16250 17900; ...
        19250 20900; ...
        22250 23900; ...
        25250 26900; ...
        28250 29900; ...
    ];
groupingStrategy = struct('groups', groups);
Dwiki_dist_100_0_107_conf = struct('dir', './wiki-sigmod', 'signature', 'wiki-dist-100', 'tranTypes', [1  2  3  4  5], 'startIdx', 1, 'endIdx', 30000, 'io_conf',[115050     300      27], 'groupingStrategy', groupingStrategy );
groups = [1250 2900; ...
        4250 5900; ...
        7250 8900; ...
        10250 11900; ...
    	13250 14900; ...
        16250 17900; ...
        19250 20900; ...
        22250 23900; ...
        25250 26900; ...
        28250 29900; ...
    ];
Dwiki_dist_900_0_1074_conf = struct('dir', './wiki-sigmod', 'signature', 'wiki-dist-900', 'tranTypes', [1  2  3  4  5], 'startIdx', 1, 'endIdx', 30000, 'io_conf',[67950    300     39], 'groupingStrategy', groupingStrategy );
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 8, 'minFreq', 1000, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dwiki100k_io_0_2044_conf = struct('dir', './wiki-sigmod', 'signature', 'wiki100k-io', 'tranTypes', [1  2  3  4  5], 'startIdx', 3600, 'endIdx', 24000, 'io_conf',[94099    300    119], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 70, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
%Dt12345_brk_100_0_121_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-100', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',([1787598.3575              1000      15.586694996]+[1525423.883              1000       8.606397511])/2, 'groupingStrategy', groupingStrategy);
Dt12345_brk_100_0_121_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-100', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1525423.883              1000       8.606397511], 'groupingStrategy', groupingStrategy);
%Dt12345_brk_100_0_121_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-100', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1787598.3575              1000      15.586694996], 'groupingStrategy', groupingStrategy);

groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 70, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_200_0_230_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-200', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1525423.883              1000       8.606397511], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_400_0_2248_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-400', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1525423.883              1000       8.606397511], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_500_0_784_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-500', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[2242874.3906              1000                20], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_600_0_1215_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-600', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1707887.0511              1000       3.257251899], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_700_0_2217_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-700', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1707887.0511              1000       3.257251899], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_800_0_1484_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-800', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1499771.0694              1000       4.014853186], 'groupingStrategy', groupingStrategy);
groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2 3 4 5],  'nClusters', 9, 'minFreq', 50, 'minTPS', 30, 'maxTPS', 950);
groupingStrategy = struct('groupParams', groupParams);
Dt12345_brk_900_0_1498_conf = struct('dir', './t-memless-dist', 'signature', 't12345-brk-900', 'tranTypes', [1  2  3  4  5], 'startIdx', 4000, 'endIdx', 28000, 'io_conf',[1524984.8632              1000       4.985266236], 'groupingStrategy', groupingStrategy);


%%%%%%%%%%%%%%%%%%% PostgreSQL

groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2],  'nClusters', 9, 'minFreq', 50, 'minTPS', 90, 'maxTPS', 115);
groupingStrategy = struct('groupParams', groupParams);
Dpsql_t12345_brk_100_var12_conf = struct('dir', '/Users/alekh/Work/RelationalCloud/postgres_experiments/psql_exp5', 'signature', 'pgtpcc', 'tranTypes', [1  2  3  4  5], 'startIdx', 200, 'endIdx', 2900, 'io_conf',[50202             301             100], 'groupingStrategy', groupingStrategy);

groupParams = struct('groupByTPSinsteadOfIndivCounts', false, 'byWhichTranTypes', [1 2],  'nClusters', 9, 'minFreq', 80, 'minTPS', 800, 'maxTPS', 1000);
groupingStrategy = struct('groupParams', groupParams);
Dpsql_t12345_brk_900_var12_conf = struct('dir', '/Users/alekh/Work/RelationalCloud/postgres_experiments/psql_exp6', 'signature', 'pgtpcc', 'tranTypes', [1  2  3  4  5], 'startIdx', 260, 'endIdx', 390, 'io_conf',[50202             301             100], 'groupingStrategy', groupingStrategy);
%flushRateJob = struct('taskName', 'FlushRatePrediction', 'io_conf', train_config1.io_conf, 'workloadName', 'pgtpcc', 'resultsFile', 'FlushRatePrediction.txt', 'appendToFile', false, 'plotX', 'byTPS', 'whichTransToPlot', 1);




