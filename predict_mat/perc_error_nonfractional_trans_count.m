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

tc = load('../data/expr5/fine_grained_deverticalization/coefs-7200--0--0--0-50-2000_trans_count');
tcrough = load('../data/expr5/fine_grained_deverticalization/coefs-7200--0--0--0-50-2000_rough_trans_count');

%plot((tc-tcrough)/mean(tc)*100);
plot(sum((tc-tcrough)')'/sum(mean(tc))*100);
ylabel('error (%)');
xlabel('time (sec)');
title('Percentage error for non-fractional transaction counting');
grid;

avgthroughput = sum(mean(tc));
abserr = max(sum((tc-tcrough)'));
fprintf(1,'\nAbsolute max error: %4.4f transactions in 1 sec window (running at %6.2f tps)\n',abserr,avgthroughput);

meanerr = sum(mean((tc-tcrough)));
fprintf(1,'\nAbsolute mean error: %4.4f transactions (The error is almost unbiased)\n ',meanerr);


set(gcf,'Color','w');
