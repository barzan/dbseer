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

config='7200-10-10-10-10-2000';
%header_index;
coef = horzcat('/Users/sina/expr5/deverticalized/coefs-', config, '_rough_trans_count');
monitor=horzcat('/Users/sina/expr5/raw/monitor-', config, '.csv');
C = load(coef);
C = C(2:7200,:);

M=load(monitor);
hold off;
plot(sum(C'), 'g');
hold on;
%plot(M(:,Com_commit)+100, 'r');
plot(M(3+1199:end,Com_commit)+200, 'b');


