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

% the function receives in input a data file a window size in seconds and
% the nubmer of transaction types
% and returns 2 matrixes containing the number of transactions per type per
% window and the average latency per window



%filename = 'coefs-7200-20-20-20-20-3000';
%winSize=1;
%numVariable=5;
function [roughNumTrans  latencies  temp] = fast_deverticalize(indir, outdir, filename, winSize, numVariable)
    
    tic;
    % load the file skipping the first 4 lines
    temp = csvread(horzcat(indir,filename),4);
    
    %bring measure in timewindows (if winSize is 1 than it is in seconds)
    temp(:,2) = temp(:,2)/(winSize*1000000);
    temp(:,3) = temp(:,3)/(winSize*1000000);
    
    fprintf(1, 'READ FROM DISK TIME:');
    toc;
    
    tic;
    
    % create empty matrixes
    roughNumTrans = zeros(ceil(max(temp(:,2))),numVariable);
    %numTrans = zeros(ceil(max(temp(:,2)))-1,numVariable);
    latencies = zeros(ceil(max(temp(:,2))),numVariable);
    
    % for every transaction we recorded
    for i=1:size(temp,1)
        %roughNumTrans(ceil(temp(i,2))+1,temp(i,1)) = roughNumTrans(ceil(temp(i,2))+1,temp(i,1)) + 1;  % accumulate how many trans start in each window 
%i
%ceil(temp(i,2)+temp(i,3))
%size(roughNumTrans,1)
	if(temp(i,3) > 0) 
       		roughNumTrans(min(ceil(temp(i,2)+temp(i,3)),size(roughNumTrans,1)),temp(i,1)) = roughNumTrans(min(ceil(temp(i,2)+temp(i,3)),size(roughNumTrans,1)),temp(i,1)) + 1;  % accumulate how many trans start in each window 
        	latencies(min(ceil(temp(i,2)+temp(i,3)),size(roughNumTrans,1)),temp(i,1)) = latencies(min(ceil(temp(i,2)+temp(i,3)),size(roughNumTrans,1)),temp(i,1)) + temp(i,3);  % accumulate latency values
    	end
    end
    
    latencies = latencies./roughNumTrans; % actually compute latency AVG
        
    fprintf(1, 'CRUNCH TIME:');
    toc;
    
    fprintf(1, 'SAVE TIME:');
    tic;
    save(horzcat(outdir,filename,'_rough_trans_count'), 'roughNumTrans','-ascii');
    save(horzcat(outdir,filename,'_avg_latency'), 'latencies','-ascii');
    toc;
    
end
