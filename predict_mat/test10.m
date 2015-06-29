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

D=524300; % according to MySQL itself!
log_capacity = 1800*1024;
logRecordConstant = 15;
TPS=1:1:100000;
TPS(:) = 100;
cumTPS=cumsum(TPS);

existingDirtyPages=zeros(1,length(TPS));
existingDirtyPages(1)=D-D*(1-1/D)^cumTPS(1);
for i=2:length(TPS)
    existingDirtyPages(i) = D-D*(1-1/D)^cumTPS(i) - existingDirtyPages(i-1)*TPS(i-1)*logRecordConstant/log_capacity;
end
flushingRate=existingDirtyPages.*TPS * logRecordConstant / log_capacity;


%required_flush_rate = (cumDirtyPages .* TPS) / log_capacity%
%desired_flush_rate = (cumDirtyPages .* TPS) / log_capacity - LRU_flush_rate;


close all
figure
%subplot(3,3,1);
plot([TPS' existingDirtyPages' flushingRate']);
legend('TPS','# current dirty pages','# flushes per sec');
xlabel('TPS');
ylabel('# current dirty pages in the buffer pool');
title('With adaptive flushing');
grid on;
