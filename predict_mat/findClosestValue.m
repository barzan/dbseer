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

function closest = findClosestValue(function_handler, inputRange, value, conf)

lb = 1;
ub = size(inputRange, 1);

iter = 1;

%if feval(function_handler, conf, inputRange(ub,:)) < value || feval(function_handler, conf, inputRange(lb,:)) > value
if feval(function_handler, conf, inputRange(ub,:)) < value    
    closest = Inf;
    return
end    
if feval(function_handler, conf, inputRange(lb,:)) > value
    closest = -Inf;
    return
end

while ub - lb > 100
    closest = round((ub+lb)/2);
    %fprintf(1,'Checking %d\n', closest);
    iter = iter + 1;
    cv = feval(function_handler, conf, inputRange(closest,:));
    if cv < value
        lb = closest;
    else
        if cv > value
            ub = closest;
        else % equals!
            lb = closest;
            ub = closest;
        end
    end
end

closest = round((ub+lb)/2);

iter 
end

