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

function [p1New p2New p3New] = findP3(n)
%n is the number of steps!
D = 2;
tps = 1;
L = 5;

mixture = [1];

PP = 1:D;
PP = 1./PP;
PP = ones(1,D);
PP = PP / sum(PP);

T = probOfBeingChosenAtLeastOnce(PP, mixture, tps);

p1 = [0.937500 0.937500];
p2 = [0 0];
p3 = 1-p1-p2;

mysum = 0;
for i=0:n-1
    mysum = mysum + (1-T).^(n-1-i) * (1-tps/L)^i;
end

p1New = p1 * (1-tps/L)^n;
p3New = p3 .* (1-T).^n + (tps/L) * p1.* mysum;
p2New = 1-p1New-p3New;

fprintf(1,'%f, %f, %f\n', p1New(1), p2New(1), p3New(1));

end

