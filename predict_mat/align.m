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

function result = align(columns, coefFileDev, valFileCut, alignFile1, alignFile2)

A = load(coefFileDev);
B = load(valFileCut);

X = sum(A(:,1:columns)');
Y = B(:,1:1)+B(:,2:2);

X1=xcorr(X-mean(X),Y-mean(Y));
%[m,d]=max(X1);
%delay=d-max(length(X),length(Y));
mm = max(length(X),length(Y));
reasonable = X1(max(1,mm-120):mm);
[maxCor,d]=max(reasonable);
d = d + mm - length(reasonable);
elmOfBonA1 = mm+1-d;
elmOfBonA1

commonLen = min(length(X)-1, length(Y)-elmOfBonA1); 

dlmwrite(alignFile1, A(1:1+commonLen,:), 'delimiter',',');
dlmwrite(alignFile2, B(elmOfBonA1:elmOfBonA1+commonLen,:), 'delimiter',',');

result = 1
