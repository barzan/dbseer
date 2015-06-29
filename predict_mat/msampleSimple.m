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

function msampleSimple(columns, coefsFileReady, valFileReady);
A = load(coefsFileReady);
B = load(valFileReady);

addpath('/home/barzan/scripts/');

X = linsolve(A(:,1:columns),B(:,1:1)+B(:,2:2));


dlmwrite('means.dat', mean(X), 'delimiter',',','-append');
dlmwrite('variances.dat', var(X), 'delimiter',',','-append');

err = mean(abs(A(:,1:columns)*X - B(:,1:1)-B(:,2:2)))
err2 = mean(A(:,1:columns)*X - B(:,1:1)-B(:,2:2))


