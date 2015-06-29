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

function compareDirs( signature, jump, varargin)

dim1 = [1 1 3 2 2 2 3 3 3 4 4 4 4 4 4 4];
dim2 = [1 2 1 2 3 3 3 3 3 3 3 3 4 4 4 4];

num = size(varargin,2);
%D1 = dim1(num*jump);
%D2 = dim2(num*jump);
D1 = num;
D2 = jump;


screen_size = get(0, 'ScreenSize');
fh = figure('Name','?!');
set(fh, 'Position', [0 0 screen_size(3) screen_size(4)]);

for i=1:num
    load_and_plot(horzcat(varargin{i},'/monitor-',signature),horzcat(varargin{i},'/trans-',signature), 1+(i-1)*jump, D1, D2);
    for j=1:jump
        subplot(D1, D2, j+(i-1)*jump);
        title(horzcat(varargin{i},' : ',signature));
        if i>1
            legend('off');
        end
    end
end

end
