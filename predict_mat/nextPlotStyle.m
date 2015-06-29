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

function [ nextStyle ] = nextPlotStyle( reset )

colors = {'b','r','g', 'm', 'y', 'c', 'k'};
symbols = {'*-', '+:', 'o', 'x', '.', 'v', '+', '^'};


persistent colorIdx;
persistent symbolIdx;

if (isempty(colorIdx) && isempty(symbolIdx)) || (nargin == 1 && reset==true)
    colorIdx = 1;
    symbolIdx = 1;  
else
    colorIdx = colorIdx + 1;
    symbolIdx = symbolIdx + 1;
end

if symbolIdx>length(symbols)
    symbolIdx = 1;
end
if colorIdx>length(colors)
    colorIdx = 1;
end

nextStyle = [colors{colorIdx} symbols{symbolIdx}];

end

