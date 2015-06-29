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

A = load('coefsXYZ.ready');
B = load('valXYZ.ready');

addpath('/home/barzan/scripts/');

smoothinwindow = 5;
A = winsum(A,smoothinwindow);
B = winavg(B,smoothinwindow);

%A =[A ones(size(A,1),1)];

numOfEquations = 50;

s=1:size(B,1);

for i=1:floor(size(B,1)/numOfEquations),
%s = randperm(length(B));
  r = s((i-1)*numOfEquations+1:i*numOfEquations); %100 could be at little as 5
  X = linsolve([A(r,:) sum(B(r,2:3)')'],100-B(r,1:1));
  if (i==1),
    unknowns = X';
  else
    unknowns = [unknowns; X'];
  end
end


dlmwrite('disk-means.dat', mean(unknowns), 'delimiter',',','-append');
dlmwrite('disk-variances.dat', var(unknowns), 'delimiter',',','-append');


