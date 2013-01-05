close all;
figure

subplot(2,2,1);
Iso = load('isotonic-lock-time.txt', '-ascii');
IsoStep = [Iso(1,1) 0; Iso(1,1) Iso(1,2)];
for i=2:size(Iso,1)
   IsoStep = [IsoStep; Iso(i,1) Iso(i-1,2); Iso(i,1) Iso(i,2)] 
end
IsoStep(end,end) = max(iM1(:,Innodb_row_lock_time));
 
plot(IsoStep(:,2), IsoStep(:,1),'b');
hold on;
plot(iM1(:,Innodb_row_lock_time), iL1(:,1), 'r.');
plot(iM3(:,Innodb_row_lock_time), iL3(:,1), 'g.');
plot(iM4(:,Innodb_row_lock_time), iL4(:,1), 'y.');

l = legend('predicted latency (IsotonicRegression)', 'Test data', 'Train data');
ylabel('Latency (sec)');
xlabel('Innodb-row-lock-time');
title('Latency Prediction');
grid;

subplot(2,2,2);
plot(IsoStep(:,2), IsoStep(:,1),'b.');
hold on;
l = legend('predicted latency (IsotonicRegression)');
ylabel('Latency (sec)');
xlabel('Innodb-row-lock-time');
title('Latency Prediction');
set(gca,'XScale','log');
set(gca,'YScale','log');
grid;


subplot(2,2,3);
Iso = load('isotonic-iowrit.txt', '-ascii');
IsoStep = [Iso(1,1) 0; Iso(1,1) Iso(1,2)];
for i=2:size(Iso,1)
   IsoStep = [IsoStep; Iso(i,1) Iso(i-1,2); Iso(i,1) Iso(i,2)] 
end
IsoStep(end,end) = max(iM1(:,io_writ));

plot(IsoStep(:,2), IsoStep(:,1),'b');
hold on;
plot(iM1(:,io_writ), iL1(:,1), 'r.');
plot(iM3(:,io_writ), iL3(:,1), 'g.');
l = legend('predicted latency (IsotonicRegression)', 'Test data', 'Train data');
ylabel('Latency (sec)');
xlabel('io-writ');
title('Latency Prediction');
grid;

subplot(2,2,4);
plot(IsoStep(:,2), IsoStep(:,1),'b.');
hold on;
l = legend('predicted latency (IsotonicRegression)');
ylabel('Latency (sec)');
xlabel('io-writ');
title('Latency Prediction');
%set(gca,'XScale','log');
set(gca,'YScale','log');
grid;

