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

function elmOfBonA1 = alignnew(coef_columns, monitor_attr_index, coefFileDev, monitorFile, latencyFile, ...
     alignFile1, alignFile2, alignFile3, manualCorrection)

A = load(coefFileDev);
B = load(monitorFile);
L = load(latencyFile);

startA=2;
endA=7200;
startB=1199;

X = sum(A(startA:endA,1:coef_columns)');
Y = B(startB:end,monitor_attr_index)';

X1=xcorr(X-mean(X),Y-mean(Y));
%[m,d]=max(X1);
%delay=d-max(length(X),length(Y));
mm = max(length(X),length(Y));
manualMax = 5;
%mm
reasonable = X1(max(1,mm-manualMax):mm);
%reasonable
[maxCor,d]=max(reasonable);
d = d + mm - length(reasonable);
elmOfBonA1 = mm+1-d;

commonLen = min(length(X)-1, length(Y)-elmOfBonA1); 

dlmwrite(alignFile1, A(startA:startA+commonLen,:), 'delimiter',',');
dlmwrite(alignFile3, L(startA:startA+commonLen,:), 'delimiter',',');
dlmwrite(alignFile2, B(startB-1+elmOfBonA1+manualCorrection:startB-1+elmOfBonA1+commonLen+manualCorrection,:), 'delimiter',',');

startB+elmOfBonA1
end
