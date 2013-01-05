function linfitCPU()
header_aligned;

%[M1 L1 C1] = load3('syn-cpuN', 'cpu12-0-100', 325,350);
%[M2 L2 C2] = load3('syn-cpuN', 'cpu12-100-0', 330,445);
%[M3 L3 C3] = load3('syn-cpuN', 'cpu12-50-50', 330,373);

[M1 L1 C1] = load3('syn-io-8g', 'io1');
[M2 L2 C2] = load3('syn-io-8g', 'io2');
[M3 L3 C3] = load3('syn-io-8g', 'io3');


trainC = [C1(:,7:8); C2(:,7:8) ];
trainP = [CpuUserAvg(M1); CpuUserAvg(M2)];


testC = [C3(:,7:8)];
testP = [CpuUserAvg(M3)];

model = regress(trainP, [trainC ones(size(trainC),1)]);

predictions = [testC ones(size(testC,1),1)]*model; 


close all;
figure
plot(testP, 'b');
hold on;
plot(predictions, 'r');
legend('actual CPU usage (%)', 'predicted CPU(%)');
text(5,20, horzcat('MAE=',num2str(mean(abs(predictions-testP)))));


range=1:1:size(trainC,2);
combs = combnk(range, 2);
comb1 = combs(:,1);
comb2 = combs(:,2);

blownTrainC = [trainC trainC.*trainC trainC(:, comb1).*trainC(:, comb2)];
trainP = [CpuUserAvg(M1); CpuUserAvg(M2)];

testC = [C3(:,7:8)];
blownTestC = [testC testC.*testC testC(:, comb1).*testC(:, comb2)];
testP = [CpuUserAvg(M3)];

blownModel = regress(trainP, [blownTrainC ones(size(blownTrainC,1),1)]);

blownPredictions = [blownTestC ones(size(testC,1),1)]*blownModel; 

figure
plot(testP, 'b');
hold on;
plot(blownPredictions, 'r');
legend('actual CPU usage (%)', 'blown predicted CPU(%)');
text(5,20, horzcat('MAE=',num2str(mean(abs(blownPredictions-testP)))));


