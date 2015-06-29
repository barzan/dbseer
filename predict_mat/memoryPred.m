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

screen_size = get(0, 'ScreenSize');
fontsize=40;
fh1 = figure('Name', 'Memory provisioning','Color',[1 1 1]);
fontsize=40;
subplot(1,1,1, 'fontsize', fontsize);
set(fh1, 'Position', [0 0 screen_size(3) screen_size(4)]);

fontsize =40; % 14 normal, 40 paper;
linewidth=6.5; % 1 normal, 6.5 paper;

MissRates = [.1 .09 .08 .07 .06 .05 .04 .03 .02 .01]; % plot along these points!

confIdx = 1;
%first solving the problem!
ram = [8	4	2	1	0.5	0.25	0.125]; % in GB
actual = [0	0.001	0.002	0.007	0.017	0.06	0.106];
OrigAvgReadMB = [0.375	0.936	1.65	2.836	3.798	4.867	5.474];
avgReadMB = [0.4 0.9	1.6	2.8	3.8	4.9	5.5];
predicted = [0	0	0	0.037	0.064	0.082	0.104];
allPreds = load('cachepred.txt');
predicted = allPreds(confIdx,:);

n = 1000;
moreX = linspace(min(ram), max(ram), n);
moreAct = interp1(ram, actual, moreX, 'linear');
morePred = interp1(ram, predicted, moreX, 'linear');

ph3 = plot(ram, [actual; predicted], '-*');
%hold on;
%ph4 = plot(moreX, [moreAct; morePred])
for i=1:length(avgReadMB)
   text(ram(i)+0.1, actual(i)+0.01, horzcat(num2str(avgReadMB(i)),' MB'),'fontsize', fontsize*.6, 'FontWeight', 'bold'); 
end

xlabel('Memory Size (GB)');
ylabel('Avg Cache Miss Ratio');
legend('Actual', 'Predicted');

set(ph3, 'LineWidth', linewidth);
%set(ph4, 'LineWidth', linewidth);



xActual = zeros(size(MissRates));
xPred = zeros(size(MissRates));
for i=1:length(MissRates)
    curY = MissRates(i);
    [C, I] = min(abs(moreAct-curY));
    xActual(i) = I(1);
    [C, I] = min(abs(morePred-curY));
    xPred(i) = I(1);
    myX = [moreX(xPred(i)) moreX(xActual(i))];
    myY = [curY curY];
    line(myX, myY ,'Color', 'r', 'Linewidth', 1);
end

HorizontalErr = abs(moreX(xPred) - moreX(xActual)); 
err = mean(HorizontalErr);
errP = mre(predicted', actual');
title(horzcat('HErr=', num2str(err),'  PErr=', num2str(errP),' id=', num2str(confIdx)));


fh2 = figure('Name', 'Memory provisioning','Color',[1 1 1]);
set(fh2, 'Position', [0 0 screen_size(3) screen_size(4)]);
subplot(1,1,1,'fontsize',fontsize);

% Mixture of tpcc: 45 43 4 4 4
% Prediction parameters: carlo1=0.2 and carlo2=0.5

ph = bar(MissRates, HorizontalErr, 'g');
title(horzcat('HErr=', num2str(err),'  PErr=', num2str(errP),' id=', num2str(confIdx)));

ph1 = xlabel('Avg Cache Miss Rate');
ph2 = ylabel('Error in Memory Provisioning (GB)');

set(ph, 'LineWidth', linewidth);


axis([0 max(MissRates)+0.005 0 1]);
set(gca,'XTick',[min(MissRates):0.01:max(MissRates)]);
%set(gca,'XTickLabel',['0';' ';'1';' ';'2';' ';'3';' ';'4'])






