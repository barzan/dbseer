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

nTypes = 5;
nColumns = 26;
header = 'transType,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26';

f = [0.2 0.2 0.2 0.2 0.2];
howManyRows = 1000;

features = zeros(howManyRows, nColumns+1);

transTypes = randsample(nTypes, howManyRows, true, f);
for i=1:howManyRows
   t = transTypes(i);
   features(i,:) = [t carloWikiTransType(t)];
end

produceWekaFile(header,features, horzcat('features-',num2str(howManyRows),'.csv'));
