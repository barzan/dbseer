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

function P = mapRowsToPages(domain_cardinality, totalRowsChanged)
%This function returns the number of unique pages touched (i.e., dirtied) when the total
%number of requests is `totalRowsChanged' and the total number of unique
%pages is `domain_cardinality'

epsilon = 1e-10;

    n = floor(totalRowsChanged);
    fractionN = totalRowsChanged - n;
    
    D = floor(domain_cardinality);
    fractionD = domain_cardinality - D;

    if fractionN>epsilon
        P = fractionN * mapRowsToPages(domain_cardinality, n+1) + (1-fractionN)*mapRowsToPages(domain_cardinality, n);
    else
       if fractionD>epsilon
           P = fractionD * mapRowsToPages(D+1, n) + (1-fractionD)*mapRowsToPages(D, n);
       else
           if D==0
                P = 0;
           else
                P=  D - D * (1-1/D).^n;
           end
       end
    end
end

