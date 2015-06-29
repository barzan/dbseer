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

function  [suggestion] = alignEZ( config , manualCorrection)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

header_index;

%iC = load(horzcat('/Users/sina/expr5/deverticalized/coefs-', config, '_rough_trans_count'));
%iL = load(horzcat('/Users/sina/expr5/deverticalized/coefs-', config, '_avg_latency'));
[iM, suggestion] = aggregateMonitor(horzcat('/Users/sina/expr5/raw/monitor-', config), false);

alignnew(5,Com_commit, horzcat('/Users/sina/expr5/deverticalized/coefs-', config, '_rough_trans_count'), ...
                       horzcat('/Users/sina/expr5/raw/monitor-', config, '.csv'), ...
                       horzcat('/Users/sina/expr5/deverticalized/coefs-', config, '_avg_latency'), ...
                       horzcat('/Users/sina/expr5/processed/coefs-', config, '_count.al'), ...
                       horzcat('/Users/sina/expr5/processed/monitor-', config, '.al'), ...
                       horzcat('/Users/sina/expr5/processed/coefs-', config, '_latency.al'), ...
                       manualCorrection);
%system(horzcat('cp /Users/sina/expr5/deverticalized/coefs-', config, '_avg_latency', ...
%    ' /Users/sina/expr5/processed/coefs-', config, '_latency.al' ));


end

