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

close all;
figure

Ypredicted =   0.000000031927 .* Callez1(:,1).*Callez1(:,2) ...
    - 0.000128888666 .* Mallez1(:,Com_delete) ...
    + 0.00000180394 .* Mallez1(:,Innodb_row_lock_time)  ...
    + 0.000003876117 .* Mallez1(:,Innodb_rows_updated) ...
    - 0.081137780296 .* log(Mallez1(:,Innodb_log_writes)) ...
    + 0.530226260608;

Yactual = Lallez1(:, 1);

subplot(2,2,1);
plot(Yactual,'b');
hold on;
plot(Ypredicted, 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (entire data)');
grid;

subplot(2,2,2);
plot(Yactual(1:1000),'b');
hold on;
plot(Ypredicted(1:1000), 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (zoomed in)');
grid;

subplot(2,2,3);
plot(Yactual(36000:36500),'b');
hold on;
plot(Ypredicted(36000:36500), 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (easiest workload)');
grid;


subplot(2,2,4);
plot(Yactual(34900:35000),'b');
hold on;
plot(Ypredicted(34900:35000), 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (zoomed in)');
grid;