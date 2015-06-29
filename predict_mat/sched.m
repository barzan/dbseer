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

function sched( )
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

n=1;
t = [6 2.3];
W = zeros(1,2);
A = zeros(99,2);
for i=1:99
    r = [i 100-i] / 100.0;
    L = dot(t,r);
    W(1)=0.5 * r(1) * n * L;
    W(2)=(r(1)*n + 0.5*r(2)*n) * L;
    A(i,:)=dot(r,W) + t;
end
plot(A);
end

