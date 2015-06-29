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

function [ Y ] = recpow(X, n)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

if round(n)~= n
    error('The power has to be an integer%f\n', n);
end

if n==Inf || isnan(n)
    Y = X.^n;    
    return
end
    
if n==0
    Y = ones(size(X));
else
    if n==1
        Y = X;
    else
        if mod(n,2) == 0
            Z = recpow(X, n/2);
            Y = Z .* Z;
        else
            Z = recpow(X, (n-1)/2);
            Y = Z .* Z .* X;
        end
    end
end

end
