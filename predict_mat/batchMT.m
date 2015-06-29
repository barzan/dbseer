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

overallTime = tic;
delete maxThroughput.txt;

linfitCPU([1 2 3 4 5], 't12345', 1250, 7000, 't1', 1250, 2250, 't2', 1250, 2250, 't3', 1250, 2250, 't4', 1250, 2250, 't5', 1250, 2250);
linfitCPU([1 2 3 4 5], 't12345-b0-orig', 4600, 7000, 't12345-b0-orig', 3600, 4600);
linfitCPU([1 2 3 4 5], 't12345-b1', 3600, 7000, 't12345-b0-orig', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b2', 3600, 7000, 't12345-b0-orig', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b3', 3600, 7000, 't12345-b0-orig', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b4', 3600, 7000, 't12345-b0-orig', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b5', 3600, 7000, 't12345-b0-orig', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-00', 3600, 7000, 't12345-b1', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b1', 4600, 7000, 't12345-b1', 3600, 4600);
linfitCPU([1 2 3 4 5], 't12345-b0-orig', 3600, 7000, 't12345-b1', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b2', 3600, 7000, 't12345-b1', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b3', 3600, 7000, 't12345-b1', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b4', 3600, 7000, 't12345-b1', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b5', 3600, 7000, 't12345-b1', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-00', 3600, 7000, 't12345-b4', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b4', 4600, 7000, 't12345-b4', 3600, 4600);
linfitCPU([1 2 3 4 5], 't12345-b1', 3600, 7000, 't12345-b4', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b2', 3600, 7000, 't12345-b4', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b3', 3600, 7000, 't12345-b4', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b0-orig', 3600, 7000, 't12345-b4', 3600, 7000);
linfitCPU([1 2 3 4 5], 't12345-b5', 3600, 7000, 't12345-b4', 3600, 7000);



elapsed = toc(overallTime);
fprintf(1,'elapsed time=%f\n',elapsed);

