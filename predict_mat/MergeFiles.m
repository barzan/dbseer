function MergeFiles(outputSignature, varargin)

M = [];
L = [];
pL = [];
C = [];

check = [];

for i=1:size(varargin,2)
    M = [M; csvread(horzcat('monitor-',varargin{i}),2)];
    L = [L; load(horzcat('trans-',varargin{i},'_avg_latency.al'))];
    pL = [pL; load(horzcat('trans-',varargin{i},'_prctile_latencies.mat'))];
    C = [C; load(horzcat('trans-',varargin{i},'_rough_trans_count.al'))];
    
    check = [check; size(M,1) size(L,1) size(pL,1) size(C,1)];
end

csvwrite(horzcat('monitor-',outputSignature), M);
save(horzcat('trans-',outputSignature,'_avg_latency.al'),'L', '-ascii');
save(horzcat('trans-',outputSignature,'_prctile_latencies.mat'),'pL');
save(horzcat('trans-',outputSignature,'_rough_trans_count.al'),'C', '-ascii');

end

