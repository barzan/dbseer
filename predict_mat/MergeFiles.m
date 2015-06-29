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

