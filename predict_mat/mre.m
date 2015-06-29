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

function out = mre(predictions, actualdata, varargin)
%if the third parameter is provided as true, then we will first convert
%everything into integers!
if nargin == 3 && varargin{1}
    predictions = ceil(predictions);
    actualdata = ceil(actualdata);
end;

% mean absolute error

out = zeros(1,size(predictions,2));
for i=1:size(predictions,2)
    if i > size(actualdata,2)
        continue
    end
    act = actualdata(:,i);
    pre = predictions(:,i);
    nonz = find(act);

    %My own definition
    out(i) = mean(abs(pre(nonz)-act(nonz))./abs(act(nonz)));
    %Weka!!
    %avgAct = mean(act);
    %out(i) = sum(abs(pre-act)) / sum(abs(avgAct-act));
end

end
