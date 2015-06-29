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

function ph = drawLine(HorzOrVert, ColorStyle, value, caption)

ax = gca;
xlim = get(ax, 'XLim');
xmin = xlim(1); xmax=xlim(2);
ylim = get(ax, 'YLim');
ymin = ylim(1); ymax=ylim(2);

s=50;

if nargin < 4
    caption = '';
end

if HorzOrVert=='h'
    vals = [(xmin:s:xmax)' repmat(value, size((xmin:s:xmax)'))];
else
    if HorzOrVert=='v'
        vals = [repmat(value, size((ymin:s:ymax)')) (ymin:s:ymax)'];
    else
        error('Invalid HorzOrVert!');
    end
end
hold on;    
ph = plot(vals(:,1), vals(:,2), ColorStyle, 'DisplayName', caption);

end

