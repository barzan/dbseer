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

overTimeDist = load('err-b12t1000');
%overTimeDist = load('err-i2t1-10000');

totalDist = overTimeDist(end,:);
%overTimeDist = diff(overTimeDist);


figure('Name', 'b12t2-10000');

subplot(2,2,1);

plot(totalDist,'.');
%axis([0 160 0 15000]);
xlabel('Thread (Client) id');
ylabel('Total # of transactions sent');

subplot(2,2,2);

plot(overTimeDist);
xlabel('Time (seconds)');
ylabel('TPS per each thread');

subplot(2,2,3);

plot(overTimeDist(:,1:10));
xlabel('Time (seconds)');
ylabel('TPS per each thread (plotted for 10 threads)');


subplot(2,2,4);

plot(overTimeDist(:,10:11));
xlabel('Time (seconds)');
ylabel('TPS per each thread (plotted for 5 threads)');

