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

clc;
tripLenInHour = 12;
timeInLA = 18.75;
x = 0;

unit=0.25;
while x<=1
    TimeAtX = mod(timeInLA + 10 *x, 24);
    fprintf(1,'time in LA=%d, progress=%f, timeRightHere=%d, time in Turkey=%d\n', timeInLA, x, TimeAtX, mod(timeInLA+10,24));
    x=x+unit/tripLenInHour;
    timeInLA=mod(timeInLA+unit, 24);
end

