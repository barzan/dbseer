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

function x2 = winsum(y,win);

r = size(y,1);
c = size(y,2);

x = y(1:win*floor(r/win),:);

for i=1:c,
   
    t =sum(reshape(x(:,i),win,ceil(prod(size(x(:,i)))/win)))';
    if(i==1),
        x2=t;
    else
        x2 = [x2 t];
    end
end    

end
